import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import games.{CompleteGame, GroupStage, Statistics}
import group.{Group, GroupConsumer}
import jwt.SessionConsumer
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient
import org.mongodb.scala.bson.codecs.Macros._
import prode.{Prode, ProdeConsumer}

import scala.io.StdIn
import scala.util.{Failure, Success}

object AppServer {
  sealed trait Message
  private final case class StartFailed(cause: Throwable) extends Message
  private final case class Started(binding: ServerBinding) extends Message
  case object Stop extends Message

  def apply(): Behavior[Message] = Behaviors.setup { ctx =>
    implicit val system = ctx.system

    val uri: String = "mongodb://localhost:27017"
    val client: MongoClient = MongoClient(uri)
    val codecRegistry = fromRegistries(
      fromProviders(classOf[Prode], classOf[GroupStage], classOf[CompleteGame], classOf[Statistics], classOf[Group]),
      MongoClient.DEFAULT_CODEC_REGISTRY
    )
    val database = client.getDatabase("prodes").withCodecRegistry(codecRegistry)

    val groupConsumer = new GroupConsumer(database)
    val prodeConsumer = new ProdeConsumer(database, groupConsumer.groupService)
    val sessionConsumer = new SessionConsumer()

    val resultUpdater = ctx.spawn(ResultUpdater(), "ResultUpdater")
    val apiPoller = ctx.spawn(ApiPoller(resultUpdater), "Poller")

    val bindingFuture = Http().newServerAt("localhost", 8080)
      .bind(concat(prodeConsumer.route, groupConsumer.route, sessionConsumer.route))

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return

    ctx.pipeToSelf(bindingFuture) {
      case Success(binding) => Started(binding)
      case Failure(ex) => StartFailed(ex)
    }

    def running(binding: ServerBinding): Behavior[Message] =
      Behaviors.receiveMessagePartial[Message] {
        case Stop =>
          ctx.log.info("Stop server")
          Behaviors.stopped
      }.receiveSignal {
        case (_, PostStop) =>
          binding.unbind()
          Behaviors.same
      }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
          throw new RuntimeException("Failed to start", cause)
        case Started(binding) =>
          ctx.log.info("Server online")
          if (wasStopped) ctx.self ! Stop
          running(binding)
        case Stop =>
          starting(wasStopped = true)
      }

    starting(wasStopped = false)
  }
}
