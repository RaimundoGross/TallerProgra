package SensorHumo

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import Sensor.activeMqUrl

object Rociadores {

  def main(args: Array[String]): Unit = {
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", """*""")
    val cFactory = new ActiveMQConnectionFactory(activeMqUrl)
    val connection = cFactory.createConnection
    connection.start()

    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val rq = session.createQueue("RociadoresQueue")

    val consumidor = session.createConsumer(rq)

    val listener = new MessageListener {
      def onMessage(message: Message): Unit ={
        message match {
          case objeto: ObjectMessage =>
            val alerta : Alerta = objeto.getObject.asInstanceOf[Alerta]

            val emergencia : Boolean =  alerta.getEmergencia
            if(emergencia){
                println("[°] Rociadores: Activando rociadores!")
            }
            

          case _ =>
            throw new Exception("Error desconocido")

        }
      }
    }
    consumidor.setMessageListener(listener)
  }
}