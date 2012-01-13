/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.config;

import java.net.URI;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.transport.jaxrs.UriEndPointDescriptionProvider;
import com.opengamma.util.ArgumentChecker;

/**
 * Configuration document describing how to connect to or otherwise work with components from an OpenGamma installation. Configuration
 * information is typically published at a URL as a Fudge message containing further URLs and relevant configuration strings. 
 */
public final class Configuration {

  private static final Logger s_logger = LoggerFactory.getLogger(Configuration.class);

  private final FudgeContext _fudgeContext;
  private final FudgeMsg _configuration;
  private boolean _failOnInvalidURI;
  private boolean _failOnMissingConfiguration;
  private UriEndPointDescriptionProvider.Validater _uriValidater;

  protected Configuration(final FudgeContext fudgeContext, final FudgeMsg configuration) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(configuration, "configuration");
    _fudgeContext = fudgeContext;
    _configuration = configuration;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  protected FudgeMsg getConfiguration() {
    return _configuration;
  }

  protected void setFailOnInvalidURI(final boolean failOnInvalidURI) {
    _failOnInvalidURI = failOnInvalidURI;
  }

  protected boolean isFailOnInvalidURI() {
    return _failOnInvalidURI;
  }

  protected void setFailOnMissingConfiguration(final boolean failOnMissingConfiguration) {
    _failOnMissingConfiguration = failOnMissingConfiguration;
  }

  protected boolean isFailOnMissingConfiguration() {
    return _failOnMissingConfiguration;
  }

  protected UriEndPointDescriptionProvider.Validater getURIValidater() {
    if (_uriValidater == null) {
      _uriValidater = UriEndPointDescriptionProvider.validater(Executors.newCachedThreadPool());
    }
    return _uriValidater;
  }

  protected <T> T missingConfiguration(final String entry) {
    if (isFailOnMissingConfiguration()) {
      throw new OpenGammaRuntimeException("Missing configuration " + entry);
    } else {
      s_logger.debug("Ignoring missing configuration {}", entry);
    }
    return null;
  }

  protected <T> T invalidUrl(final String entry) {
    if (isFailOnInvalidURI()) {
      throw new OpenGammaRuntimeException("Invalid URI for configuration entry " + entry);
    } else {
      s_logger.debug("Ignoring invalid URI for {}", entry);
    }
    return null;
  }

  /**
   * Returns a sub-configuration document.
   * 
   * @param entry configuration item name
   * @return the configuration document, or null if there is none (and passive failure is allowed)
   */
  public Configuration getSubConfiguration(final String entry) {
    final FudgeMsg submsg = getConfiguration().getMessage(entry);
    if (submsg == null) {
      s_logger.warn("No sub-configuration {}", entry);
      return missingConfiguration(entry);
    }
    return new Configuration(getFudgeContext(), submsg);
  }

  /**
   * Returns a REST end point as a {@link URI}
   * 
   * @param entry configuration item name
   * @return the URI, or null if there is none or it is inaccessible (and passive failure is allowed)
   */
  public URI getURIConfiguration(final String entry) {
    final FudgeMsg submsg = getConfiguration().getMessage(entry);
    if (submsg == null) {
      s_logger.warn("No URI for {}", entry);
      return missingConfiguration(entry);
    }
    final URI uri = getURIValidater().getAccessibleURI(submsg);
    if (uri == null) {
      s_logger.warn("No accessible URI for {}", entry);
      s_logger.debug("Tried {}", submsg);
      return invalidUrl(entry);
    }
    return uri;
  }

  /**
   * Returns a REST end point as a {@link RestTarget}
   * 
   * @param entry configuration item name
   * @return the RestTarget, or null if there is none or it is inaccessible (and passive failure is allowed)
   */
  public RestTarget getRestTargetConfiguration(final String entry) {
    final URI uri = getURIConfiguration(entry);
    if (uri == null) {
      return null;
    }
    return new RestTarget(uri);
  }

  // TODO: JMS configuration

  /**
   * Returns an arbitrary string value.
   * 
   * @param entry configuration item name
   * @return the string value
   */
  public String getStringConfiguration(final String entry) {
    final String value = getConfiguration().getString(entry);
    if (value == null) {
      s_logger.warn("No string for {}", entry);
      return missingConfiguration(entry);
    }
    return value;
  }

}
