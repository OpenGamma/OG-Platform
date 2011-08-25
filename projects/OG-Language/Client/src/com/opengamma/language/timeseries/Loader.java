/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.timeseries;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.historicaltimeseries.rest.RemoteHistoricalTimeSeriesSource;
import com.opengamma.language.config.Configuration;
import com.opengamma.language.context.ContextInitializationBean;
import com.opengamma.language.context.MutableGlobalContext;
import com.opengamma.language.function.AbstractFunctionProvider;
import com.opengamma.language.function.MetaFunction;
import com.opengamma.language.invoke.AbstractTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverter;
import com.opengamma.transport.jaxrs.RestTarget;
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
    final RestTarget restTarget = getConfiguration().getRestTargetConfiguration(getConfigurationEntry());
    if (restTarget == null) {
      s_logger.warn("Time series support not available");
      return;
    }
    s_logger.info("Configuring time-series support");
    globalContext.setHistoricalTimeSeriesSource(new RemoteHistoricalTimeSeriesSource(getConfiguration().getFudgeContext(), restTarget));
    globalContext.getFunctionProvider().addProvider(new AbstractFunctionProvider() {
      @Override
      protected void loadDefinitions(final Collection<MetaFunction> definitions) {
        definitions.add(new FetchTimeSeriesFunction().getMetaFunction());
      }
    });
    globalContext.getTypeConverterProvider().addTypeConverterProvider(new AbstractTypeConverterProvider() {
      @Override
      protected void loadTypeConverters(final Collection<TypeConverter> converters) {
        converters.add(new HistoricalTimeSeriesConverter());
        converters.add(new LocalDateDoubleTimeSeriesConverter());
      }
    });
  }

}
