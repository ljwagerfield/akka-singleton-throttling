package example.throttling

import forks.akka.contrib.throttle.Throttler.QueueFull
import akka.actor.{Props, ActorRef, ActorRefFactory, ActorLogging, Actor}

/**
 * Awaits the response of a queue before performing the composed action and terminating.
 * @param state Contains queued actions to be performed.
 */
class LocalQueuedActionActor(state: ThrottleAction) extends Actor with ActorLogging {

  /**
   * Initial behaviour.
   */
  override def receive: Actor.Receive = {
    case Dequeue() =>
      state.action()
      context stop self

    case QueueFull() =>
      state.queueFull()
      context stop self
  }
}

/**
 * Local queued action actor factory.
 */
object LocalQueuedActionActor {

  /**
   * Creates an actor pending a queue response.
   * @param state Actions to perform on queue response.
   * @param factory Context to create actor under.
   * @return Actor reference.
   */
  def apply(state: ThrottleAction, factory: ActorRefFactory): ActorRef =
    factory.actorOf(Props(classOf[LocalQueuedActionActor], state))
}
