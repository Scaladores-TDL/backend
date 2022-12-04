package group

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import games.{CompleteGame, GroupStage, Statistics}
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import prode.{Prode, ProdeService}
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}
import jwt.JwtAuthenticator

import java.util.NoSuchElementException

class GroupConsumer(val database: MongoDatabase, prodeService: ProdeService) {
  val groupsCollection: MongoCollection[Group] = database.getCollection("groups")
  val groupService = new GroupService(groupsCollection, prodeService)
  val jwtAuthenticator = new JwtAuthenticator

  // formats for unmarshalling and marshalling
  implicit val createGroupFormat = jsonFormat2(CrateGroupRequest)
  implicit val groupStageFormat = jsonFormat4(GroupStage)
  implicit val octaveFinalFromat = jsonFormat6(CompleteGame)
  implicit val statisticsFormat = jsonFormat3(Statistics)
  implicit val prodeFormat = jsonFormat7(Prode)
  implicit val groupFormat = jsonFormat3(Group)


  val route = cors() {
    pathPrefix("group") {
      Route.seal {
        authenticateOAuth2(realm = "secure route", jwtAuthenticator.authenticate) { token =>
          concat(
            post {
              pathEnd {
                entity(as[CrateGroupRequest]) {
                  newGroup => {
                    val f = groupService.create(newGroup)
                    onComplete(f) {
                      case Success(value) => complete("group created succesfully")
                      case Failure(e) =>  complete(StatusCodes.InternalServerError)
                    }
                  }
                }
              }
            },
            delete {
              path(LongNumber) {
                groupId => {
                  val f = groupService.delete(groupId)
                  onComplete(f) {
                    case Success(result: DeleteResult) => {
                      println(result)
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
            get {
              concat(
                pathEnd {
                  complete(groupService.getGroups)
                },
                path(LongNumber) {
                  groupId => {
                    val f = groupService.getGroupById(groupId)
                    onComplete(f) {
                      case Success(group) => complete(group)
                      case Failure(e) => e match {
                        case e: NoSuchElementException => complete(StatusCodes.NotFound)
                        case _ => complete(StatusCodes.InternalServerError)
                      }
                    }
                  }
                },
              )
            }
          )
        }
      }
    }
  }
}
