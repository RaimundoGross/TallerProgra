package SensorHumo

import javax.jms._
import org.apache.activemq.ActiveMQConnectionFactory
import scala.util.Random


object Sensor {
  val activeMqUrl: String = "tcp://localhost:61616"

  def main(args: Array[String]): Unit = {
    System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", """*""")
    val cFactory = new ActiveMQConnectionFactory(activeMqUrl)
    cFactory.setTrustAllPackages(true)
    val connection = cFactory.createConnection()
    connection.start()
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val hq = session.createQueue("HumoQueue")

    val productor = session.createProducer(hq)
    val uuid = java.util.UUID.randomUUID.toString

    val rand: Random = Random
    var cont = 0
    while (cont < 100){
      val dato: DatoHumo = new DatoHumo(rand.nextInt(200))
      val objMessage = session.createObjectMessage(dato)

      productor.send(objMessage)
      println("[~] SENSOR: Mensaje enviado: "+ dato.nivelHumo )
      Thread.sleep(5000)

      cont += 1
    }
    Thread.sleep(10000)
    connection.close()

    val thread: Thread = new Thread(uuid)
    thread.start()
  }
}
