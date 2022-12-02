package games

case class CompleteGame(team1: String, team2: String, result1: Long, result2: Long, penalties1: Long, penalties2: Long) extends Game(team1, team2, result1, result2) {
  require(if (result1 == result2) { penalties1 != penalties2 } else true )

  override def winner: Long = {
    super.winner match {
      case 0 => (penalties1 - penalties2).sign
      case winner => winner
    }
  }
  def tie: Boolean = result1 == result2
  def penaltiesWinner: Long = (penalties1 - penalties2).sign
  def penalties: (Long, Long) = (penalties1, penalties2)

  override def calculatePoints(other: Game): Statistics = {
    other match {
      case other: CompleteGame => {
        val statisticsBase = super.calculatePoints(other)

        if (tie && other.tie) {
          //Calculate point for penalties
          if (penalties == other.penalties) {
            return Statistics(4,0,0)
          }
          if (winner == other.winner) {
            return Statistics(1,0,0)
          }
        }

        statisticsBase
      }
      case _ => {
        println("Cannot compare")
        Statistics(0,0,0)
      }
    }
  }

  def calculatePointsFinal(other: Game): Statistics = {
    if (winner == other.winner) {
      println("final winner is correct")
      return Statistics(10, 1, 0)
    }
    Statistics(0,0,0)
  }
}
