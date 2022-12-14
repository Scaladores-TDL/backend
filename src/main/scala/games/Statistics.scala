package games

case class Statistics(var points: Long, var totalHits: Long, var totalWrong: Long) {

  def +(other: Statistics): Statistics = {
    Statistics(points + other.points, totalHits + other.totalHits, totalWrong + other.totalWrong)
  }

  def >(other: Statistics): Boolean = {
    if (points > other.points) {
      return true
    }

    if (points == other.points) {
      if (totalHits > other.totalHits) {
        return true
      }

      if (totalHits == other.totalHits) {
        if (totalWrong < other.totalWrong){
          return true
        }
      }
    }
    false
  }
}
