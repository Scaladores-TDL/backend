import akka.http.scaladsl.server.directives.Credentials
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.util.{Failure, Success}

class JwtAuthenticator {

  def authenticate(credentials: Credentials): Option[String] = {
    credentials match {
      case Credentials.Provided(token) if Jwt.isValid(token,"secretKey", Seq(JwtAlgorithm.HS256)) => {
        Jwt.decode(token, "secretKey", Seq(JwtAlgorithm.HS256)) match {
          case Success(data) => {
            println(data.content)
          }
          case Failure(e) => println(e)
        }
        Some(token)
      }
      case _ => None
    }
  }
}
