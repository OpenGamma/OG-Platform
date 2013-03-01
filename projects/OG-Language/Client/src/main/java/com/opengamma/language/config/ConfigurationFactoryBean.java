/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.ConnectorStartupError;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider.Validater;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Factory bean for creating a {@link Configuration} object from a supplied URL.
 */
public final class ConfigurationFactoryBean extends SingletonFactoryBean<Configuration> {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigurationFactoryBean.class);

  private URI _configurationUri;
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  private boolean _failOnInvalidURI;
  private boolean _failOnMissingConfiguration;

  //-------------------------------------------------------------------------
  public URI getConfigurationURIAsURI() {
    return _configurationUri;
  }

  public String getConfigurationURI() {
    return ObjectUtils.toString(_configurationUri, null);
  }

  public void setConfigurationURI(final String configurationUri) {
    ArgumentChecker.notNull(configurationUri, "configurationURI");
    try {
      _configurationUri = new URI(configurationUri);
    } catch (final URISyntaxException ex) {
      throw new ConnectorStartupError("The configuration URL is not valid: " + configurationUri,
          "A valid configuration URL must be supplied to start the service. Please re-run the Language Integration " +
              "configuration tool or consult the distribution documentation for more details.");
    }
  }

  //-------------------------------------------------------------------------
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  public boolean isFailOnInvalidURI() {
    return _failOnInvalidURI;
  }

  public void setFailOnInvalidURI(final boolean failOnInvalidURI) {
    _failOnInvalidURI = failOnInvalidURI;
  }

  //-------------------------------------------------------------------------
  public boolean isFailOnMissingConfiguration() {
    return _failOnMissingConfiguration;
  }

  public void setFailOnMissingConfiguration(final boolean failOnMissingConfiguration) {
    _failOnMissingConfiguration = failOnMissingConfiguration;
  }

  //-------------------------------------------------------------------------
  // SingletonFactoryBean
  @Override
  protected Configuration createObject() {
    ArgumentChecker.notNull(_configurationUri, "configurationUri");
    s_logger.debug("Querying configuration document at {}", getConfigurationURI());
    final Configuration configuration = getConfiguration();
    configuration.setFailOnInvalidURI(isFailOnInvalidURI());
    configuration.setFailOnMissingConfiguration(isFailOnMissingConfiguration());
    return configuration;
  }

  protected FudgeMsg getConfigurationMessage() {
    final RemoteConfiguration remote = new RemoteConfiguration(getConfigurationURIAsURI());
    final FudgeMsg msg;
    try {
      msg = remote.getConfigurationMsg();
    } catch (final Throwable t) {
      throw new ConnectorStartupError("The OpenGamma server at " + getConfigurationURI() + " is not responding",
          "The Language Integration service cannot start without an OpenGamma backend server to connect to. " +
              "Please check the OpenGamma server is running and try again.");
    }
    if (msg == null) {
      throw new ConnectorStartupError("The configuration address " + getConfigurationURI() + " is not valid",
          "Either the configuration address is wrong, or the OpenGamma server is not running correctly.",
          "Please check the OpenGamma server is running, re-run the Language Integration " +
              "configuration tool or consult the distribution documentation for more details.");
    }
    return msg;
  }

  protected Configuration getConfiguration() {
    final Validater validater = UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool(), getConfigurationURIAsURI());
    final FudgeMsg msg = getConfigurationMessage();
    return new Configuration(getFudgeContext(), msg, validater);
  }

}
