package jwt

import akka.http.scaladsl.server.directives.Credentials
import pdi.jwt.{Jwt, JwtAlgorithm}

import scala.util.{Failure, Success}

object JwtAuthenticator {
  def apply(): JwtAuthenticator = {
    new JwtAuthenticator
  }
}

class JwtAuthenticator {
  val SECRET_KEY = "super_duper_secret_key"

  def authenticate(credentials: Credentials): Option[String] = {
    credentials match {
      case Credentials.Provided(token) if Jwt.isValid(token, SECRET_KEY, Seq(JwtAlgorithm.HS256)) => {
        Jwt.decode(token, SECRET_KEY, Seq(JwtAlgorithm.HS256)) match {
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
