final case class Game(team1: String, team2: String, result1: Long, result2: Long) {
  require(result1 >= 0, "result must be grather than 0")
  require(result2 >= 0, "result must be grather than 0")

  def sameGame(other: Game): Boolean = {
    (team1, team2) == (other.team1, other.team2)
  }

  def result: (Long, Long) = (result1, result2)
  def winner: Long = (result1 - result2).sign

  def calculatePoints(other: Game): (Long, Long, Long) = {
    println("compare " + this + " with " + other)

    if (result == other.result) {
      println("Result is correct")
      return (3, 1, 0)
    }

    //See who win
    //0 empate, 1 gana team2 -1 gana team2
    if (winner == other.winner) {
      println("Team is corect")
      return (1, 1, 0)
    }

    println("Resul is wrong")
    (0, 0 ,1)
  }
}
