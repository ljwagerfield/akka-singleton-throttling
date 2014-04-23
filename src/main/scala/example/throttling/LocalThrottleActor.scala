package example.throttling

import akka.actor._
import forks.akka.contrib.throttle.Throttler.QueueFull

/**
 * Performs in-process throttling.
 * @param singletonThrottle Singleton throttle proxy reference.
 */
class LocalThrottleActor(singletonThrottle: ActorRef) extends Actor with ActorLogging with Stash {

  override def receive: Receive = {
    case message: ThrottleAction =>
      val pendingActor = context.actorOf(Props(classOf[LocalQueuedActionActor], message))
      singletonThrottle.tell(Enqueue(), pendingActor)
  }
}

/**
 * Local throttle actor factory.
 */
object LocalThrottleActor {

  /**
   * Creates an actor for throttling locally-scoped actions.
   * @param singletonThrottle Proxy to singleton throttle.
   * @return Local throttle actor reference.
   */
  def apply(singletonThrottle: ActorRef, system: ActorSystem): ActorRef =
    system.actorOf(Props(new LocalThrottleActor(singletonThrottle)), name = "local-throttle")
}

class LocalQueuedActionActor(message: ThrottleAction) extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case Dequeue() =>
      message.action()
      context stop self

    case QueueFull() =>
      message.queueFull()
      context stop self
  }
}
