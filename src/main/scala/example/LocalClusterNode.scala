package example

import akka.actor.ActorSystem

/**
 * Node running in a locally hosted cluster.
 * @param port Port number this node is bound to. Used to discriminate this node in the local cluster.
 * @param system Actor system.
 */
case class LocalClusterNode(port: Int, system: ActorSystem)
