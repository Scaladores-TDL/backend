final case class Game(team1: String, team2: String, result: Long) {
  require(result >= 0 && result <= 2, "result must be 0, 1 or 2")
}

final case class CreateProdeRequest(id: Long, user: String, groupId: Long, matches: List[Game]) {
  require(!user.isEmpty, "user name must not be empty")
}

final case class Prode(id: Long, user: String, groupId: Long, matches: List[Game], points: Long){
  require(!user.isEmpty)
  require(points >= 0)

  def simulateGame(game: Game): Prode = {
    matches.find(g => g == game) match {
      case Some(_) => {
        println("Result is correct")
        this.copy(points = points + 1)
      }
      case None => {
        println("game does not exists or the result is incorrect")
        this
      }
    }
  }
}