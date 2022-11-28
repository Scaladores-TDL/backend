import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mongodb.MongoWriteException
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.result.DeleteResult

import scala.util.{Failure, Success}

class ProdeConsumer(val database: MongoDatabase) {

  val prodesCollection: MongoCollection[Prode] = database.getCollection("prodes")
  val prodeService = new ProdeService(prodesCollection)

  // formats for unmarshalling and marshalling
  implicit val matchFormat = jsonFormat4(Game)
  implicit val createprodeFormat = jsonFormat4(CreateProdeRequest)
  implicit val prodeFormat = jsonFormat7(Prode)

  val route = cors() {
    concat(
      get {
        pathPrefix("prode") {
          val f = prodeService.find()
          onComplete(f) {
            case Success(prodes) => complete(prodes)
            case Failure(e) =>  complete(StatusCodes.InternalServerError)
          }
        }
      },
      (pathPrefix("prode") & post) {
        entity(as[CreateProdeRequest]) {
          prode => {
            val f = prodeService.create(prode)
            onComplete(f) {
              case Success(value) => complete("prode created succesfully")
              case Failure(e) => {
                e match {
                  case e: MongoWriteException => complete(StatusCodes.BadRequest, "Prode already exists.")
                  case _ => complete(StatusCodes.InternalServerError)
                }
              }
            }
          }
        }
      },
      delete {
        pathPrefix("prode" / LongNumber) {
          prodeId => {
            val f = prodeService.delete(prodeId)
            onComplete(f) {
              case Success(result: DeleteResult) => {
                if (result.getDeletedCount == 0) {
                  complete(StatusCodes.NotFound)
                } else {
                  complete("delete succesfully")
                }
              }
              case Failure(e) =>  complete(StatusCodes.InternalServerError)
            }
          }
        }
      },
      (pathPrefix("prode" / "simulate_game") & post) {
        entity(as[Game]) {
          game => {
            val f = prodeService.simulateGame(game)
            onComplete(f) {
              case Success(_) => complete("Simulation completed succesfully")
              case Failure(e) =>  complete(StatusCodes.InternalServerError)
            }
          }
        }
      },
      (pathPrefix("prode" / "init") & post){
        val f = prodeService.initProdes
        onComplete(f) {
          case Success(_) => complete("Initialization succesfully")
          case Failure(e) => {
            e match {
              case e: MongoWriteException => complete(StatusCodes.BadRequest, "Prode already exists.")
              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        }
      }
    )
  }

}
