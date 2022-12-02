import games.{Game, GroupStage, CompleteGame}

final case class CreateProdeRequest(_id: Long, user: String, groupId: Long, matches: List[GroupStage], octaveFinal: List[CompleteGame], finalGame: CompleteGame) {
  require(!user.isEmpty, "user name must not be empty")
}
