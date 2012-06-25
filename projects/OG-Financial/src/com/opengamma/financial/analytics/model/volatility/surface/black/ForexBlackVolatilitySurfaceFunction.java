/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_METHOD;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class ForexBlackVolatilitySurfaceFunction extends BlackVolatilitySurfaceFunction {

  @Override
  protected boolean isCorrectIdType(final ComputationTarget target) {
    if (target.getUniqueId() == null) {
      return false;
    }
    return UnorderedCurrencyPair.OBJECT_SCHEME.equals(target.getUniqueId().getScheme());
  }

  @Override
  protected SmileSurfaceDataBundle getData(final FunctionInputs inputs, final ValueRequirement volatilityDataRequirement, final ValueRequirement forwardCurveRequirement) {
    final Object volatilitySurfaceObject = inputs.getValue(volatilityDataRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + volatilityDataRequirement);
    }
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    return ForexSurfaceUtils.getDataFromStrangleRiskReversalQuote(forwardCurve, fxVolatilitySurface);
  }

  @Override
  protected ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveCalculationMethodName = desiredValue.getConstraint(CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE_CALCULATION_METHOD, curveCalculationMethodName)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  @Override
  protected String getSurfaceQuoteUnits() {
    return SurfaceAndCubePropertyNames.VOLATILITY_QUOTE;
  }

  @Override
  protected String getSurfaceQuoteType() {
    return SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL;
  }

  /**
   * Spline interpolator function for Black volatility surfaces
   */
  public static class Spline extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfaceUtils.ensureSplineVolatilityInterpolatorProperties(desiredValue.getConstraints());
      if (specificRequirements == null) {
        return null;
      }
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.addAll(specificRequirements);
      return requirements;
    }

    @Override
    protected ValueProperties getResultProperties() {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addSplineVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }

  /**
   * SABR interpolator function for Black volatility surfaces
   */
  public static class SABR extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfaceUtils.ensureSABRVolatilityInterpolatorProperties(desiredValue.getConstraints());
      if (specificRequirements == null) {
        return null;
      }
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.addAll(specificRequirements);
      return requirements;
    }

    @Override
    protected ValueProperties getResultProperties() {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addSABRVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addSABRVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }

  /**
   * Mixed log-normal interpolator for Black volatility surface
   */
  public static class MixedLogNormal extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfaceUtils.ensureMixedLogNormalVolatilityInterpolatorProperties(desiredValue.getConstraints());
      if (specificRequirements == null) {
        return null;
      }
      final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
      if (requirements == null) {
        return null;
      }
      requirements.addAll(specificRequirements);
      return requirements;
    }

    @Override
    protected ValueProperties getResultProperties() {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addMixedLogNormalVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfaceUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfaceUtils.addMixedLogNormalVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }
}
