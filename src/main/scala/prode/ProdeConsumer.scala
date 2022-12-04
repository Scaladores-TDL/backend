package prode

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mongodb.MongoWriteException
import games.{CompleteGame, GroupStage, Statistics}
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}

class ProdeConsumer(val database: MongoDatabase) {

  val prodesCollection: MongoCollection[Prode] = database.getCollection("prodes")
  val prodeService = new ProdeService(prodesCollection)

  // formats for unmarshalling and marshalling
  implicit val groupStageFormat = jsonFormat4(GroupStage)
  implicit val octaveFinalFromat = jsonFormat6(CompleteGame)
  implicit val createprodeFormat = jsonFormat6(CreateProdeRequest)
  implicit val statisticsFormat = jsonFormat3(Statistics)
  implicit val prodeFormat = jsonFormat7(Prode)

  val route = cors() {
    pathPrefix("prode") {
      concat(
        get {
          concat(
            pathEnd {
              val f = prodeService.find()
              onComplete(f) {
                case Success(prodes) => complete(prodes)
                case Failure(e) =>  complete(StatusCodes.InternalServerError)
              }
            },
            path(LongNumber) { prodeId =>
              val f = prodeService.findProdeById(prodeId)
              onComplete(f) {
                case Success(prode) => prode match {
                  case Some(prode) => complete(prode)
                  case None => complete(StatusCodes.NotFound)
                }
                case Failure(e) => complete(StatusCodes.InternalServerError)
              }
            }
          )
        },
        post {
          concat(
            pathEnd {
              entity(as[CreateProdeRequest]) {
                request => {
                  val f = prodeService.create(request)
                  onComplete(f) {
                    case Success(_) => complete("prodeTest created succesfully")
                    case Failure(e) => {
                      println(e)
                      e match {
                        case e: MongoWriteException => complete(StatusCodes.BadRequest, "prodeTest.Prode already exists.")
                        case e => {
                          println(e.getMessage)
                          complete(StatusCodes.InternalServerError)
                        }
                      }
                    }
                  }
                }
              }
            },
            path("init") {
              val f = prodeService.initProdes
              onComplete(f) {
                case Success(_) => complete("Initialization succesfully")
                case Failure(e) => {
                  e match {
                    case e: MongoWriteException => complete(StatusCodes.BadRequest, "prodeTest.Prode already exists.")
                    case _ => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },
            path("simulate_game") {
              entity(as[GroupStage]) {
                game => {
                  val f = prodeService.simulate(game)
                  onComplete(f) {
                    case Success(_) => complete("Simulation completed succesfully")
                    case Failure(e) =>  complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },
            path("simulate_octave_final") {
              entity(as[CompleteGame]) {
                game => {
                  val f = prodeService.simulate(game)
                  onComplete(f) {
                    case Success(_) => complete("Simulation octave final game completed succesfully")
                    case Failure(e) =>  complete(StatusCodes.InternalServerError)
                  }
                }
              }
            },
            path("simulate_final") {
              entity(as[CompleteGame]) {
                game => {
                  val f = prodeService.simulateFinal(game)
                  onComplete(f) {
                    case Success(_) => complete("Simulation octave final game completed succesfully")
                    case Failure(e) => {
                      println(e)
                      complete(StatusCodes.InternalServerError)
                    }
                  }
                }
              }
            }
          )
        },
        delete {
          path("prode" / LongNumber) {
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
      )
    }
  }
}
