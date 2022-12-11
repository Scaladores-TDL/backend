import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.ByteString

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ApiPoller {
  val INTERVAL = 5.seconds

  sealed trait Command
  final private case class Poll() extends Command

  private case object TimerKey

  def apply(updater: ActorRef[ResultUpdater.Message]): Behavior[Command] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ApiPoller")
    Behaviors.withTimers(timers => {
      timers.startTimerAtFixedRate(TimerKey, Poll(), INTERVAL)

      listen(updater)
    })
  }

  def listen(updater: ActorRef[ResultUpdater.Message]): Behavior[Command] = Behaviors.setup { ctx =>
    Behaviors.receiveMessage[Command] {
      case Poll() => {
        poll(updater)
      }
    }
  }

  def poll(updater: ActorRef[ResultUpdater.Message]): Behavior[Command] = Behaviors.setup { ctx =>
    implicit val system = ctx.system
    implicit val executor = ctx.executionContext

    ctx.log.info("Polling...")

    val responseFuture = Http(ctx.system).singleRequest(
      HttpRequest(
        uri = "https://akka.io"
      )
    )

    responseFuture.onComplete {
      case Success(res) => res match {
        case HttpResponse(StatusCodes.OK, headers, entity, _) =>
          val content = Await.result(Unmarshal(entity).to[String], 1.minute)
          updater ! ResultUpdater.MatchResult(content.substring(0, 20))
        case resp@HttpResponse(code, _, _, _) =>
          resp.discardEntityBytes()
          ctx.log.info("Request failed, response code: " + code)
      }
      case Failure(msg) => ctx.log.info("Request failed, error:" + msg.getMessage)
    }

    Behaviors.same
  }
}
