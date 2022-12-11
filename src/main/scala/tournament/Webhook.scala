package tournament

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.duration._
import spray.json._
import DefaultJsonProtocol._

import java.time.LocalDate
import scala.collection.mutable.ArrayBuffer

class Webhook{
  val credentials = new Credentials()

  def getTodayMatchesStatuses: ArrayBuffer[ujson.Value] = {
    val month: Int = LocalDate.now().getMonthValue
    val day: Int = LocalDate.now().getDayOfMonth
    val year: Int = LocalDate.now().getYear
    val todayDate: String = month.toString + "/" + day.toString + "/" + year.toString
    val todayMatchesReq = requests.post(credentials.getMatchesByDateURI,
      data = ujson.Obj("date" -> todayDate).render(),
      headers = Map(
        "Content-Type" -> "application/json",
        "Authorization" -> ("Bearer " + credentials.getToken)
      )
    )
    val todayMatches = ujson.read(todayMatchesReq)("data").arr
    todayMatches
  }
}