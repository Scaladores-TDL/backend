package prode

import games.{CompleteGame, GroupStage}

final case class CreateProdeRequest(_id: Long, user: String, groupId: Long, groupStage: List[GroupStage], matches: List[CompleteGame], finalGame: CompleteGame) {
  require(user.nonEmpty, "user name must not be empty")
}
