package worker

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.javadsl.model.headers.RawHeader
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import spray.json.DefaultJsonProtocol._

import java.time.LocalDate
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object ApiPoller {
  val INTERVAL = 30.seconds

  sealed trait Command
  final private case class Poll() extends Command

  implicit val apiMatchFormat = jsonFormat(ApiTypes.ApiMatch, "home_team_en", "away_team_en",
    "home_score", "away_score", "home_scorers", "away_scorers", "type", "time_elapsed")
  implicit val apiResponseFormat = jsonFormat2(ApiTypes.ApiResponse)

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

    val date = LocalDate.now

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = "http://api.cup2022.ir/api/v1/bydate",
//      entity = HttpEntity(ContentTypes.`application/json`, s"{\"date\": \"${date.getMonthValue}/${date.getDayOfMonth}/${date.getYear}\"}"),
      entity = HttpEntity(ContentTypes.`application/json`, s"{\"date\": \"12/10/2022\"}"),
      headers = List(RawHeader.create("Authorization", s"Bearer ${sys.env("API_TOKEN")}"))
    )

    val responseFuture = Http(ctx.system).singleRequest(request)

    responseFuture.onComplete {
      case Success(res) => res match {
        case HttpResponse(StatusCodes.OK, headers, entity, _) =>
          val content = Await.result(Unmarshal(entity).to[ApiTypes.ApiResponse], 1.minute)
          content.data.foreach { m => updater ! ResultUpdater.MatchResult(m) }
        case resp@HttpResponse(code, _, _, _) =>
          resp.discardEntityBytes()
          println("Request failed, response code: " + code)
      }
      case Failure(msg) => ctx.log.info("Request failed, error:" + msg.getMessage)
    }

    Behaviors.same
  }
}
