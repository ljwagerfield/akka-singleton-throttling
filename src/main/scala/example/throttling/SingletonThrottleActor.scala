package example.throttling

import akka.actor._
import akka.contrib.pattern.{ClusterSingletonProxy, ClusterSingletonManager}
import forks.akka.contrib.throttle.Throttler.QueueFull
import forks.akka.contrib.throttle.Throttler.SetTarget
import scala.Some

/**
 * Encapsulates a timer-based throttle into a self-contained queue.
 * @param timerBasedThrottle Underlying throttle.
 */
class SingletonThrottleActor(timerBasedThrottle: ActorRef) extends Actor with ActorLogging {

  /**
   * Called when an Actor is started.
   */
  override def preStart(): Unit =
    timerBasedThrottle ! SetTarget(Some(self))

  /**
   * Initial behaviour.
   */
  override def receive: Receive = {
    case Enqueue() =>
      timerBasedThrottle forward Dequeue()

    case Dequeue() =>
      sender ! Dequeue()

    case QueueFull() =>
      sender ! QueueFull()
  }
}

/**
 * Singleton throttle actor factory.
 */
object SingletonThrottleActor {

  /**
   * Creates a singleton throttle actor _manager_ and returns a proxy for accessing it (potentially via remoting).
   * @param timerBasedThrottler Throttling mechanism to be used by the singleton throttle.
   * @return Proxy to singleton throttle.
   */
  def apply(timerBasedThrottler: ActorRef, system: ActorSystem): ActorRef = {

    // Wrap singleton throttle with cluster manager to ensure 0-1 instances of the actor exist across the cluster.
    system.actorOf(ClusterSingletonManager.props(
      singletonProps = Props(classOf[SingletonThrottleActor], timerBasedThrottler),
      singletonName = "throttled",
      terminationMessage = PoisonPill,
      role = None),
      name = "singleton")

    // Return a proxy reference capable of forwarding calls to the (potentially remote) singleton actor.
    system.actorOf(ClusterSingletonProxy.props(
      singletonPath = "/user/singleton/throttled",
      role = None),
      name = "singleton-proxy")
  }
}
