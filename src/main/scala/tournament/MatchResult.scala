package tournament

case class MatchResult(matchId: Int, team1: String, team2: String, goals1: Int, goals2: Int, pens1: Int, pens2: Int, matchWinnerOrDraw: String){
  def getTeam1Name: String = team1
  def getTeam2Name: String = team2

  def getTeam1Goals: Int = goals1
  def getTeam2Goals: Int = goals2
  def getTeam1Pens: Int = pens1
  def getTeam2Pens: Int = pens2

  def getMatchWinnerOrDraw: String = matchWinnerOrDraw
}