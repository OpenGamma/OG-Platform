/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.util.concurrent.Executors;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider.Validater;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.UniformInterfaceException404NotFound;

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
    _configurationUri = URI.create(configurationUri);
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
    Configuration configuration = getConfiguration();
    configuration.setFailOnInvalidURI(isFailOnInvalidURI());
    configuration.setFailOnMissingConfiguration(isFailOnMissingConfiguration());
    return configuration;
  }

  protected Configuration getConfiguration() {
    try {
      FudgeMsg msg = new RemoteConfiguration(getConfigurationURIAsURI()).getConfigurationMsg();
      Validater validater = UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool(), getConfigurationURIAsURI());
      return new Configuration(getFudgeContext(), msg, validater);
      
    } catch (UniformInterfaceException404NotFound ex) {
      return new Configuration(getFudgeContext(), getFudgeContext().newMessage(), null);
    }
  }

}
