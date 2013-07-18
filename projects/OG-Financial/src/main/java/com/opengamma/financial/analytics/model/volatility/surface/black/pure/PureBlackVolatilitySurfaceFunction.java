/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.surface.black.pure;

import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PRICE_QUOTE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE;
import static com.opengamma.engine.value.SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CALCULATION_CONFIG;
import static com.opengamma.engine.value.ValuePropertyNames.CURVE_CURRENCY;
import static com.opengamma.engine.value.ValuePropertyNames.SURFACE;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.EQUITY_OPTION;
import static com.opengamma.financial.analytics.model.InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE;
import static com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

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
import com.opengamma.financial.analytics.model.FutureOptionExpiries;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfacePropertyUtils;
import com.opengamma.financial.analytics.model.volatility.surface.black.BlackVolatilitySurfaceUtils;
import com.opengamma.financial.analytics.volatility.surface.FunctionalVolatilitySurfaceData;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Triple;

/**
 *
 */
public abstract class PureBlackVolatilitySurfaceFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property describing the treatment of dividends */
  public static final String PROPERTY_DIVIDEND_TREATMENT = "DividendTreatment";
  private static final String X_LABEL = "Expiry (years)";
  private static final String Y_LABEL = "Moneyness";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final LocalDate date = ZonedDateTime.now(executionContext.getValuationClock()).toLocalDate();
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
    final AffineDividends dividends = getDividends(inputs);
    @SuppressWarnings("unchecked")
    final VolatilitySurfaceData<Object, Object> volatilitySurfaceData = (VolatilitySurfaceData<Object, Object>) volatilitySurfaceObject;
    final Triple<double[], double[][], double[][]> strikesAndValues = BlackVolatilitySurfaceUtils.getStrippedStrikesAndValues(volatilitySurfaceData);
    final double[] expiryNumber = strikesAndValues.getFirst();
    final double[] expiries = getExpiries(expiryNumber, date);
    final double[][] strikes = strikesAndValues.getSecond();
    final double[][] prices = strikesAndValues.getThird();
    final VolatilitySurfaceInterpolator surfaceInterpolator = (VolatilitySurfaceInterpolator) interpolatorObject;
    final PureImpliedVolatilitySurface pureSurface = EquityVolatilityToPureVolatilitySurfaceConverter.getConvertedSurface(spot, curve, dividends, expiries, strikes, prices,
        surfaceInterpolator);
    final FunctionalVolatilitySurfaceData surfaceData = new FunctionalVolatilitySurfaceData(pureSurface, X_LABEL, expiries[0], expiries[expiries.length - 1], 25, Y_LABEL,
        0.25, 1.75, 50, 0, 0.6);
    final ValueProperties properties = getResultProperties(desiredValue);
    return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.PURE_VOLATILITY_SURFACE, target.toSpecification(), properties), surfaceData));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
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
    final Set<String> currencies = constraints.getValues(CURVE_CURRENCY);
    if (currencies == null || currencies.size() != 1) {
      return null;
    }
    final String surfaceName = Iterables.getOnlyElement(surfaceNames) + "_PRICE";
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Currency currency = Currency.of(Iterables.getOnlyElement(currencies));
    final ValueRequirement curveRequirement = getCurveRequirement(currency, desiredValue);
    final ValueRequirement volatilitySurfaceRequirement = getVolatilityDataRequirement(targetSpec, surfaceName);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, targetSpec);
    final ValueRequirement interpolatorRequirement = getInterpolatorRequirement(targetSpec, desiredValue);
    return Sets.newHashSet(curveRequirement, volatilitySurfaceRequirement, spotRequirement, interpolatorRequirement);
  }

  protected abstract ValueProperties getResultProperties();

  protected abstract ValueProperties getResultProperties(final ValueRequirement desiredValue);

  protected abstract AffineDividends getDividends(FunctionInputs inputs);

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
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(currency), properties);
  }

  private ValueRequirement getInterpolatorRequirement(final ComputationTargetSpecification target, final ValueRequirement desiredValue) {
    final ValueProperties properties = BlackVolatilitySurfacePropertyUtils.addVolatilityInterpolatorProperties(ValueProperties.builder().get(), desiredValue).get();
    return new ValueRequirement(ValueRequirementNames.BLACK_VOLATILITY_SURFACE_INTERPOLATOR, target, properties);
  }


  private double[] getExpiries(final double[] expiryNumber, final LocalDate date) {
    final int n = expiryNumber.length;
    final double[] expiries = new double[n];
    for (int i = 0; i < n; i++) {
      expiries[i] = FutureOptionExpiries.EQUITY.getFutureOptionTtm((int) expiryNumber[i], date);
    }
    return expiries;
  }

}
