package tournament

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import webhooks.Hook.NoBody
import webhooks.HookSpec._
import webhooks.models.HookConfig

import scala.concurrent.duration._

import Credentials.*
import spray.json._
import DefaultJsonProtocol._
import ujson._

class Webhook{
  implicit val TOKEN: String = Credentials.getToken()
  implicit val MATCHES_URI: String = Credentials.getMatchesURI()
  implicit val STANDINGS_URI: String = Credentials.getStandingsURI()
  implicit val TEAMS_URI: String = Credentials.getTeamsURI()
  implicit val MATCHES_BY_DATE_URI: String = Credentials.getMatchesByDateURI()

  def poll(){
    var tournamentIsFinished: Boolean = false
    val endDay: Int = 19
    val endMonth: Int = 12
    val endYear: Int = 2022
    do{
      var month: Int = LocalDate.now()getMonthValue()
      var day: Int = LocalDate.now().getDayOfMonth()
      var year: Int = LocalDate.now().getYear()
      var todayDate: String = month.toString + "/" + day.toString + "/" + year.toString
      var postData: String = "{\"date\": \"" + todayDate + "\"}"
      val response = getTodayMatchesStatuses(postData)
      val responseJson = ujson.read(response)
      val matchList = responseJson("data").value
      tournamentIsFinished = (endDay <= day) && (endMonth <= month) && (endYear <= year)
      Thread.sleep(60000)
    } while(!tournamentIsFinished)
  }

  def getTodayMatchesStatuses(postData: String): String = {
    val responseFuture: Future[HttpResponse] = Http().singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        endpoint = this.MATCHES_BY_DATE_URI,
        entity = HttpEntity(ContentTypes.`application/json`, postData)
      ).withHeaders(
        Authorization(OAuth2BearerToken(TOKEN))
      )
    )
    val responseAsString = Await.result(
      responseFuture
        .flatMap { resp => resp.entity.toStrict(timeout) }
        .map { strictEntity => strictEntity.data.utf8String },
      timeout = 1000.millis
    )
    responseAsString
  }
}