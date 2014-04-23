package example

import akka.actor._
import akka.cluster.Cluster
import com.typesafe.config.ConfigFactory

/**
 * Demonstrates how to perform distributed throttling using the akka-clustering and akka-contrib libraries.
 */
object Application {

  /**
   * Actor system name used by all members in the cluster.
   */
  final val UnifiedName = "throttle"

  /**
   * Hostname used by all members in the cluster.
   */
  final val UnifiedHostname = "127.0.0.1"

  /**
   * Protocol used by all members in the cluster.
   */
  final val UnifiedProtocol = "akka.tcp"

  /**
   * Application entry point.
   * @param args Arguments. Expects none.
   */
  def main(args: Array[String]): Unit =
    createCluster(startPort = 8001, size = 10)
      .foreach(runMemberOnUp)

  /**
   * Creates an Akka cluster of nodes hosted locally across a contiguous range of ports.
   * @param startPort Port for first node in the cluster.
   * @param size Number of nodes to create.
   * @return Cluster nodes.
   */
  private def createCluster(startPort: Int, size: Int): Seq[LocalClusterNode] = {
    assert(size > 0)
    
    // Create actor systems (one for each node).
    val endPort = (startPort + size) - 1
    val nodes = (startPort to endPort).map(port => LocalClusterNode(port, createActorSystem(port)))

    // Establish seed nodes.
    val seedCount = Math.min(3, size)
    val seeds = nodes.take(seedCount).map(node => Address(UnifiedProtocol, UnifiedName, UnifiedHostname, node.port))
    
    // Form cluster by joining all nodes via the seed nodes.
    nodes.foreach(node => Cluster(node.system).joinSeedNodes(seeds))
    nodes
  }

  /**
   * Creates an actor system listening on the specified port.
   * @param port Port to listen on.
   * @return Actor system.
   */
  private def createActorSystem(port: Int): ActorSystem =
    ActorSystem(
      UnifiedName,
      ConfigFactory
        .parseString("akka.remote.netty.tcp.port=" + port)
        .withFallback(ConfigFactory.load))

  /**
   * Creates and runs a member instance once the specified node transitions into 'up'.
   * @param node Cluster node.
   */
  private def runMemberOnUp(node: LocalClusterNode): Unit =
    Cluster(node.system)
      .registerOnMemberUp(new LocalClusterMember(node).run())
}
