package SensorHumo

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import Sensor.activeMqUrl

object Bomberos {


  def main(args: Array[String]): Unit = {
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", """*""")
    val cFactory = new ActiveMQConnectionFactory(activeMqUrl)
    val connection = cFactory.createConnection
    connection.start()

    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val bq = session.createQueue("BomberosQueue")

    val consumidor = session.createConsumer(bq)

    val listener = new MessageListener {
      def onMessage(message: Message): Unit ={
        message match {
          case objeto: ObjectMessage => {
            val alerta: Alerta = objeto.getObject.asInstanceOf[Alerta]

            val emergencia: Boolean = alerta.getEmergencia
            if (emergencia) {
              println("[-] Bomberos: Enviando SMS a los bomberos!")
            }
          }
          case _ =>
            throw new Exception("Error desconocido")

        }
      }
    }
    consumidor.setMessageListener(listener)
  }
}