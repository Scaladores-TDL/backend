import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, pathPrefix, post}
import akka.http.scaladsl.server.PathMatchers.LongNumber
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.result.DeleteResult
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.json.DefaultJsonProtocol.{jsonFormat2}
import akka.http.scaladsl.server.Directives._

import scala.util.{Failure, Success}


class GroupConsumer(val database: MongoDatabase) {
  val groupsCollection: MongoCollection[Group] = database.getCollection("groups")
  val groupService = new GroupService(groupsCollection)

  // formats for unmarshalling and marshalling
  implicit val groupFormat = jsonFormat2(Group)

  val route = cors() {
    concat(
      get {
        pathPrefix("group") {
          val f = groupService.find
          onComplete(f) {
            case Success(groups) => complete(groups)
            case Failure(e) =>  complete(StatusCodes.InternalServerError)
          }
        }
      },
      post {
        pathPrefix("group") {
          entity(as[Group]) {
            prode => {
              val f = groupService.create(prode)
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
      }
    )
  }

}
