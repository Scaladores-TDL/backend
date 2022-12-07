package jwt;

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{path, _}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.mongodb.MongoWriteException
import games.{CompleteGame, GroupStage, Statistics}
import org.mongodb.scala.result.DeleteResult
import org.mongodb.scala.{MongoCollection, MongoDatabase}
import spray.json.DefaultJsonProtocol._

class SessionConsumer {
  val jwtGenerator = new JwtGenerator()

  implicit val createSessionFormat = jsonFormat1(CreateSessionRequest)

  val route = cors() {
    pathPrefix ("session") {
      concat(
        post {
          concat(
            pathEnd {
              entity(as[CreateSessionRequest]) {
                request => {
                  val token = jwtGenerator.createToken(request.username)
                  complete(token)
                }
              }
            }
          )
        }
      )
    }
  }
}
