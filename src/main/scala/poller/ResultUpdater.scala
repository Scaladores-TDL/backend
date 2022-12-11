import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import java.io.{BufferedWriter, File, FileWriter}


object ResultUpdater {
  sealed trait Message;
  final case class MatchResult(matchId: Int, team1: String, team2: String, goals1: Int, goals2: Int, pens1: Int, pens2: Int, matchWinnerOrDraw: String, winOnPK: Boolean) extends Message {
    def getTeam1Name: String = team1
    def getTeam2Name: String = team2
    def getTeam1Goals: Int = goals1
    def getTeam2Goals: Int = goals2
    def getTeam1Pens: Int = pens1
    def getTeam2Pens: Int = pens2
    def getMatchWinnerOrDraw: String = matchWinnerOrDraw
  }

  def apply(): Behavior[Message] = Behaviors.setup { ctx =>
    ctx.log.info("Set up ResultUpdater")
    Behaviors.receiveMessage[Message] { msg =>
      msg match {
        case MatchResult(matchId: Int, team1: String, team2: String, goals1: Int, goals2: Int, pens1: Int, pens2: Int, matchWinnerOrDraw: String, winOnPK: Boolean) => {
          val line = matchId + ',' + team1 + ',' + team2 + ',' + goals1 + ',' + goals2  + ',' + matchWinnerOrDraw + ',' + winOnPK + ',' + pens1  + ',' + pens2
          val resultsFile = new File("../../resources/results.txt")
          val bufferedWriter = new BufferedWriter(new FileWriter(resultsFile))
          bufferedWriter.write(line)
          bufferedWriter.close()
          ctx.log.info("Received message!")
        }
      }
      Behaviors.same
    }
  }
}
