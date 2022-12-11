import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

object Main {
  def main(args: Array[String]): Unit = {
    val system: ActorSystem[AppServer.Message] = ActorSystem(AppServer(), "server-system")
  }
}
