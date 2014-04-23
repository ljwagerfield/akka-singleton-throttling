Akka Singleton Throttling
=========================

>   Throttles an action performed by a P2P system through a singleton actor.

This project demonstrates how to use [Akka Clustering][akka-cluster-theory] and [Akka Contrib][akka-contrib] to throttle
a specific action performed by a distributed system.

This is useful in the context of limiting requests to a legacy or paid service, without introducing a:

-   Single point of failure; cluster contains multiple nodes.

-   Maintenance overhead of active/passive roles; all nodes run the same code and configuration.

-   Dormant fail-over infrastructure; singleton is activated via Akka meaning the same node can be used to perform other
    operations.

[akka-cluster-theory]: http://doc.akka.io/docs/akka/snapshot/common/cluster.html#cluster "Akka Clustering Theory"
[akka-contrib]: https://github.com/akka/akka/tree/master/akka-contrib "Akka Contrib"

