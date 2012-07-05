/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating an {@link OpenGammaExecutionContext}.
 */
public class OpenGammaExecutionContextFactoryBean extends SingletonFactoryBean<FunctionExecutionContext> {

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private HistoricalTimeSeriesResolver _historicalTimeSeriesResolver;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RegionSource _regionSource;
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionBundleSource;
  private ExchangeSource _exchangeSource;
  private ConfigSource _configSource;
  private OverrideOperationCompiler _overrideOperationCompiler;

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(HistoricalTimeSeriesSource source) {
    _historicalTimeSeriesSource = source;
  }

  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _historicalTimeSeriesResolver;
  }

  public void setHistoricalTimeSeriesResolver(final HistoricalTimeSeriesResolver historicalTimeSeriesResolver) {
    _historicalTimeSeriesResolver = historicalTimeSeriesResolver;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public void setPositionSource(PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setRegionSource(RegionSource regionRepository) {
    _regionSource = regionRepository;
  }

  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  public void setExchangeSource(ExchangeSource exchangeSource) {
    _exchangeSource = exchangeSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  public void setConventionBundleSource(ConventionBundleSource referenceRateRepository) {
    _conventionBundleSource = referenceRateRepository;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(ConfigSource configSource) {
    _configSource = configSource;
  }

  public OverrideOperationCompiler getOverrideOperationCompiler() {
    return _overrideOperationCompiler;
  }

  public void setOverrideOperationCompiler(final OverrideOperationCompiler overrideOperationCompiler) {
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  @Override
  protected FunctionExecutionContext createObject() {
    FunctionExecutionContext context = new FunctionExecutionContext();
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    if (getHistoricalTimeSeriesSource() != null) {
      OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    }
    if (getHistoricalTimeSeriesResolver() != null) {
      OpenGammaExecutionContext.setHistoricalTimeSeriesResolver(context, getHistoricalTimeSeriesResolver());
    }
    if (getRegionSource() != null) {
      OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    }
    if (getExchangeSource() != null) {
      OpenGammaExecutionContext.setExchangeSource(context, getExchangeSource());
    }
    if (getHolidaySource() != null) {
      OpenGammaExecutionContext.setHolidaySource(context, getHolidaySource());
    }
    if (getConventionBundleSource() != null) {
      OpenGammaExecutionContext.setConventionBundleSource(context, getConventionBundleSource());
    }
    if (getConfigSource() != null) {
      OpenGammaExecutionContext.setConfigSource(context, getConfigSource());
    }
    if (getOverrideOperationCompiler() != null) {
      OpenGammaExecutionContext.setOverrideOperationCompiler(context, getOverrideOperationCompiler());
    }
    return context;
  }

}
