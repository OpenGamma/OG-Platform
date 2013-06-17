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
import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.engine.marketdata.OverrideOperationCompiler;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.ConventionSource;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Factory bean for creating an {@link OpenGammaExecutionContext}.
 */
public class OpenGammaExecutionContextFactoryBean extends SingletonFactoryBean<FunctionExecutionContext> {

  private HistoricalTimeSeriesSource _historicalTimeSeriesSource;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RegionSource _regionSource;
  private HolidaySource _holidaySource;
  private ConventionBundleSource _conventionBundleSource;
  private ExchangeSource _exchangeSource;
  private ConfigSource _configSource;
  private OrganizationSource _organizationSource;
  private ConfigMaster _configMaster;
  private OverrideOperationCompiler _overrideOperationCompiler;
  private ConventionSource _conventionSource;

  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _historicalTimeSeriesSource;
  }

  public void setHistoricalTimeSeriesSource(final HistoricalTimeSeriesSource source) {
    _historicalTimeSeriesSource = source;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setRegionSource(final RegionSource regionRepository) {
    _regionSource = regionRepository;
  }

  public OrganizationSource getOrganizationSource() {
    return _organizationSource;
  }

  public void setOrganizationSource(final OrganizationSource organizationSource) {
    _organizationSource = organizationSource;
  }

  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  public void setExchangeSource(final ExchangeSource exchangeSource) {
    _exchangeSource = exchangeSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(final HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  public void setConventionBundleSource(final ConventionBundleSource referenceRateRepository) {
    _conventionBundleSource = referenceRateRepository;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public void setConfigSource(final ConfigSource configSource) {
    _configSource = configSource;
  }

  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  public void setConfigMaster(final ConfigMaster configMaster) {
    _configMaster = configMaster;
  }

  public OverrideOperationCompiler getOverrideOperationCompiler() {
    return _overrideOperationCompiler;
  }

  public void setOverrideOperationCompiler(final OverrideOperationCompiler overrideOperationCompiler) {
    _overrideOperationCompiler = overrideOperationCompiler;
  }

  public ConventionSource getConventionSource() {
    return _conventionSource;
  }

  public void setConventionSource(final ConventionSource conventionSource) {
    _conventionSource = conventionSource;
  }

  @Override
  protected FunctionExecutionContext createObject() {
    final FunctionExecutionContext context = new FunctionExecutionContext();
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
    if (getHistoricalTimeSeriesSource() != null) {
      OpenGammaExecutionContext.setHistoricalTimeSeriesSource(context, getHistoricalTimeSeriesSource());
    }
    if (getRegionSource() != null) {
      OpenGammaExecutionContext.setRegionSource(context, getRegionSource());
    }
    if (getOrganizationSource() != null) {
      context.setOrganizationSource(getOrganizationSource());
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
    if (getConventionSource() != null) {
      OpenGammaExecutionContext.setConventionSource(context, getConventionSource());
    }
    if (getConfigSource() != null) {
      OpenGammaExecutionContext.setConfigSource(context, getConfigSource());
    }
    if (getConfigMaster() != null) {
      OpenGammaExecutionContext.setConfigMaster(context, getConfigMaster());
    }
    if (getOverrideOperationCompiler() != null) {
      OpenGammaExecutionContext.setOverrideOperationCompiler(context, getOverrideOperationCompiler());
    }
    return context;
  }

}
