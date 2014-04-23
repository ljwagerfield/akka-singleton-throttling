package example.throttling

/**
 * Throttles execution of the provided [[action]]. If the underlying queue is full, then [[queueFull]] will be invoked instead.
 * @param action Action to invoke within throttle restrictions.
 * @param queueFull Action to invoke when throttle queue is at capacity.
 */
case class ThrottleAction(action: () => Unit, queueFull: () => Unit)
