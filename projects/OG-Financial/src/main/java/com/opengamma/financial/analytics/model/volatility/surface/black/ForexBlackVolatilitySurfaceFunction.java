/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr.SmileSurfaceDataBundle;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class ForexBlackVolatilitySurfaceFunction extends BlackVolatilitySurfaceFunction {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR;
  }

  /**
   * Spline interpolator function for Black volatility surfaces
   */
  public static class Spline extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfacePropertyUtils.ensureSplineVolatilityInterpolatorProperties(desiredValue.getConstraints());
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
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }

  /**
   * SABR interpolator function for Black volatility surfaces
   */
  public static class SABR extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfacePropertyUtils.ensureSABRVolatilityInterpolatorProperties(desiredValue.getConstraints());
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
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addSABRVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addSABRVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }

  /**
   * Mixed log-normal interpolator for Black volatility surface
   */
  public static class MixedLogNormal extends ForexBlackVolatilitySurfaceFunction {

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final Set<ValueRequirement> specificRequirements = BlackVolatilitySurfacePropertyUtils.ensureMixedLogNormalVolatilityInterpolatorProperties(desiredValue.getConstraints());
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
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addMixedLogNormalVolatilityInterpolatorProperties(properties).get(), getInstrumentType()).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      final ValueProperties properties = createValueProperties().get();
      return BlackVolatilitySurfacePropertyUtils.addBlackSurfaceProperties(
          BlackVolatilitySurfacePropertyUtils.addMixedLogNormalVolatilityInterpolatorProperties(properties, desiredValue).get(), getInstrumentType(), desiredValue).get();
    }

  }

  @Override
  protected SmileSurfaceDataBundle getData(final FunctionInputs inputs) {
    final Object volatilitySurfaceObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    final Object forwardCurveObject = inputs.getValue(ValueRequirementNames.FORWARD_CURVE);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get forward curve");
    }
    final ForwardCurve forwardCurve = (ForwardCurve) forwardCurveObject;
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>> fxVolatilitySurface = (VolatilitySurfaceData<Tenor, Pair<Number, FXVolQuoteType>>) volatilitySurfaceObject;
    return BlackVolatilitySurfaceUtils.getDataFromStrangleRiskReversalQuote(forwardCurve, fxVolatilitySurface);
  }

  @Override
  protected ValueRequirement getForwardCurveRequirement(final ComputationTarget target, final ValueRequirement desiredValue) {
    final String curveCalculationMethodName = desiredValue.getConstraint(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD);
    final String forwardCurveName = desiredValue.getConstraint(CURVE);
    final ValueProperties properties = ValueProperties.builder()
        .with(ForwardCurveValuePropertyNames.PROPERTY_FORWARD_CURVE_CALCULATION_METHOD, curveCalculationMethodName)
        .with(CURVE, forwardCurveName).get();
    return new ValueRequirement(ValueRequirementNames.FORWARD_CURVE, target.toSpecification(), properties);
  }

  @Override
  protected ValueRequirement getVolatilityDataRequirement(final ComputationTarget target, final String surfaceName) {
    final ValueRequirement volDataRequirement = new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target.toSpecification(),
        ValueProperties.builder()
        .with(SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, getInstrumentType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE, getSurfaceQuoteType())
        .with(SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS, getSurfaceQuoteUnits()).get());
    return volDataRequirement;
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  private String getSurfaceQuoteUnits() {
    return SurfaceAndCubePropertyNames.VOLATILITY_QUOTE;
  }

  private String getSurfaceQuoteType() {
    return SurfaceAndCubeQuoteType.MARKET_STRANGLE_RISK_REVERSAL;
  }
}
