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

Standard code
-------------

This project relies entirely on standard frameworks and libraries that are either authored or advocated by [Typesafe][typesafe].

The remainder of the codebase simply parametrises these frameworks into a working example.

Non-standard code
-----------------

The only exception to the above is a slight additive change made to `TimerBasedThrottler` from the [`akka-contrib`][akka-contrib]
library.

[This addition introduces bounded queueing.][throttler-change-log]

[akka-cluster-theory]: http://doc.akka.io/docs/akka/snapshot/common/cluster.html#cluster "Akka Clustering Theory"
[akka-contrib]: https://github.com/akka/akka/tree/master/akka-contrib "Akka Contrib"
[typesafe]: https://typesafe.com/ "Typesafe"
[throttler-change-log]: https://github.com/ljwagerfield/akka-singleton-throttling/commits/master/src/main/scala/forks/akka/contrib/throttle/TimerBasedThrottler.scala "Throttler change log"
