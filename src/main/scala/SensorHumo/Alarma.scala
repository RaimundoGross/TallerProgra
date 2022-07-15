package SensorHumo

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import Sensor.activeMqUrl

object Alarma {

  def main(args: Array[String]): Unit = {
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", """*""")
    val cFactory = new ActiveMQConnectionFactory(activeMqUrl)
    val connection = cFactory.createConnection
    connection.start()
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val aq = session.createQueue("AlarmaQueue")

    val consumidor = session.createConsumer(aq)

    val listener = new MessageListener {
      def onMessage(message: Message): Unit ={
        message match {
          case objeto: ObjectMessage =>
            val alerta = objeto.getObject.asInstanceOf[Alerta]

            val emergencia : Boolean =  alerta.getEmergencia
            if(emergencia){
                println("[+] Sist-Alarma: Activando alarmas!")
            } else {
                println("[+] Sist-Alarma: No hay peligro")
            }


          case _ =>
            throw new Exception("Error desconocido")

        }
      }
    }
    consumidor.setMessageListener(listener)
  }
}