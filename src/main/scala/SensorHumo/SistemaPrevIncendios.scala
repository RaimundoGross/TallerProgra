package SensorHumo

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import Sensor.activeMqUrl
import org.apache.activemq.command.ActiveMQObjectMessage

import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration

object SistemaPrevIncendios {

  def main(args: Array[String]): Unit = {
    System.setProperty("PROVIDER_URL", "tcp://localhost:61616")
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", """*""")
    println("[*] SPI: Conectando...")
    val cFactory = new ActiveMQConnectionFactory(activeMqUrl)

    val connection = cFactory.createConnection()
    connection.start()
    println("[*] SPI: Conexion establecida.\n[*] SPI: Creando cola de mensajes...")

    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val hq = session.createQueue("HumoQueue")
    val consumidor = session.createConsumer(hq)


    val listener = new MessageListener {
      def onMessage(message: Message): Unit ={
        //println(message)

        message match {
          case objeto: ObjectMessage => {
            val mensaje = objeto.asInstanceOf[ActiveMQObjectMessage]
            //println(mensaje)
            val lectura = mensaje.getObject.asInstanceOf[DatoHumo]
            //println(lectura.nivelHumo)

            //Creacion colas de msg
            val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
            val rq = session.createQueue("RociadoresQueue")
            val aq = session.createQueue("AlarmaQueue")
            val bq = session.createQueue("BomberosQueue")

            //Se extrae el valor del objeto
            val nivelHumo: Int = lectura.nivelHumo
            println(s"[*] SPI: Lectura de Humo: $nivelHumo [ppm]")
            val alerta: Alerta = new Alerta()

            //Procesamiento del valor
            if (nivelHumo > 100) {
              println("[*] SPI: Warning: La lectura recibida supera el umbral seguro. Activando plan de emergencia.")
              alerta.setEmergencia(true)
            } else {
              alerta.setEmergencia(false)
            }

            val prodRoc = session.createProducer(rq)    //prod para rociadores
            val prodAla = session.createProducer(aq)    //prod para alarma
            val prodBom = session.createProducer(bq)    //prod para Bomberos

            val objMessage = session.createObjectMessage(alerta)
            prodRoc.send(objMessage)
            prodAla.send(objMessage)
            prodBom.send(objMessage)

            // Comunicación con API para persistencia del dato
            val cliente: HttpClient = HttpClient.newHttpClient()
            val req : HttpRequest = HttpRequest.newBuilder().uri(URI.create("https://weoibvi1j2.execute-api.sa-east-1.amazonaws.com/dev/persiste-humo/crear-persiste-humo")).timeout(Duration.ofMinutes(2))
              .header("Content-Type", "application/json").POST(BodyPublishers.ofString(s"{ 'cant_humo':$nivelHumo}")).build
            cliente.sendAsync(req, BodyHandlers.ofString()).thenAccept(println)
          }
          case _ => {
            throw new Exception("Error desconocido")
          }
        }
      }
    }
    consumidor.setMessageListener(listener)
    println("[*] SPI: Cola creada: Escuchando...")

  }
}