import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import prode.ProdeService

object ResultUpdater {
  sealed trait Message;
  final case class MatchResult(content: String) extends Message

  def apply(prodeService: ProdeService): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ResultUpdater")
    Behaviors.receiveMessage[Message] { msg =>
      msg match {
        case MatchResult(content: String) => {
          ctx.log.info("Received message!")
          ctx.log.info(s"Content ${content.toString}")
          prodeService.simulateStageGame()
          Behaviors.same
        }
      }
    }
  }
}
