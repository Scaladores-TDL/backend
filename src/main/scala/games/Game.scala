package games

abstract class Game(team1: String, team2: String, result1: Long, result2: Long) {
  require(result1 >= 0, "result must be grather than 0")
  require(result2 >= 0, "result must be grather than 0")

  def teams: (String, String) = (team1, team2)
  def sameGame(other: Game): Boolean = {
    teams == other.teams
  }
  def result: (Long, Long) = (result1, result2)
  def winner: Long = (result1 - result2).sign
  def calculatePoints(other: Game): Statistics = {
    if (result == other.result) {
      println("Result is correct")
      return Statistics(3,1,0)
    }

    //See who win
    //0 empate, 1 gana team2 -1 gana team2
    if (winner == other.winner) {
      println("Team is corect")
      return Statistics(1,0,0)
    }

    println("Resul is wrong")
    Statistics(0,0,1)
  }
}