import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import games.{CompleteGame, GroupStage, Statistics}
import group.{Group, GroupConsumer}
import jwt.SessionConsumer
import org.mongodb.scala.bson.codecs.Macros._
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient
import prode.{Prode, ProdeConsumer}

import scala.io.{Source, StdIn}

object Main {
  implicit val system = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.executionContext

  def main(args: Array[String]): Unit = {
     val uri: String = "mongodb://localhost:27017"
     val client: MongoClient = MongoClient(uri)
     val codecRegistry = fromRegistries(
       fromProviders(classOf[Prode], classOf[GroupStage], classOf[CompleteGame], classOf[Statistics], classOf[Group]),
       MongoClient.DEFAULT_CODEC_REGISTRY
     )
     val database = client.getDatabase("prodes").withCodecRegistry(codecRegistry)

     val groupConsumer = new GroupConsumer(database)
     val prodeConsumer = ProdeConsumer(database,groupConsumer.groupService)
     val sessionConsumer = new SessionConsumer()

     val bindingFuture = Http().newServerAt("localhost", 8080)
       .bind(concat(prodeConsumer.route, groupConsumer.route, sessionConsumer.route))

     println(s"Server now online. Please navigate to http://localhost:8080\nPress RETURN to stop...")
     StdIn.readLine() // let it run until user presses return
     bindingFuture
       .flatMap(_.unbind()) // trigger unbinding from the port
       .onComplete(_ => system.terminate()) // and shutdown when done*/
  }
}
