package worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import prode.ProdeService

object ResultUpdater {
  sealed trait Message;
  final case class MatchResult(result: ApiTypes.ApiMatch) extends Message

  def apply(prodeService: ProdeService): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ResultUpdater")
    Behaviors.receiveMessage[Message] { msg =>
      msg match {
        case MatchResult(result: ApiTypes.ApiMatch) => {
          ctx.log.info("Received message!")
          ctx.log.info(s"${result.homeTeam} (${result.homeScore}) vs ${result.awayTeam} (${result.awayScore})")
//          prodeService.simulateStageGame()
          Behaviors.same
        }
      }
    }
  }
}
