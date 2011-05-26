/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.PortfolioStructure;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyMatrixSource;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Bean for constructing an {@link OpenGammaCompilationContext}.
 */
public class OpenGammaCompilationContextFactoryBean extends SingletonFactoryBean<FunctionCompilationContext> {

  private ConfigSource _configSource;
  private SecuritySource _securitySource;
  private PositionSource _positionSource;
  private RegionSource _regionSource;
  private ConventionBundleSource _conventionBundleSource;
  private InterpolatedYieldCurveDefinitionSource _interpolatedYieldCurveDefinitionSource;
  private InterpolatedYieldCurveSpecificationBuilder _interpolatedYieldCurveSpecificationBuilder;
  private CurrencyMatrixSource _currencyMatrixSource;
  private HolidaySource _holidaySource;
  private ExchangeSource _exchangeSource;

  public void setSecuritySource(final SecuritySource securitySource) {
    _securitySource = securitySource;
  }

  public SecuritySource getSecuritySource() {
    return _securitySource;
  }

  public void setPositionSource(final PositionSource positionSource) {
    _positionSource = positionSource;
  }

  public PositionSource getPositionSource() {
    return _positionSource;
  }

  public void setRegionSource(final RegionSource regionSource) {
    _regionSource = regionSource;
  }

  public RegionSource getRegionSource() {
    return _regionSource;
  }

  public void setConventionBundleSource(ConventionBundleSource conventionBundleSource) {
    _conventionBundleSource = conventionBundleSource;
  }

  public ConventionBundleSource getConventionBundleSource() {
    return _conventionBundleSource;
  }

  public void setConfigSource(ConfigSource configSource) {
    _configSource = configSource;
  }

  public ConfigSource getConfigSource() {
    return _configSource;
  }

  public InterpolatedYieldCurveDefinitionSource getInterpolatedYieldCurveDefinitionSource() {
    return _interpolatedYieldCurveDefinitionSource;
  }

  public void setInterpolatedYieldCurveDefinitionSource(
      final InterpolatedYieldCurveDefinitionSource interpolatedYieldCurveDefinitionSource) {
    _interpolatedYieldCurveDefinitionSource = interpolatedYieldCurveDefinitionSource;
  }

  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder() {
    return _interpolatedYieldCurveSpecificationBuilder;
  }

  public void setInterpolatedYieldCurveSpecificationBuilder(
      final InterpolatedYieldCurveSpecificationBuilder interpolatedYieldCurveSpecificationBuilder) {
    _interpolatedYieldCurveSpecificationBuilder = interpolatedYieldCurveSpecificationBuilder;
  }

  public CurrencyMatrixSource getCurrencyMatrixSource() {
    return _currencyMatrixSource;
  }

  public void setCurrencyMatrixSource(final CurrencyMatrixSource currencyMatrixSource) {
    _currencyMatrixSource = currencyMatrixSource;
  }

  public HolidaySource getHolidaySource() {
    return _holidaySource;
  }

  public void setHolidaySource(HolidaySource holidaySource) {
    _holidaySource = holidaySource;
  }

  public ExchangeSource getExchangeSource() {
    return _exchangeSource;
  }

  public void setExchangeSource(ExchangeSource exchangeSource) {
    _exchangeSource = exchangeSource;
  }

  private void configureCompilationContext(final FunctionCompilationContext context) {
    OpenGammaCompilationContext.setConfigSource(context, _configSource);
    OpenGammaCompilationContext.setRegionSource(context, _regionSource);
    OpenGammaCompilationContext.setConventionBundleSource(context, _conventionBundleSource);
    OpenGammaCompilationContext.setInterpolatedYieldCurveDefinitionSource(context,
        _interpolatedYieldCurveDefinitionSource);
    OpenGammaCompilationContext.setInterpolatedYieldCurveSpecificationBuilder(context,
        _interpolatedYieldCurveSpecificationBuilder);
    OpenGammaCompilationContext.setCurrencyMatrixSource(context, _currencyMatrixSource);
    OpenGammaCompilationContext.setHolidaySource(context, _holidaySource);
    OpenGammaCompilationContext.setExchangeSource(context, _exchangeSource);
    context.setSecuritySource(getSecuritySource());
    context.setPortfolioStructure(new PortfolioStructure(getPositionSource()));
  }

  @Override
  protected FunctionCompilationContext createObject() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    configureCompilationContext(context);
    return context;
  }

}
