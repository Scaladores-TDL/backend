package group

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.LongNumber
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.mongodb.MongoWriteException
import games.{CompleteGame, GroupStage, Statistics}
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import prode.{Prode, ProdeService}
import spray.json.DefaultJsonProtocol._

import scala.util.{Failure, Success}
import jwt.JwtAuthenticator

import java.util.NoSuchElementException

class GroupConsumer(val database: MongoDatabase) {
  val groupsCollection: MongoCollection[Group] = database.getCollection("groups")
  val groupService = new GroupService(groupsCollection)
  val jwtAuthenticator: JwtAuthenticator = JwtAuthenticator()

  // formats for unmarshalling and marshalling
  implicit val createGroupFormat = jsonFormat2(CrateGroupRequest)
  implicit val groupFormat = jsonFormat2(Group)


  val route: Route = cors() {
    pathPrefix("group") {
      Route.seal {
        authenticateOAuth2(realm = "secure route", jwtAuthenticator.authenticate) { token =>
          concat(
            get {
              concat(
                pathEnd {
                  complete(groupService.findGroups())
                },
                path(LongNumber) {
                  groupId => {
                    val f = groupService.findGroupById(groupId)
                    onComplete(f) {
                      case Success(group) => group match {
                        case Some(group) => complete(group)
                        case None => complete(StatusCodes.NotFound)
                      }
                      case Failure(e) => complete(StatusCodes.InternalServerError)
                    }
                  }
                },
              )
            },
            post {
              pathEnd {
                entity(as[CrateGroupRequest]) {
                  newGroup => {
                    val f = groupService.create(newGroup)
                    onComplete(f) {
                      case Success(value) => complete("group created succesfully")
                      case Failure(e) => {
                        e match {
                          case e: MongoWriteException => complete(StatusCodes.BadRequest, "group already exists")
                          case _ => complete(StatusCodes.InternalServerError)
                        }
                      }
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
          )
        }
      }
    }
  }
}
