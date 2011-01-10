/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating an {@link OpenGammaExecutionContext}.
 */
public class OpenGammaExecutionContextFactoryBean extends SingletonFactoryBean<FunctionExecutionContext> {

  private HistoricalDataSource _historicalDataSource;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RegionSource _regionSource;
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionBundleSource;
  private ExchangeSource _exchangeSource;
  private ConfigSource _configSource;

  public HistoricalDataSource getHistoricalDataSource() {
    return _historicalDataSource;
  }

  public void setHistoricalDataSource(HistoricalDataSource historicalDataSource) {
    _historicalDataSource = historicalDataSource;
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

  @Override
  protected FunctionExecutionContext createObject() {
    FunctionExecutionContext context = new FunctionExecutionContext();
    context.setSecuritySource(_securitySource);
    context.setPortfolioStructure(new PortfolioStructure(_positionSource));
    OpenGammaExecutionContext.setHistoricalDataSource(context, _historicalDataSource);
    OpenGammaExecutionContext.setRegionSource(context, _regionSource);
    OpenGammaExecutionContext.setExchangeSource(context, _exchangeSource);
    OpenGammaExecutionContext.setHolidaySource(context, _holidaySource);
    OpenGammaExecutionContext.setConventionBundleSource(context, _conventionBundleSource);
    OpenGammaExecutionContext.setConfigSource(context, _configSource);
    return context;
  }

}
