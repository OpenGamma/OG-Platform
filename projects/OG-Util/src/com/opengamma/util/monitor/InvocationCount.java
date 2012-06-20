/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for measuring the number of times an invocation has occurred and the cumulative time spent in a piece of code. This is intended for debugging and basic profiling only and not for use
 * in production code. For example:
 * 
 * <pre>
 * private static final InvocationCount s_monitor = new InvocationCount(&quot;foo&quot;);
 * 
 * public void foo() {
 *   s_monitor.enter();
 *   try {
 *     // ... monitored code
 *   } finally {
 *     s_monitor.leave();
 *   }
 * }
 * </pre>
 */
public class InvocationCount {

  private static final Logger s_logger = LoggerFactory.getLogger(InvocationCount.class);
  private static final long REPORT = 1000000000L;

  private final String _name;
  private final AtomicLong _calls = new AtomicLong();
  private final AtomicLong _time = new AtomicLong();
  private long _lastReport = System.nanoTime();

  public InvocationCount(final String name) {
    _name = name;
  }

  /**
   * Mark entry into a monitored piece of code. This must be followed by a call to {@link #leave}.
   */
  public void enter() {
    _time.addAndGet(-System.nanoTime());
  }

  /**
   * Mark exit from a monitored piece of code. This must be preceded by a call to {@link #enter}.
   * 
   * @return the number of calls made, including this one.
   */
  public long leave() {
    final long calls = _calls.incrementAndGet();
    final long now = System.nanoTime();
    long time = _time.addAndGet(now);
    if (now - _lastReport > REPORT) {
      synchronized (this) {
        if (now - _lastReport > REPORT) {
          _lastReport = now;
          while (time < 0) {
            Thread.yield();
            time = _time.get();
          }
          s_logger.debug("{} calls to {} in {}ms", new Object[] {calls, _name, (double) time / 1e6 });
        }
      }
    }
    return calls;
  }

}
