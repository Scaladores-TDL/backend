package jwt

import games.{CompleteGame, GroupStage}

final case class CreateSessionRequest(username: String) {
  require(username.nonEmpty, "user name must not be empty")
}
