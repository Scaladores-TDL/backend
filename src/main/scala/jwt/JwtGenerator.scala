package jwt;

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

import java.time.Clock
import java.time.Duration

class JwtGenerator {
    val SECRET_KEY = "super_duper_secret_key"

    implicit val clock: Clock = Clock.systemUTC

    def createToken(username: String): String = {
        Jwt.encode(
            JwtClaim(s"""{"user": $username}""").issuedNow.expiresIn(Duration.ofHours(1).toSeconds),
            SECRET_KEY,
            JwtAlgorithm.HS256
        )
    }
}
