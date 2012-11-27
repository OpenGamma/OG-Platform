/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.util.tuple.Pair;

/**
 * Reads OG-Language-oriented Spring configuration files, and interprets any language-specific extensions.
 * <p>
 * Call {@link #stop()} to shut down the Spring context.
 */
public class LanguageSpringContext {

  /** Location on the classpath of the main Spring configuration file. */
  public static final String CLIENT_XML = "/com/opengamma/language/connector/Client.xml";

  /** Name of the system property specifying the location of the Spring XML config. */
  public static final String LANGUAGE_EXT_PATH = "language.ext.path";

  /** Name of the system property to specify an alternative client factory (e.g. for debug configurations. */
  public static final String CLIENT_FACTORY_CLASS_PROPERTY = "language.client.factory";

  private static final String CLIENT_FACTORY_METHOD = "getFactory";
  private static final String SESSION_CONTEXT_FACTORY = "SessionContextFactory";
  private static final String CLIENT_CONTEXT_FACTORY = "ClientContextFactory";

  private static final Logger s_logger = LoggerFactory.getLogger(LanguageSpringContext.class);

  private GenericApplicationContext _springContext;
  private ClientFactoryFactory _clientFactories;
  private ClientFactory _defaultClientFactory;
  private SessionContextFactory _defaultSessionContextFactory;

  private final Map<String, Pair<ClientFactory, SessionContextFactory>> _languageFactories = new HashMap<String, Pair<ClientFactory, SessionContextFactory>>();

  public LanguageSpringContext() {
    _springContext = createSpringContext();
    final String clientFactoryClassName = System.getProperty(CLIENT_FACTORY_CLASS_PROPERTY);
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

  /**
   * Creates a Spring context from the base configuration file in OG-Language and any other Spring XML configuration
   * files found in the configuration directory.  The directory must be specified using the system property named
   * {@link #LANGUAGE_EXT_PATH}.
   * @return A Spring context built from all the XML config files.
   */
  public static GenericApplicationContext createSpringContext() {
    s_logger.info("Starting OpenGamma language integration service");
    GenericApplicationContext context = new GenericApplicationContext();
    final XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(context);
    xmlReader.loadBeanDefinitions(new ClassPathResource(CLIENT_XML));
    String[] xmlFiles = findSpringXmlConfig();
    xmlReader.loadBeanDefinitions(xmlFiles);
    s_logger.info("Creating context beans");
    context.refresh();
    s_logger.info("Starting application context");
    context.start();
    s_logger.info("Application context started");
    return context;
  }

  /**
   * Searches the configuration directory for Spring XML files to load.  The directory must be specified using the
   * system property named {@link #LANGUAGE_EXT_PATH}. The files are returned in filename alphabetical order (case
   * insensitive) so that load order is deterministic.
   * @return Names of all the XML files in the configuration directory with {@code file:} prepended (so Spring knows
   * they are filesystem resources and not classpath resources)
   */
  private static String[] findSpringXmlConfig() {
    String extPath = System.getProperty(LANGUAGE_EXT_PATH);
    if (StringUtils.isEmpty(extPath)) {
      throw new OpenGammaRuntimeException("The directory containing the Spring XML config files for language support " +
                                              "must be specified in the system property " + LANGUAGE_EXT_PATH);
    }
    File extDir = new File(extPath);
    s_logger.debug("Scanning '{}' for Spring XML config files to load", extDir.getAbsolutePath());
    List<File> xmlFiles = new ArrayList<File>(FileUtils.listFiles(extDir, new String[] {"xml" }, false));
    Collections.sort(xmlFiles, new Comparator<File>() {
      @Override
      public int compare(final File o1, final File o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    String[] xmlFileNames = new String[xmlFiles.size()];
    int i = 0;
    for (File xmlFile : xmlFiles) {
      String xmlPath = xmlFile.getAbsolutePath();
      s_logger.debug("Found XML file: '{}'", xmlPath);
      xmlFileNames[i++] = "file:" + xmlPath;
    }
    return xmlFileNames;
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
