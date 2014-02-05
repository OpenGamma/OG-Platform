/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sub-class of the standard {@link OpenGammaComponentServer} that works with the
 * Advanced Installer service wrappers when installed on Windows.
 */
public class OpenGammaComponentService extends OpenGammaComponentServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaComponentService.class);
  /** Logger. */
  private static final Logger s_startupLogger = LoggerFactory.getLogger(ComponentManager.class);
  /**
   * Single instance.
   */
  private static final OpenGammaComponentService INSTANCE = new OpenGammaComponentService();

  /**
   * Latch used when stopping.
   */
  private final CountDownLatch _stopNotify = new CountDownLatch(1);
  /**
   * Latch used when stopping.
   */
  private final CountDownLatch _stopConfirm = new CountDownLatch(1);
  /**
   * The component repository.
   */
  private final AtomicReference<ComponentRepository> _repository = new AtomicReference<ComponentRepository>();

  //-------------------------------------------------------------------------
  /**
   * Starts the service, blocking until the stop signal is received.
   * 
   * @param args the command line arguments, the last element is the service name
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.info("Starting service {}", args[args.length - 1]);
    final String[] runArgs = new String[args.length - 1];
    System.arraycopy(args, 0, runArgs, 0, runArgs.length);
    try {
      if (!INSTANCE.run(runArgs)) {
        s_logger.error("One or more errors occurred starting the service");
        System.exit(1);
        //} else {
        //System.exit(0);
      }
    } catch (Throwable e) {
      s_logger.error("Couldn't start service", e);
      System.exit(1);
    }
  }

  /**
   * Stops the service.
   */
  public static void stop() {
    s_logger.info("Stopping service");
    INSTANCE.serverStopping();
    s_logger.info("Service stopped");
    // This is bad. Not everything currently responds nicely to the "stop" and non-daemon threads
    // keep the process alive. Remove this hack when there are no more non-daemon threads that can
    // outlive their components and prevent process termination when running as a service.
    int aliveCount = 0, nonDaemon = 0;
    for (Map.Entry<Thread, StackTraceElement[]> active : Thread.getAllStackTraces().entrySet()) {
      final Thread t = active.getKey();
      if (t.isAlive()) {
        if (!t.isDaemon()) {
          s_logger.debug("Thread {} still active", t);
          for (StackTraceElement stack : active.getValue()) {
            s_logger.debug("Stack: {}", stack);
          }
          nonDaemon++;
        }
        aliveCount++;
      }
    }
    if (nonDaemon > 0) {
      s_logger.error("{} non-daemon thread(s) (of {}) still active at shutdown, calling system.exit", nonDaemon, aliveCount);
      System.exit(1);
    } else {
      s_logger.info("No non-daemon threads (of {}) active at shutdown", aliveCount);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean run(final String[] args) {
    if (!super.run(args)) {
      return false;
    }
    s_logger.info("Service started -- waiting for stop signal");
    try {
      _stopNotify.await();
      s_logger.info("Service stopped");
      _stopConfirm.countDown();
      return true;
    } catch (InterruptedException e) {
      s_logger.warn("Service interrupted");
      return false;
    }
  }

  @Override
  protected ComponentLogger createLogger(int verbosity) {
    return new ComponentLogger.Slf4JLogger(s_startupLogger);
  }

  @Override
  protected void serverStarting(final ComponentManager manager) {
    s_logger.debug("Server starting - got component repository");
    final ComponentRepository previous = _repository.getAndSet(manager.getRepository());
    assert (previous == null);
  }

  protected void serverStopping() {
    final ComponentRepository repository = _repository.getAndSet(null);
    if (repository != null) {
      s_logger.info("Stopping components");
      try {
        repository.stop();
      } catch (Throwable e) {
        s_logger.error("Couldn't stop components", e);
      }
      s_logger.debug("Releasing main thread");
      _stopNotify.countDown();
      s_logger.info("Waiting for confirmation signal");
      try {
        _stopConfirm.await();
      } catch (InterruptedException e) {
        s_logger.warn("Service interrupted");
        System.exit(1);
      }
    } else {
      s_logger.warn("Stop signal received before service startup completed");
      System.exit(1);
    }
  }

}
