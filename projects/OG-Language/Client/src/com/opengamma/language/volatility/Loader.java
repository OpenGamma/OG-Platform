/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.volatility;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.volatility.cube.rest.RemoteVolatilityCubeDefinitionSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with support for a volatility cube definition source (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);
  
  private static final String CONFIGURATION_ENTRY = "volatilityCubeDefinitionSource";
  private Configuration _configuration;
  
  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }
  
  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(CONFIGURATION_ENTRY);
    if (uri == null) {
      s_logger.warn("Volatility cube definition support not available");
      return;
    }
    s_logger.info("Configuring security support");
    globalContext.setVolatilityCubeDefinitionSource(new RemoteVolatilityCubeDefinitionSource(uri));
  }

}
