package ololo

import akka.actor.Actor

/**
 * Created by ko3a4ok on 10/26/15.
 */
class LoginActor(user: User) extends Actor {
  override def receive: Receive = {
    case u: User => {
      sender() ! user.equals(u)
    }
  }
}
