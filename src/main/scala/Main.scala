import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient

import scala.io.StdIn

object Main {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  def main(args: Array[String]): Unit = {

    val uri: String = "mongodb://localhost:27017"
    val client: MongoClient = MongoClient(uri)
    val codecRegistry = fromRegistries(fromProviders(classOf[Prode], classOf[Game], classOf[Group]), MongoClient.DEFAULT_CODEC_REGISTRY)
    val database = client.getDatabase("prodes").withCodecRegistry(codecRegistry)

    val prodeConsumer = new ProdeConsumer(database)
    val groupConsumer = new GroupConsumer(database)

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(concat(prodeConsumer.route, groupConsumer.route))

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
