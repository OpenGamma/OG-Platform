/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.currency;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.currency.rest.RemoteCurrencyPairsSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with currency pairs support.
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "currencyPairsSource";
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

  @Override
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("Currency pair support not available");
      return;
    }
    s_logger.info("Configuring currency pair support");
    globalContext.getFunctionProvider().addProvider(new FunctionProviderBean(
        CurrencyPairFunction.INSTANCE,
        FxRateFunction.INSTANCE));
    globalContext.setCurrencyPairsSource(new RemoteCurrencyPairsSource(uri));
  }

}
