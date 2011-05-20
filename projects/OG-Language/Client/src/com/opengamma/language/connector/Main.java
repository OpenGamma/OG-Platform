/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.StringUtils;

import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.util.tuple.Pair;

/**
 * Entry point for the Language add-in. Kicks off a Spring configuration script to create the main connector and
 * load language specific extensions.
 */
public class Main {

  private static final Logger s_logger = LoggerFactory.getLogger(Main.class);

  private static final String SESSION_CONTEXT_FACTORY = "SessionContextFactory";
  private static final String CLIENT_CONTEXT_FACTORY = "ClientContextFactory";

  private static GenericApplicationContext s_springContext;
  private static ClientFactoryFactory s_clientFactories;
  private static ClientFactory s_defaultClientFactory;
  private static SessionContextFactory s_defaultSessionContextFactory;

  private static final Map<String, Pair<ClientFactory, SessionContextFactory>> s_languageFactories = new HashMap<String, Pair<ClientFactory, SessionContextFactory>>();
  private static final ExecutorService s_executorService = Executors.newCachedThreadPool(new CustomizableThreadFactory(
      "Client-"));

  private static int s_activeConnections;

  /**
   * Debug entry point from the service wrapper tests.
   * 
   * @return {@code true}
   */
  public static boolean svcTest() {
    s_logger.info("svcTest called");
    return true;
  }

  private static <T> T getBean(final String beanName, final Class<T> clazz) {
    try {
      s_logger.debug("Trying {}", beanName);
      return s_springContext.getBean(beanName, clazz);
    } catch (BeansException e) {
      s_logger.warn("Bean {} not defined", beanName);
      return null;
    }
  }

  /**
   * Entry point from the service wrapper - starts the service.
   * 
   * @return {@code true} if the service started properly, {@code false} otherwise
   */
  public static boolean svcStart() {
    try {
      s_logger.info("Starting OpenGamma language integration service");
      s_springContext = new GenericApplicationContext();
      s_logger.debug("Reading OpenGamma.xml");
      final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(s_springContext);
      xmlReader.loadBeanDefinitions(new ClassPathResource("OpenGamma.xml"));
      s_logger.debug("Finished loading core bean definitions");
      s_springContext.refresh();
      // TODO: load other .xml files from the ext/ folder
      s_logger.info("Starting application context");
      s_springContext.start();
      s_logger.info("Application context started");
      s_clientFactories = ClientFactory.getFactory();
      // TODO: allow the client factory factory to be selected/overridden from command line, or property for e.g. the remote debugging version
      final ClientContextFactory clientContextFactory = getBean(StringUtils.uncapitalize(CLIENT_CONTEXT_FACTORY), ClientContextFactory.class);
      if (clientContextFactory != null) {
        s_defaultClientFactory = s_clientFactories.createClientFactory(clientContextFactory.createClientContext());
      } else {
        s_logger.info("No default client context factory");
        s_defaultClientFactory = null;
      }
      s_defaultSessionContextFactory = getBean(StringUtils.uncapitalize(SESSION_CONTEXT_FACTORY), SessionContextFactory.class);
      if (s_defaultSessionContextFactory == null) {
        s_logger.info("No default session context factory");
      }
      return true;
    } catch (Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

  /**
   * Returns the factories for a bound language. By default "clientContextFactory" and "sessionContextFactory" are used
   * and should generally be extended in a language agnostic fashion, or use custom message filters with an explicit hierarchy
   * that won't interfere with any other language bindings. If behaviors that will interfere are needed, custom factories
   * can be specified. For a language "Foo", "FooClientContextFactory" and "FooSessionContextFactory" will take
   * precedent over the defaults if they are defined.
   * 
   * @param languageID the language ID from the incoming client connection
   * @return the {@link ClientFactory} and {@link SessionContextFactory} instances 
   */
  private static Pair<ClientFactory, SessionContextFactory> getLanguageFactories(final String languageID) {
    Pair<ClientFactory, SessionContextFactory> factories = s_languageFactories.get(languageID);
    if (factories == null) {
      s_logger.info("Resolving factories for {}", languageID);
      final ClientContextFactory clientContextFactory = getBean(languageID + CLIENT_CONTEXT_FACTORY,
          ClientContextFactory.class);
      final ClientFactory clientFactory;
      if (clientContextFactory == null) {
        if (s_defaultClientFactory == null) {
          s_logger.error("No client context factory for {} defined and no default factory", languageID);
          throw new IllegalArgumentException();
        }
        clientFactory = s_defaultClientFactory;
      } else {
        clientFactory = s_clientFactories.createClientFactory(clientContextFactory.createClientContext());
      }
      SessionContextFactory sessionContextFactory = getBean(languageID + SESSION_CONTEXT_FACTORY,
          SessionContextFactory.class);
      if (sessionContextFactory == null) {
        if (s_defaultSessionContextFactory == null) {
          s_logger.error("No session context factory for {} defined and no default factory", languageID);
          throw new IllegalArgumentException();
        }
        sessionContextFactory = s_defaultSessionContextFactory;
      }
      factories = Pair.of(clientFactory, sessionContextFactory);
      s_languageFactories.put(languageID, factories);
    }
    return factories;
  }

  /**
   * Entry point from the service wrapper - starts a connection handler for a given client.
   * 
   * @param userName the user name of the incoming connection
   * @param inputPipeName the pipe created for sending data from C++ to Java
   * @param outputPipeName the pipe created for sending data from Java to C++
   * @param languageID the identifier of the bound language. Language specific factories will
   *                   be used if present, otherwise the default factories will be used.
   * @param debug {@code true} if the bound language is a debug build, {@code false} otherwise 
   * @return {@code true} if the connection started okay, {@code false} otherwise
   */
  public static synchronized boolean svcAccept(final String userName, final String inputPipeName,
      final String outputPipeName, final String languageID, final boolean debug) {
    try {
      s_logger.info("Accepted {} connection from {}", languageID, userName);
      s_logger.debug("Using pipes IN:{} OUT:{}", inputPipeName, outputPipeName);
      final Pair<ClientFactory, SessionContextFactory> factories = getLanguageFactories(languageID);
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
    } catch (Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

  /**
   * Reports that a client has disconnected. The last client to disconnect will cause the service to terminate.
   * The potential race between this and a pending call to {@link #svcAccept} is handled by locking within the
   * service wrapper. 
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
   * @return {@code true} if there are no active clients, {@code false} otherwise.
   */
  public static synchronized boolean svcIsStopped() {
    return s_activeConnections == 0;
  }

  /**
   * Requests the host process stop the service. This may be ignored if there is a pending request that
   * has not reached {@link #svcAccept} yet, or if stopping on the last client disconnect is disabled.
   */
  private static native void notifyStop();

  /**
   * Deadlocks the calling thread against the pipe dispatch thread within the C++ layer. This
   * is for testing error recovery by deliberately hanging the JVM.
   * 
   * DO NOT CALL THIS FUNCTION UNLESS YOU WANT THINGS TO BREAK.
   */
  public static native void notifyPause();

  /**
   * Entry point from the service wrapper - stops the service.
   * 
   * @return {@code true} if the service stopped cleanly, {@code false} otherwise
   */
  public static boolean svcStop() {
    try {
      s_logger.info("Waiting for client threads to stop");
      s_executorService.shutdown();
      s_executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      s_logger.info("Stopping application context");
      s_springContext.stop();
      s_logger.info("OpenGamma Excel Integration service stopped");
      return true;
    } catch (Throwable t) {
      s_logger.error("Exception thrown", t);
      return false;
    }
  }

}
