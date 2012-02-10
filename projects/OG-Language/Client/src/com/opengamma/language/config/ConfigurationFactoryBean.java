/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating a {@link Configuration} object from a supplied URL. 
 */
public final class ConfigurationFactoryBean extends SingletonFactoryBean<Configuration> {

  private static final Logger s_logger = LoggerFactory.getLogger(ConfigurationFactoryBean.class);

  private RestTarget _configurationTarget;
  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;
  private boolean _failOnInvalidURI;
  private boolean _failOnMissingConfiguration;

  public void setConfigurationURI(final String configurationURI) {
    ArgumentChecker.notNull(configurationURI, "configurationURI");
    setConfigurationTarget(new RestTarget(configurationURI));
  }

  public String getConfigurationURI() {
    return getConfigurationTarget().getURI().toString();
  }

  public void setConfigurationTaxonomy(final int taxonomyId) {
    setConfigurationTarget(getConfigurationTarget().withTaxonomyId(taxonomyId));
  }

  public int getConfigurationTaxonomy() {
    return getConfigurationTarget().getTaxonomyId();
  }

  public void setConfigurationTarget(final RestTarget configurationTarget) {
    ArgumentChecker.notNull(configurationTarget, "configurationTarget");
    _configurationTarget = configurationTarget;
  }

  public RestTarget getConfigurationTarget() {
    return _configurationTarget;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setFailOnInvalidURI(final boolean failOnInvalidURI) {
    _failOnInvalidURI = failOnInvalidURI;
  }

  public boolean isFailOnInvalidURI() {
    return _failOnInvalidURI;
  }

  public void setFailOnMissingConfiguration(final boolean failOnMissingConfiguration) {
    _failOnMissingConfiguration = failOnMissingConfiguration;
  }

  public boolean isFailOnMissingConfiguration() {
    return _failOnMissingConfiguration;
  }

  // SingletonFactoryBean

  @Override
  protected Configuration createObject() {
    ArgumentChecker.notNull(getConfigurationTarget(), "configurationTarget");
    final RestClient client = RestClient.getInstance(getFudgeContext(), null);
    s_logger.debug("Querying configuration document at {}", getConfigurationTarget());
    final FudgeMsg msg = client.getMsg(getConfigurationTarget());
    final Configuration configuration;
    if (msg == null) {
      s_logger.error("No configuration document found at {}", getConfigurationURI());
      configuration = new Configuration(getFudgeContext(), FudgeContext.EMPTY_MESSAGE, null);
    } else {
      s_logger.debug("Configuration document {}", msg);
      configuration = new Configuration(getFudgeContext(), msg, UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool(), getConfigurationTarget().getURI()));
    }
    configuration.setFailOnInvalidURI(isFailOnInvalidURI());
    configuration.setFailOnMissingConfiguration(isFailOnMissingConfiguration());
    return configuration;
  }

}
