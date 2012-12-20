/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the context with trade support.
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);
  
  private String _configurationEntry = "holidaySource";
  private Configuration _configuration;
  
  public void setConfiguration(final Configuration configuration) {
    ArgumentChecker.notNull(configuration, "configuration");
    _configuration = configuration;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public void setConfigurationEntry(final String configurationEntry) {
    ArgumentChecker.notNull(configurationEntry, "configurationEntry");
    _configurationEntry = configurationEntry;
  }

  public String getConfigurationEntry() {
    return _configurationEntry;
  }

  // ContextInitializationBean

  @Override
  protected void initContext(MutableGlobalContext globalContext) {
    s_logger.info("Configuring trade support");
    final FunctionProviderBean functions = new FunctionProviderBean();
    functions.addFunction(new EncodeDealFunction());
    functions.addFunction(new DecodeDealFunction());
    globalContext.getFunctionProvider().addProvider(functions);
  }
  
}
