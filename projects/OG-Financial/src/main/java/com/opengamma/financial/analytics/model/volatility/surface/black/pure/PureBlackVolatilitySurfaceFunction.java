/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.pure;

import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.EQUITY_OPTION;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames.PRICE_QUOTE;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.equity.variance.pricing.EquityVolatilityToPureVolatilitySurfaceConverter;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.volatility.surface.PureImpliedVolatilitySurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurfaceInterpolator;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceUtils;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public abstract class PureBlackVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Spline interpolator function for pure Black volatility surfaces
   */
  public static class Spline extends PureBlackVolatilitySurfaceFunction {

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
      return BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(createValueProperties().get())
          .withAny(SURFACE)
          .withAny(CURVE)
          .withAny(CURVE_CALCULATION_CONFIG).get();
    }

    @Override
    protected ValueProperties getResultProperties(final ValueRequirement desiredValue) {
      return BlackVolatilitySurfacePropertyUtils.addSplineVolatilityInterpolatorProperties(desiredValue.getConstraints(), desiredValue).get();
    }
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final Object spotObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot value");
    }
    final Object curveObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE);
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get yield curve");
    }
    final Object volatilitySurfaceObject = inputs.getValue(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface");
    }
    final Object interpolatorObject = inputs.getValue(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR);
    if (interpolatorObject == null) {
      throw new OpenGammaRuntimeException("Could not get surface interpolator");
    }
    final double spot = (Double) spotObject;
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    final AffineDividends dividends = null;
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> volatilitySurfaceData = (VolatilitySurfaceData<Object, Object>) volatilitySurfaceObject;
    final double[] expiries = BlackVolatilitySurfaceUtils.getUniqueExpiries(volatilitySurfaceData);
    final double[] uniqueStrikes = BlackVolatilitySurfaceUtils.getUniqueStrikes(volatilitySurfaceData);
    final Pair<double[][], double[][]> strikesAndValues = BlackVolatilitySurfaceUtils.getStrikesAndValues(expiries, uniqueStrikes, volatilitySurfaceData);
    final double[][] strikes = strikesAndValues.getFirst();
    final double[][] prices = strikesAndValues.getSecond();
    final VolatilitySurfaceInterpolator surfaceInterpolator = (VolatilitySurfaceInterpolator) interpolatorObject;
    final PureImpliedVolatilitySurface pureSurface = EquityVolatilityToPureVolatilitySurfaceConverter.getConvertedSurface(spot, curve, dividends, expiries, strikes, prices,
        surfaceInterpolator);
    final ValueProperties properties = getResultProperties(desiredValue);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.PURE_VOLATILITY_SURFACE, target.toSpecification(), properties), pureSurface));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.EQUITY_VARIANCE_SWAP_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY).or(FinancialSecurityTypes.EQUITY_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PURE_VOLATILITY_SURFACE, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> curveNames = constraints.getValues(CURVE);
    if (curveNames == null || curveNames.size() != 1) {
      return null;
    }
    final Set<String> curveCalculationConfigs = constraints.getValues(CURVE_CALCULATION_CONFIG);
    if (curveCalculationConfigs == null || curveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final String surfaceName = Iterables.getOnlyElement(surfaceNames);
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(currency, desiredValue);
    final ValueRequirement volatilitySurfaceRequirement = getVolatilityDataRequirement(targetSpec, surfaceName);
    // ValueRequirement dividendsRequirement = getDividendRequirement(targetSpec, desiredValue); //TODO include
    final ValueRequirement interpolatorRequirement = getInterpolatorRequirement(targetSpec, desiredValue);
    return Sets.newHashSet(forwardCurveRequirement, volatilitySurfaceRequirement, interpolatorRequirement);
  }

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

  private ValueRequirement getVolatilityDataRequirement(final ComputationTargetSpecification target, final String surfaceName) {
    final ValueProperties properties = ValueProperties.builder()
        .with(SURFACE, surfaceName)
        .with(PROPERTY_SURFACE_INSTRUMENT_TYPE, EQUITY_OPTION)
        .with(PROPERTY_SURFACE_QUOTE_TYPE, CALL_AND_PUT_STRIKE)
        .with(PROPERTY_SURFACE_UNITS, PRICE_QUOTE).get();
    return new ValueRequirement(ValueRequirementNames.VOLATILITY_SURFACE_DATA, target, properties);
  }

  private ValueRequirement getCurveRequirement(final Currency currency, final ValueRequirement desiredValue) {
    final String discountingCurve = desiredValue.getConstraint(CURVE);
    final String curveCalculationConfig = desiredValue.getConstraint(CURVE_CALCULATION_CONFIG);
    final ValueProperties properties = ValueProperties.builder()
        .with(CURVE, discountingCurve)
        .with(CURVE_CALCULATION_CONFIG, curveCalculationConfig).get();
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, currency.getUniqueId(), properties);
  }

  private ValueRequirement getInterpolatorRequirement(final ComputationTargetSpecification target, final ValueRequirement desiredValue) {
    final ValueProperties properties = BlackVolatilitySurfacePropertyUtils.addVolatilityInterpolatorProperties(ValueProperties.builder().get(), desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, target, properties);
  }

  @SuppressWarnings("unused")
  private ValueRequirement getDividendRequirement(final ComputationTargetSpecification target, final ValueRequirement desiredValue) {
    //TODO implement
    return null;
  }
}
