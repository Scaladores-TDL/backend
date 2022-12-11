package worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import prode.ProdeService

object ResultUpdater {
  sealed trait Message
  final case class MatchResult(result: ApiTypes.ApiMatch) extends Message

  def apply(prodeService: ProdeService): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ResultUpdater")
    Behaviors.receiveMessage[Message] {
      case MatchResult(result: ApiTypes.ApiMatch) if result.state == "finished" =>
        result.matchType match {
          case "group" => prodeService.simulateStageGame(mapToStageGame(result))
          case "R16" | "QR" | "semi" => prodeService.simulateMatch(mapToCompleteGame(result))
          case "final" => prodeService.simulateFinal(mapToCompleteGame(result))
        }
        Behaviors.same
      case _ => Behaviors.same
    }
  }

  def mapToStageGame(result: ApiTypes.ApiMatch): games.GroupStage = {
    games.GroupStage(
      team1 = result.homeTeam, team2 = result.awayTeam, result1 = result.homeScore, result2 = result.awayScore,
      finished = false
    )
  }

  def mapToCompleteGame(result: ApiTypes.ApiMatch): games.CompleteGame = {
    var penalties1: Int = 0
    var penalties2: Int = 0
    if (result.homeScorers.nonEmpty && result.awayScorers.nonEmpty && result.homeScore == result.awayScore) {
      val homeScorersIndex = (result.homeScorers.head indexOf "Penalties") + "Penalties".length
      val awayScorersIndex = (result.awayScorers.head indexOf "Penalties") + "Penalties".length
      penalties1 = result.homeScorers.head.substring(homeScorersIndex).drop(1).dropRight(1).toInt
      penalties2 = result.awayScorers.head.substring(awayScorersIndex).drop(1).dropRight(1).toInt
    }
    games.CompleteGame(
      team1 = result.homeTeam, team2 = result.awayTeam, result1 = result.homeScore, result2 = result.awayScore,
      penalties1 = penalties1, penalties2 = penalties2, finished = false
    )
  }
}
