package prode

import games.{CompleteGame, Game, GroupStage, Statistics}

case class Prode(_id: Long,
                 user: String,
                 groupId: Long,
                 groupStage: List[GroupStage],
                 matches: List[CompleteGame],
                 finalGame: CompleteGame,
                 statistics: Statistics) {
  require(user.nonEmpty)
  require(groupStage.distinct == groupStage, "duplicate games on stage phase")
  require(matches.distinct == matches, "duplicate games on octaveFinal/quarterFinal/semiFinal")
  require(statistics.points >= 0)
  require(statistics.totalHits >= 0)
  require(statistics.totalWrong >= 0)

  def simulateStageGame(simulation: GroupStage): Prode = {
    groupStage
      .find(g => g.sameGame(simulation))
      .map(g => {
        println("games.Game found in prodeTest")
        val statistics = g.calculatePoints(simulation)
        this.copy(
          statistics = this.statistics + statistics)
      })
      .getOrElse({
        println("game does not exists")
        this
      })
  }

  def simulateMatch(simulation: CompleteGame): Prode = {
    matches.find(g => g.sameGame(simulation)).map(g => {
      println("games.Game found in prode")
      val statistics = g.calculatePoints(simulation)
      this.copy(
        statistics = this.statistics + statistics)
    }).getOrElse({
      println("game does not exists")
      this
    })
  }

  def simulateFinal(simulation: CompleteGame): Prode = {
    val statisticsBase = if (finalGame.sameGame(simulation)) {
      finalGame.calculatePoints(simulation)
    } else {
      Statistics(0,0,0)
    }

    val statistics = statisticsBase + finalGame.calculatePointsFinal(simulation)
    this.copy(
      statistics = this.statistics + statistics)
  }

  def compare(other: Prode): Boolean = {
    statistics > other.statistics
  }
}
