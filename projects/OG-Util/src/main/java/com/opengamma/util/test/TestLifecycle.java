/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.Lifecycle;

/**
 * Helper class for managing {@link Lifecycle} implementing objects during a test session. Objects may be created by test infrastructure that must be shutdown but are embedded or wrapped in other
 * object instances which don't expose life cycle methods so may not be directly available for the caller to explicitly shut them down.
 * <p>
 * Typically a test method may written along the lines of:
 * 
 * <pre>
 * public void testFoo() {
 *   TestLifecycle.begin();
 *   try {
 *     // ... test stuff
 *   } finally {
 *     TestLifecycle.end();
 *   }
 * }
 * </pre>
 * 
 * Any test infrastructure that creates things that must be shutdown at the end of the test can register them, after they've been started, by a call to {@link #register}.
 */
public final class TestLifecycle {

  private static final ThreadLocal<TestLifecycle> s_instances = new ThreadLocal<TestLifecycle>();

  private final List<Lifecycle> _toStop = new ArrayList<Lifecycle>();

  private TestLifecycle() {
  }

  private static TestLifecycle instance() {
    final TestLifecycle instance = s_instances.get();
    if (instance != null) {
      return instance;
    } else {
      throw new IllegalStateException("Current thread not associated with a test instance");
    }
  }

  /**
   * Call at the start of a test.
   */
  public static void begin() {
    if (s_instances.get() != null) {
      throw new IllegalStateException("Current thread already associated with a test instance");
    }
    s_instances.set(new TestLifecycle());
  }

  /**
   * Call at the end of a test.
   */
  public static void end() {
    final List<Lifecycle> toStop = instance()._toStop;
    s_instances.set(null);
    for (Lifecycle instance : toStop) {
      if (instance.isRunning()) {
        instance.stop();
      }
    }
  }

  /**
   * Associate a {@link Lifecycle} instance with this test session. When the session ends the {@link Lifecycle#stop} method will be called.
   * <p>
   * The object will only be registered if it is already started.
   * 
   * @param instance the instance to register, not null
   * @throws IllegalStateException if {@link #begin} has not been called for this session
   */
  public static void register(final Lifecycle instance) {
    if (instance.isRunning()) {
      instance()._toStop.add(instance);
    }
  }

}
