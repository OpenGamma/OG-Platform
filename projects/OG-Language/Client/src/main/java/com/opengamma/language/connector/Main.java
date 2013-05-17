/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.language.install.ConfigureMain;
import com.opengamma.util.tuple.Pair;

/**
 * Entry point for the Language add-in, defining static methods for calling externally.
 */
public class Main {

  private static final Logger s_logger = LoggerFactory.getLogger(Main.class);

  private static LanguageSpringContext s_springContext;
  private static final ExecutorService s_executorService = Executors.newCachedThreadPool(new CustomizableThreadFactory("Client-"));

  private static int s_activeConnections;

  /**
   * Sets a system property.
   * 
   * @param property key of the property to set, never null
   * @param value value to set, never null
   */
  private static boolean setProperty(final String property, final String value) {
    try {
      s_logger.debug("Setting system property {}={}", property, value);
      System.setProperty(property, value);
      return true;
    } catch (final Throwable t) {
      s_logger.error("Couldn't set property {}={}", property, value);
      s_logger.warn("Exception thrown", t);
      return false;
    }
  }

  /**
   * Updates the store that the service wrapper retrieves properties from that are passed to {@link #setProperty}.
   * 
   * @param property key of the property to set, never null
   * @param value value to set, or null to delete the property
   */
  private static native void writeProperty(final String property, final String value);

  /**
   * Debug entry point from the service wrapper tests.
   * 
   * @return true always
   */
  public static boolean svcTest() {
    s_logger.info("svcTest called");
    return true;
  }

  /**
   * Entry point from the service wrapper - starts the service.
   * 
   * @return null if the service started properly, otherwise a string for display to the user describing why the stack wasn't started
   */
  public static String svcStart() {
    try {
      s_logger.info("Starting OpenGamma language integration service");
      s_springContext = new LanguageSpringContext();
      return null;
    } catch (final BeanCreationException e) {
      s_logger.error("Exception thrown", e);
      Throwable t = e;
      do {
        t = t.getCause();
      } while (t instanceof BeanCreationException);
      if (t != null) {
        return t.getMessage();
      } else {
        return e.getMessage();
      }
    } catch (final Throwable t) {
      s_logger.error("Exception thrown", t);
      return t.getMessage();
    }
  }

  /**
   * Entry point from the service wrapper - starts a connection handler for a given client.
   * 
   * @param userName the user name of the incoming connection
   * @param inputPipeName the pipe created for sending data from C++ to Java
   * @param outputPipeName the pipe created for sending data from Java to C++
   * @param languageID the identifier of the bound language. Language specific factories will be used if present, otherwise the default factories will be used.
   * @param debug true if the bound language is a debug build
   * @return true if the connection started okay
   */
  public static synchronized boolean svcAccept(final String userName, final String inputPipeName,
      final String outputPipeName, final String languageID, final boolean debug) {
    try {
      s_logger.info("Accepted {} connection from {}", languageID, userName);
      s_logger.debug("Using pipes IN:{} OUT:{}", inputPipeName, outputPipeName);
      final Pair<ClientFactory, SessionContextFactory> factories = s_springContext.getLanguageFactories(languageID);
      final SessionContext sessionContext = factories.getSecond().createSessionContext(userName, debug);
      final Client client = factories.getFirst().createClient(inputPipeName, outputPipeName, sessionContext);
      s_activeConnections++;
      s_executorService.submit(new Runnable() {
        @Override
        public void run() {
          client.run();
          s_logger.info("Session for {} disconnected", userName);
          clientDisconnected();
        }
      });
      return true;
    } catch (final Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

  /**
   * Reports that a client has disconnected. The last client to disconnect will cause the service to terminate. The potential race between this and a pending call to {@link #svcAccept} is handled by
   * locking within the service wrapper.
   */
  private static synchronized void clientDisconnected() {
    if (--s_activeConnections == 0) {
      s_logger.info("Attempting to stop service on last client disconnect");
      notifyStop();
    } else {
      s_logger.info("{} clients still connected", s_activeConnections);
    }
  }

  /**
   * Entry point for the service wrapper - queries if there are no active clients.
   * 
   * @return true if there are no active clients
   */
  public static synchronized boolean svcIsStopped() {
    return s_activeConnections == 0;
  }

  /**
   * Requests the host process stop the service. This may be ignored if there is a pending request that has not reached {@link #svcAccept} yet, or if stopping on the last client disconnect is
   * disabled.
   */
  private static native void notifyStop();

  /**
   * Deadlocks the calling thread against the pipe dispatch thread within the C++ layer. This is for testing error recovery by deliberately hanging the JVM. DO NOT CALL THIS FUNCTION UNLESS YOU WANT
   * THINGS TO BREAK.
   */
  public static native void notifyPause();

  /**
   * Entry point from the service wrapper - stops the service.
   * 
   * @return true if the service stopped cleanly
   */
  public static boolean svcStop() {
    try {
      s_logger.info("Waiting for client threads to stop");
      s_executorService.shutdown();
      s_executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      s_logger.info("Stopping application context");
      s_springContext.stop();
      s_logger.info("OpenGamma Language Integration service stopped");
      return true;
    } catch (final Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

  /**
   * Entry point from the service wrapper to configure the application.
   * 
   * @return true if the configuration callback ran, false if there was a problem
   */
  public static boolean svcConfigure() {
    new ConfigureMain(new ConfigureMain.Callback() {

      @Override
      public void setProperty(final String property, final String value) {
        Main.writeProperty(property, value);
      }

      @Override
      public String getProperty(final String property) {
        return System.getProperty(property);
      }

    }).run();
    return true;
  }

}
