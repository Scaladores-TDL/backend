package prode

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mongodb.MongoWriteException
import games.{CompleteGame, GroupStage, Statistics}
import group.{CrateGroupRequest, GroupService}
import jwt.JwtAuthenticator
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import spray.json.DefaultJsonProtocol._

import java.util.NoSuchElementException
import scala.util.{Failure, Success}

class ProdeConsumer(private val prodeService: ProdeService, private val groupService: GroupService) {
  val jwtAuthenticator: JwtAuthenticator = JwtAuthenticator()

  // formats for unmarshalling and marshalling
  implicit val groupStageFormat = jsonFormat5(GroupStage)
  implicit val octaveFinalFromat = jsonFormat7(CompleteGame)
  implicit val createprodeFormat = jsonFormat6(CreateProdeRequest)
  implicit val statisticsFormat = jsonFormat3(Statistics)
  implicit val prodeFormat = jsonFormat7(Prode)

  val route: Route = cors() {
    pathPrefix("prode") {
      Route.seal {
        authenticateOAuth2(realm = "secure route", jwtAuthenticator.authenticate) { token =>
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
                },
                path("group" / LongNumber) { groupId =>
                  val f = prodeService.findProdeByGroupId(groupId)
                  onComplete(f) {
                    case Success(prodes) => complete(prodes)
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
                        case Success(_) => complete("prode created succesfully")
                        case Failure(e) => {
                          e match {
                            case e: MongoWriteException => complete(StatusCodes.BadRequest, "prode already exists.")
                            case e: Exception => complete(StatusCodes.BadRequest, e.getMessage)
                            case _ => complete(StatusCodes.InternalServerError)
                          }
                        }
                      }
                    }
                  }
                },
                path("simulate_stage_game") {
                  entity(as[GroupStage]) {
                    game => {
                      val f = prodeService.simulateStageGame(game)
                      onComplete(f) {
                        case Success(_) => complete("Simulation completed succesfully")
                        case Failure(e) =>  complete(StatusCodes.InternalServerError)
                      }
                    }
                  }
                },
                path("simulate_match") {
                  entity(as[CompleteGame]) {
                    game => {
                      val f = prodeService.simulateMatch(game)
                      onComplete(f) {
                        case Success(_) => complete("Simulation match completed succesfully")
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
                        case Success(_) => complete("Simulation final game completed succesfully")
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
              path(LongNumber) {
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
  }
}
object ProdeConsumer {
  def apply(database: MongoDatabase, groupService: GroupService): ProdeConsumer = {
    new ProdeConsumer(database, groupService)
  }
}
