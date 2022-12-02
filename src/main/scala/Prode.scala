import games.{Game, GroupStage, CompleteGame, Statistics}

case class Prode(_id: Long, user: String, groupId: Long, matches: List[GroupStage], octaveFinal: List[CompleteGame], finalGame: CompleteGame, statistics: Statistics){
  require(user.nonEmpty)
  require(statistics.points >= 0)
  require(statistics.totalHits >= 0)
  require(statistics.totalWrong >= 0)

  def simulate(game: Game): Prode = {
    game match {
      case g: GroupStage => {
        println("games.GroupStage")
        simulateStageGame(g)
      }
      case g: CompleteGame => {
        println("games.OctaveFinal")
        simulateOctaveFinal(g)
      }
      case _ => {
        println("cannot simulate")
        this
      }
    }
  }

  def simulateStageGame(simulation: GroupStage) = {
    matches
      .find(g => g.sameGame(simulation))
      .map(g => {
        println("games.Game found in prode")
        val statistics = g.calculatePoints(simulation)
        this.copy(
          statistics = this.statistics + statistics)
      })
      .getOrElse({
        println("game does not exists")
        this
      })
  }

  def simulateOctaveFinal(simulation: CompleteGame) = {
    octaveFinal.find(g => g.sameGame(simulation)).map(g => {
      println("games.Game found in prode")
      val statistics = g.calculatePoints(simulation)
      println(statistics)
      this.copy(
        statistics = this.statistics + statistics)
    }).getOrElse({
      println("game does not exists")
      this
    })
  }

  def simulateFinal(simulation: CompleteGame) = {
    val statisticsBase = finalGame.calculatePoints(simulation)
    val statistics = statisticsBase + finalGame.calculatePointsFinal(simulation)
    this.copy(
      statistics = this.statistics + statistics)
  }

  def compare(other: Prode): Boolean = {
    statistics > other.statistics
  }
}