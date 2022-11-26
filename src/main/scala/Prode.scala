final case class CreateProdeRequest(id: Long, user: String, groupId: Long, matches: List[Game]) {
  require(!user.isEmpty, "user name must not be empty")
}

final case class Prode(id: Long, user: String, groupId: Long, matches: List[Game], points: Long){
  require(!user.isEmpty)
  require(points >= 0)

  def simulateGame(game: Game): Prode = {
    matches.find(g => g.sameGame(game)) match {
      case Some(g) => {
        println("Game found in prode")
        this.copy(points = points + g.calculatePoints(game))
      }
      case None => {
        println("game does not exists")
        this
      }
    }
  }
}