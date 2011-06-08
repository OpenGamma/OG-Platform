/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.connector.debug.DebugClient;
import com.opengamma.language.connector.debug.DebugClientFactory;
import com.opengamma.language.context.GlobalContext;
import com.opengamma.language.context.GlobalContextFactory;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.util.tuple.Pair;

/**
 * Reads OG-Language-oriented Spring configuration files, and interprets any language-specific extensions.
 * <p>
 * Call {@link #stop()} to shut down the Spring context.
 */
public class LanguageSpringContext {

  private static final String CLIENT_FACTORY_CLASS_PROPERTY = "language.client.factory";
  private static final String CLIENT_FACTORY_METHOD = "getFactory";
  
  private static final String SYSTEM_SETTINGS = "SystemSettings";
  private static final String SESSION_CONTEXT_FACTORY = "SessionContextFactory";
  private static final String CLIENT_CONTEXT_FACTORY = "ClientContextFactory";
  
  private static final Logger s_logger = LoggerFactory.getLogger(LanguageSpringContext.class);
  
  private GenericApplicationContext _springContext;
  private ClientFactoryFactory _clientFactories;
  private ClientFactory _defaultClientFactory;
  private SessionContextFactory _defaultSessionContextFactory;

  private final Map<String, Pair<ClientFactory, SessionContextFactory>> _languageFactories = new HashMap<String, Pair<ClientFactory, SessionContextFactory>>();

  public LanguageSpringContext() {
    _springContext = new GenericApplicationContext();
    s_logger.debug("Reading OpenGamma.xml");
    final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(_springContext);
    xmlReader.loadBeanDefinitions(new ClassPathResource("OpenGamma.xml"));
    s_logger.debug("Finished loading core bean definitions");
    _springContext.refresh();
    // TODO: load other .xml files from the ext/ folder
    s_logger.info("Starting application context");
    _springContext.start();
    s_logger.info("Application context started");
    
    Properties systemSettings = getBean(StringUtils.uncapitalize(SYSTEM_SETTINGS), Properties.class);
    String clientFactoryClassName = systemSettings.getProperty(CLIENT_FACTORY_CLASS_PROPERTY);
    if (!StringUtils.isBlank(clientFactoryClassName)) {
      try {
        Class<?> clientFactoryClass = Class.forName(clientFactoryClassName);
        Method factoryFactoryMethod = clientFactoryClass.getMethod(CLIENT_FACTORY_METHOD);
        _clientFactories = (ClientFactoryFactory) factoryFactoryMethod.invoke(null);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Error using custom client factory: " + clientFactoryClassName, e);
      }
    } else {
      _clientFactories = ClientFactory.getFactory();
    }
    
    final ClientContextFactory clientContextFactory = getBean(StringUtils.uncapitalize(CLIENT_CONTEXT_FACTORY), ClientContextFactory.class);
    if (clientContextFactory != null) {
      _defaultClientFactory = _clientFactories.createClientFactory(clientContextFactory.createClientContext());
    } else {
      s_logger.info("No default client context factory");
      _defaultClientFactory = null;
    }
    _defaultSessionContextFactory = getBean(StringUtils.uncapitalize(SESSION_CONTEXT_FACTORY), SessionContextFactory.class);
    if (_defaultSessionContextFactory == null) {
      s_logger.info("No default session context factory");
    }
  }
  
  public void stop() {
    _springContext.stop();
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
  public Pair<ClientFactory, SessionContextFactory> getLanguageFactories(final String languageID) {
    Pair<ClientFactory, SessionContextFactory> factories = _languageFactories.get(languageID);
    if (factories == null) {
      s_logger.info("Resolving factories for {}", languageID);
      final ClientContextFactory clientContextFactory = getBean(languageID + CLIENT_CONTEXT_FACTORY,
          ClientContextFactory.class);
      final ClientFactory clientFactory;
      if (clientContextFactory == null) {
        if (_defaultClientFactory == null) {
          s_logger.error("No client context factory for {} defined and no default factory", languageID);
          throw new IllegalArgumentException();
        }
        clientFactory = _defaultClientFactory;
      } else {
        clientFactory = _clientFactories.createClientFactory(clientContextFactory.createClientContext());
      }
      SessionContextFactory sessionContextFactory = getBean(languageID + SESSION_CONTEXT_FACTORY,
          SessionContextFactory.class);
      if (sessionContextFactory == null) {
        if (_defaultSessionContextFactory == null) {
          s_logger.error("No session context factory for {} defined and no default factory", languageID);
          throw new IllegalArgumentException();
        }
        sessionContextFactory = _defaultSessionContextFactory;
      }
      factories = Pair.of(clientFactory, sessionContextFactory);
      _languageFactories.put(languageID, factories);
    }
    return factories;
  }
  
  private <T> T getBean(final String beanName, final Class<T> clazz) {
    try {
      s_logger.debug("Trying {}", beanName);
      return _springContext.getBean(beanName, clazz);
    } catch (BeansException e) {
      s_logger.warn("Bean {} not defined", beanName);
      return null;
    }
  }
  
}
