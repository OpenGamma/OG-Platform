/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Sub-class of the standard {@link OpenGammaComponentServer} that works with the Advanced
 * Installer service wrappers when installed on Windows.
 */
public class OpenGammaComponentService extends OpenGammaComponentServer {

  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaComponentService.class);

  private static final OpenGammaComponentService INSTANCE = new OpenGammaComponentService();

  private final CountDownLatch _latch = new CountDownLatch(1);
  private final AtomicReference<ComponentRepository> _repository = new AtomicReference<ComponentRepository>();

  /**
   * Starts the service, blocking until the stop signal is received.
   * 
   * @param args the command line arguments
   */
  public static void main(final String[] args) { // CSIGNORE
    s_logger.info("Starting service");
    try {
      INSTANCE.run(args);
    } catch (Throwable e) {
      s_logger.error("Couldn't start service", e);
    }
  }

  /**
   * Stops the service.
   */
  public static void stop() {
    s_logger.info("Stopping service");
    INSTANCE.serverStopping();
  }

  @Override
  public void run(final String[] args) {
    super.run(args);
    s_logger.info("Service started -- waiting for stop signal");
    try {
      _latch.await();
      s_logger.info("Service stopped");
    } catch (InterruptedException e) {
      s_logger.warn("Service interrupted");
      throw new OpenGammaRuntimeException("Interrupted", e);
    }
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
      _latch.countDown();
    } else {
      s_logger.warn("Stop signal received before service startup completed");
      System.exit(0);
    }
  }

}
