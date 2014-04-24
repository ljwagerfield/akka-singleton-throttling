package example.throttling

import akka.actor.{Props, ActorSystem, Stash, ActorLogging, Actor, ActorRef}

/**
 * Performs in-process throttling.
 * @param singletonThrottle Singleton throttle proxy reference.
 */
class LocalThrottleActor(singletonThrottle: ActorRef) extends Actor with ActorLogging with Stash {

  /**
   * Initial behaviour.
   */
  override def receive: Receive = {
    case message: ThrottleAction =>
      singletonThrottle.tell(
        Enqueue(),
        LocalQueuedActionActor(message, context))
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
