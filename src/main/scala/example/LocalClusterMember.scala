package example

import akka.actor.ActorRef
import example.throttling.{ThrottleAction, LocalThrottleActor, SingletonThrottleActor}
import rx.lang.scala.Observable
import scala.concurrent.duration.DurationInt
import forks.akka.contrib.throttle.Throttler.{Rate, RateInt}
import org.slf4j.{LoggerFactory, Logger}
import forks.akka.contrib.throttle.TimerBasedThrottlerActor

/**
 * Node which has joined the locally hosted cluster. 'joined' is defined as having observed the 'joining -> up' transition.
 * @param node Underlying node.
 */
class LocalClusterMember(val node: LocalClusterNode) {

  import node._

  /**
   * Logger instance.
   */
  final lazy val Log: Logger = LoggerFactory.getLogger(classOf[LocalClusterMember])

  /**
   * Duration between simulated requests for a single cluster member.
   */
  final val MemberRequestInterval = 1.second

  /**
   * Rate at which the singleton throttle passes messages.
   */
  final val ThrottledRate = 3 msgsPer 1.second

  /**
   * Maximum backlog for the singleton throttle.
   */
  final val QueueSize = 15

  /**
   * Simple stateful example.
   */
  var requestCounter = 0

  /**
   * Member node entry point. Called once the node has successfully joined the cluster.
   */
  def run(): Unit =
    simulateRequests(throttlingEcosystem(ThrottledRate, QueueSize))

  /**
   * Begins simulating inbound requests against this member.
   * @param throttle Local throttle actor reference.
   */
  private def simulateRequests(throttle: ActorRef): Unit =
    Observable
      .interval(MemberRequestInterval)
      .subscribe(_ => onSimulatedRequest(throttle))

  /**
   * Simulates a request against this member.
   * @param throttle Local throttle actor reference.
   */
  private def onSimulatedRequest(throttle: ActorRef): Unit = {

    val requestIndex = requestCounter
    requestCounter = requestCounter + 1

    val onSuccess = () => Log.info(s"Node $port -> #$requestIndex")
    val onQueueFull = () => Log.info(s"Node $port !! #$requestIndex")

    throttle ! ThrottleAction(onSuccess, onQueueFull)
  }

  /**
   * Creates all the actors necessary for distributed throttling.
   * @param rate Rate to limit requests at.
   * @param queueSize Maximum number of outstanding requests to queue before immediately failing.
   * @return Reference to local throttle actor.
   */
  private def throttlingEcosystem(rate: Rate, queueSize: Int): ActorRef = {
    val timerBasedThrottler = TimerBasedThrottlerActor(rate, Some(queueSize), system)
    val singletonThrottle = SingletonThrottleActor(timerBasedThrottler, system)
    LocalThrottleActor(singletonThrottle, system)
  }
}
