import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration._
import tournament.{Credentials, Webhook}

import scala.collection.mutable.ArrayBuffer

object ApiPoller {
  val INTERVAL = 5.seconds
  val matches = collection.mutable.Buffer.empty[ujson.Value]
  val credentials = new Credentials()

  sealed trait Command
  final private case class Poll() extends Command

  private case object TimerKey

  def apply(updater: ActorRef[ResultUpdater.Message]): Behavior[Command] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ApiPoller")
    Behaviors.withTimers(timers => {
      timers.startTimerAtFixedRate(TimerKey, Poll(), INTERVAL)

      Behaviors.receiveMessage[Command] { message =>
        message match {
          case Poll() => {
            val webhook = new Webhook()
            val todayMatches: ArrayBuffer[ujson.Value] = webhook.getTodayMatchesStatuses
            if (todayMatches.nonEmpty) {
              matches.appendAll(todayMatches)
              for (partido <- matches) {
                if (partido("finished").str.equals("TRUE")) {
                  val matchId = partido("id").str.toInt
                  val team1Id = partido("home_team_id").str.toInt
                  val team2Id = partido("away_team_id").str.toInt
                  val team1 = credentials.getTeamNameById(team1Id)
                  val team2 = credentials.getTeamNameById(team2Id)
                  val team1Goals = partido("home_score").num.toInt
                  val team2Goals = partido("away_score").num.toInt
                  var team1Pens = 0
                  var team2Pens = 0
                  val winOnPK = team1Goals == team2Goals
                  if (winOnPK) {
                    val scorers_home = partido("home_scorers").arr.map(_.str)
                    val index_home = scorers_home.map(_ indexOf "Penalties")(0) + "Penalties".length
                    team1Pens = scorers_home(0).substring(index_home).drop(1).dropRight(1).toInt
                    val scorers_away = partido("away_scorers").arr.map(_.str)
                    val index_away = scorers_away.map(_ indexOf "Penalties")(0) + "Penalties".length
                    team2Pens = scorers_away(0).substring(index_away).drop(1).dropRight(1).toInt
                  }
                  val winner = if(winOnPK) "Draw" else {
                    if(team1Goals < team2Goals) team2 else team1
                  }
                  updater ! ResultUpdater.MatchResult(matchId = matchId, team1 = team1, team2 = team2, goals1 = team1Goals, goals2 = team2Goals, pens1 = team1Pens, pens2 = team2Pens, matchWinnerOrDraw = winner, winOnPK = winOnPK)
                }
              }
            }
            Behaviors.same
          }
        }
      }
    })
  }

//  def idle(): Behavior[Command] = {
//    Behaviors.receiveMessage[Command] { message =>
//      Behaviors.same
//    }
//  }
}
