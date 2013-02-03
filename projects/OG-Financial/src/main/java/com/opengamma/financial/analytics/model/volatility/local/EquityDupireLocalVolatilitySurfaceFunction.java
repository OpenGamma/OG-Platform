/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyNamesAndValues;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;

/**
 * 
 */
public abstract class EquityDupireLocalVolatilitySurfaceFunction extends DupireLocalVolatilitySurfaceFunction {
  private static final Logger s_logger = LoggerFactory.getLogger(EquityDupireLocalVolatilitySurfaceFunction.class);

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final String targetScheme = target.getUniqueId().getScheme();
    return (targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER.getName()) ||
        targetScheme.equalsIgnoreCase(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE; // Bloomberg ticker or weak ticker
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.EQUITY_OPTION;
  }

  /**
   * Equity requires an additional three properties.
   * This is to specify the Funding curve used to build the Equity Forwards.
   * @return ValueProperties specifying any currency, curve name and curve calculation config
   */
  protected ValueProperties getCurrencyProperties() {
    final ValueProperties equityProperties = createValueProperties()
        .withAny(ValuePropertyNames.CURVE_CURRENCY)
        .withAny(ValuePropertyNames.CURVE)
        .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG)
        .get();
    return equityProperties;
  }

  /**
   * Equity requires an additional three properties.
   * This is to specify the Funding curve used to build the Equity Forwards.
   * @param desiredValue ValueRequirement containing "CurveCurrency" and "FundingCurve"
   * @return ValueProperties containing specified values
   */
  protected ValueProperties getCurrencyProperties(final ValueRequirement desiredValue) {
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String fundingCurve = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ValueProperties equityProperties = createValueProperties()
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(ValuePropertyNames.CURVE, fundingCurve)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    return equityProperties;
  }

  /**
   * Equity requires an additional three properties.
   * This is to specify the Funding curve used to build the Equity Forwards.
   * @param desiredValue ValueRequirement containing "CurveCurrency" and "FundingCurve"
   * @return ValueProperties containing specified values
   */
  protected ValueProperties getCurrencyPropertiesForVolatilitySurface(final ValueRequirement desiredValue) {
    final String curveCurrency = desiredValue.getConstraint(ValuePropertyNames.CURVE_CURRENCY);
    final String fundingCurve = desiredValue.getConstraint(ValuePropertyNames.CURVE);
    final String curveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
    final ValueProperties equityProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE_CURRENCY, curveCurrency)
        .with(ValuePropertyNames.CURVE, fundingCurve)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfig)
        .get();
    return equityProperties;
  }

  @Override
  protected ValueProperties getResultProperties(final String parameterizationType) {
    final ValueProperties equityProperties = getCurrencyProperties();
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(equityProperties,
        getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType).get();
  }


  @Override
  protected ValueProperties getResultProperties(final ValueRequirement desiredValue, final String parameterizationType) {
    final ValueProperties equityProperties = getCurrencyProperties(desiredValue);
    return LocalVolatilitySurfaceUtils.addAllDupireLocalVolatilitySurfaceProperties(equityProperties,
        getInstrumentType(), getBlackSmileInterpolatorName(), parameterizationType, desiredValue).get();
  }

  @Override
  protected ValueRequirement getVolatilitySurfaceRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties equityProperties = getCurrencyPropertiesForVolatilitySurface(desiredValue);
    final ValueProperties properties = BlackVolatilitySurfacePropertyUtils.addAllBlackSurfaceProperties(equityProperties, getInstrumentType(), desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE, target.toSpecification(), properties);
  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with spline interpolation
   */
  public static class Spline extends EquityDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.SPLINE;
    }

  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with SABR interpolation
   */
  public static class SABR extends EquityDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.SABR;
    }

  }

  /**
   * Function producing a local volatility surface using a Black volatility surface with mixed log-normal interpolation
   */
  public static class MixedLogNormal extends EquityDupireLocalVolatilitySurfaceFunction {

    @Override
    protected String getBlackSmileInterpolatorName() {
      return BlackVolatilitySurfacePropertyNamesAndValues.MIXED_LOG_NORMAL;
    }

  }
}
