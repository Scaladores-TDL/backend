final case class CreateProdeRequest(id: Long, user: String, groupId: Long, matches: List[Game]) {
  require(!user.isEmpty, "user name must not be empty")
}

final case class Prode(id: Long, user: String, groupId: Long, matches: List[Game], points: Long, totalHits: Long, totalWrong: Long){
  require(!user.isEmpty)
  require(points >= 0)

  def simulateGame(game: Game): Prode = {
    matches.find(g => g.sameGame(game)) match {
      case Some(g) => {
        println("Game found in prode")
        val statistics = g.calculatePoints(game)
        println(statistics)
        this.copy(
          points = points + statistics._1,
          totalHits = totalHits + statistics._2,
          totalWrong = totalWrong + statistics._3)
      }
      case None => {
        println("game does not exists")
        this
      }
    }
  }

  def compare(other: Prode): Boolean = {
    if (points > other.points) {
      return true
    }

    if (points == other.points) {
      if (totalHits > other.totalHits) {
        return true
      }

      if (totalHits == other.totalHits) {
        if (totalWrong < other.totalWrong){
          return true
        }
      }
    }

    false
  }
}