/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.FunctionProviderBean;
import com.opengamma.language.invoke.TypeConverterProviderBean;
import com.opengamma.language.procedure.ProcedureProviderBean;
import com.opengamma.util.ArgumentChecker;

/**
 * Extends the contexts with time series support (if available).
 */
public class Loader extends ContextInitializationBean {

  private static final Logger s_logger = LoggerFactory.getLogger(Loader.class);

  private String _configurationEntry = "historicalTimeSeriesSource";
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
  protected void assertPropertiesSet() {
    ArgumentChecker.notNull(getConfiguration(), "configuration");
  }

  @Override
  protected void initContext(final MutableGlobalContext globalContext) {
    final URI uri = getConfiguration().getURIConfiguration(getConfigurationEntry());
    if (uri == null) {
      s_logger.warn("Time series support not available");
      return;
    }
    s_logger.info("Configuring time-series support");
    globalContext.setHistoricalTimeSeriesSource(new RemoteHistoricalTimeSeriesSource(uri));
    globalContext.getFunctionProvider().addProvider(new FunctionProviderBean(
        FetchTimeSeriesFunction.INSTANCE));
    globalContext.getProcedureProvider().addProvider(new ProcedureProviderBean(
        StoreTimeSeriesProcedure.INSTANCE));
    globalContext.getTypeConverterProvider().addTypeConverterProvider(new TypeConverterProviderBean(
        HistoricalTimeSeriesConverter.INSTANCE,
        LocalDateDoubleTimeSeriesConverter.INSTANCE));
  }

}
