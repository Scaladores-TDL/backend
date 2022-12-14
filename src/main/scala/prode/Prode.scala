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
        if (g.finished) {
          println(s"Game ${g.title} already finished")
          return this
        }

        println(s"Simulate game ${g.title}")

        val statistics = g.calculatePoints(simulation)
        this.copy(
          groupStage = this.groupStage.map(game => if (game.sameGame(g)) game.copy(finished = true) else game),
          statistics = this.statistics + statistics)
      })
      .getOrElse({
        println(s"Game ${simulation.title} does not exist")
        this
      })
  }

  def simulateMatch(simulation: CompleteGame): Prode = {
    matches.find(g => g.sameGame(simulation)).map(g => {
      if (g.finished) {
        println(s"Game ${g.title} already finished")
        return this
      }

      println(s"Simulate game ${g.title}")

      val statistics = g.calculatePoints(simulation)
      this.copy(
        matches = this.matches.map(game => if (game.sameGame(g)) game.copy(finished = true) else game),
        statistics = this.statistics + statistics)
    }).getOrElse({
      println(s"Game ${simulation.title} does not exist")
      this
    })
  }

  def simulateFinal(simulation: CompleteGame): Prode = {
    if (this.finalGame.finished) {
      println(s"Game ${simulation.title} already finished")
      return this
    }

    println(s"Simulate game ${simulation.title}")

    val statisticsBase = if (finalGame.sameGame(simulation)) {
      finalGame.calculatePoints(simulation)
    } else {
      Statistics(0,0,0)
    }

    val statistics = statisticsBase + finalGame.calculatePointsFinal(simulation)
    this.copy(
      finalGame = this.finalGame.copy(finished = true),
      statistics = this.statistics + statistics)
  }

  def compare(other: Prode): Boolean = {
    statistics > other.statistics
  }
}
