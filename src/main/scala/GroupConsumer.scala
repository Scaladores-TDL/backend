import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, pathPrefix, post}
import akka.http.scaladsl.server.PathMatchers.LongNumber
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.result.DeleteResult
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.DefaultJsonProtocol.jsonFormat2
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.unmarshalling.{Unmarshal}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import scala.util.{Failure, Success}


class GroupConsumer(val database: MongoDatabase) {
  val groupsCollection: MongoCollection[Group] = database.getCollection("groups")
  val prodesCollection: MongoCollection[Prode] = database.getCollection("prodes")
  val groupService = new GroupService(groupsCollection, prodesCollection)
  val jwtAuthenticator = new JwtAuthenticator

  // formats for unmarshalling and marshalling
  implicit val createGroupFormat = jsonFormat2(CrateGroupRequest)
  implicit val groupFormat = jsonFormat3(Group)
  implicit val matchFormat = jsonFormat4(Game)
  implicit val prodeFormat = jsonFormat5(Prode)

  case class User(name: String)

  val route = cors() {
    Route.seal {
      authenticateOAuth2(realm = "secure route", jwtAuthenticator.authenticate) { token =>
        concat(
          post {
            pathPrefix("group") {
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
            pathPrefix("group" / LongNumber) {
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
          (pathPrefix("group" / LongNumber) & get) {
            groupId => {
              complete(groupService.getGroupById(groupId))
            }
          },
          (pathPrefix("group") & get) {
            complete(groupService.getGroups)
          },
        )
      }
    }
  }
}
