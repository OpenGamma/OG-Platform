/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackForexTermStructureBundle;
import com.opengamma.analytics.financial.model.volatility.curve.BlackForexTermStructureParameters;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ConventionBasedFXRateFunction;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * @deprecated This function uses deprecated functions from the analytics library.
 */
@Deprecated
public abstract class FXOptionBlackTermStructureFunction extends AbstractFunction.NonCompiledInvoker {
  /** The name of the calculation method */
  public static final String BLACK_TERM_STRUCTURE_METHOD = "BlackTermStructure";
  private final String _valueRequirementName;

  public FXOptionBlackTermStructureFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = ZonedDateTime.now(snapshotClock);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String putCurveName = desiredValue.getConstraint(PUT_CURVE);
    final String callCurveName = desiredValue.getConstraint(CALL_CURVE);
    final String surfaceName = desiredValue.getConstraint(ValuePropertyNames.SURFACE);
    final String putCurveConfig = desiredValue.getConstraint(PUT_CURVE_CALC_CONFIG);
    final String callCurveConfig = desiredValue.getConstraint(CALL_CURVE_CALC_CONFIG);
    final String interpolatorName = desiredValue.getConstraint(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    final String leftExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    final String rightExtrapolatorName = desiredValue.getConstraint(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    final YieldAndDiscountCurve putFundingCurve = getCurve(inputs, putCurrency, putCurveName, putCurveConfig);
    final YieldAndDiscountCurve callFundingCurve = getCurve(inputs, callCurrency, callCurveName, callCurveConfig);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<>();
    curveCurrency.put(fullPutCurveName, putCurrency);
    curveCurrency.put(fullCallCurveName, callCurrency);
    final InstrumentDefinition<?> definition = security.accept(new ForexSecurityConverter(baseQuotePairs));
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot requirement");
    }
    final double spot; // = (Double) spotObject;
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, callFundingCurve };
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName };
      spot = (Double) spotObject;
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, putFundingCurve };
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName };
      ccy1 = callCurrency;
      ccy2 = putCurrency;
      spot = 1. / (Double) spotObject;
    }
    final InstrumentDerivative fxOption = definition.toDerivative(now);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final ValueRequirement fxVolatilitySurfaceRequirement = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final ValueProperties.Builder properties = getResultProperties(target, desiredValue, baseQuotePair);
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get());
    final YieldCurveBundle curvesWithFX = new YieldCurveBundle(fxMatrix, curveCurrency, yieldCurves.getCurvesMap());
    final Pair<Currency, Currency> currencyPair = Pairs.of(ccy1, ccy2);
    BlackForexTermStructureParameters termStructure;
    if (volatilitySurfaceObject instanceof SmileDeltaTermStructureParametersStrikeInterpolation) {
      final SmileDeltaTermStructureParametersStrikeInterpolation smiles = (SmileDeltaTermStructureParametersStrikeInterpolation) volatilitySurfaceObject;
      termStructure = smiles.toTermStructureOnlyData(interpolator);
    } else {
      termStructure = (BlackForexTermStructureParameters) volatilitySurfaceObject;
    }
    final YieldCurveWithBlackForexTermStructureBundle flatData = new YieldCurveWithBlackForexTermStructureBundle(curvesWithFX, termStructure, currencyPair);
    return getResult(fxOption, flatData, target, desiredValues, inputs, spec, executionContext);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY.or(FinancialSecurityTypes.FX_BARRIER_OPTION_SECURITY).or(FinancialSecurityTypes.FX_DIGITAL_OPTION_SECURITY)
        .or(FinancialSecurityTypes.NON_DELIVERABLE_FX_OPTION_SECURITY).or(FinancialSecurityTypes.NON_DELIVERABLE_FX_DIGITAL_OPTION_SECURITY);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties.Builder properties = getResultProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> putCurveNames = constraints.getValues(PUT_CURVE);
    if (putCurveNames == null || putCurveNames.size() != 1) {
      return null;
    }
    final Set<String> callCurveNames = constraints.getValues(CALL_CURVE);
    if (callCurveNames == null || callCurveNames.size() != 1) {
      return null;
    }
    final Set<String> putCurveCalculationConfigs = constraints.getValues(PUT_CURVE_CALC_CONFIG);
    if (putCurveCalculationConfigs == null || putCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> callCurveCalculationConfigs = constraints.getValues(CALL_CURVE_CALC_CONFIG);
    if (callCurveCalculationConfigs == null || callCurveCalculationConfigs.size() != 1) {
      return null;
    }
    final Set<String> surfaceNames = constraints.getValues(ValuePropertyNames.SURFACE);
    if (surfaceNames == null || surfaceNames.size() != 1) {
      return null;
    }
    final Set<String> interpolatorNames = constraints.getValues(InterpolatedDataProperties.X_INTERPOLATOR_NAME);
    if (interpolatorNames == null || interpolatorNames.size() != 1) {
      return null;
    }
    final Set<String> leftExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME);
    if (leftExtrapolatorNames == null || leftExtrapolatorNames.size() != 1) {
      return null;
    }
    final Set<String> rightExtrapolatorNames = constraints.getValues(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME);
    if (rightExtrapolatorNames == null || rightExtrapolatorNames.size() != 1) {
      return null;
    }
    final String putCurveName = putCurveNames.iterator().next();
    final String callCurveName = callCurveNames.iterator().next();
    final String putCurveCalculationConfig = putCurveCalculationConfigs.iterator().next();
    final String callCurveCalculationConfig = callCurveCalculationConfigs.iterator().next();
    final String surfaceName = surfaceNames.iterator().next();
    final String interpolatorName = interpolatorNames.iterator().next();
    final String leftExtrapolatorName = leftExtrapolatorNames.iterator().next();
    final String rightExtrapolatorName = rightExtrapolatorNames.iterator().next();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(putCurveName, putCurrency, putCurveCalculationConfig);
    final ValueRequirement callFundingCurve = getCurveRequirement(callCurveName, callCurrency, callCurveCalculationConfig);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement spotRequirement = ConventionBasedFXRateFunction.getSpotRateRequirement(currencyPair);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetSpecification.NULL);
    return Sets.newHashSet(putFundingCurve, callFundingCurve, fxVolatilitySurface, spotRequirement, pairQuoteRequirement);
  }

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target);

  protected abstract ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair);

  //TODO clumsy. Push the execute() method down into the functions and have getDerivative() and getData() methods
  protected abstract Set<ComputedValue> getResult(final InstrumentDerivative forex, final YieldCurveWithBlackForexTermStructureBundle data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext);

  protected static ValueRequirement getCurveRequirement(final String curveName, final Currency currency, final String curveCalculationConfigName) {
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.CURVE, curveName)
        .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveCalculationConfigName);
    return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.CURRENCY.specification(currency), properties.get());
  }

  protected static ValueRequirement getSurfaceRequirement(final String surfaceName, final Currency putCurrency, final Currency callCurrency,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    final ValueProperties surfaceProperties = ValueProperties.builder()
        .with(ValuePropertyNames.SURFACE, surfaceName)
        .with(InstrumentTypeProperties.PROPERTY_SURFACE_INSTRUMENT_TYPE, InstrumentTypeProperties.FOREX)
        .with(InterpolatedDataProperties.X_INTERPOLATOR_NAME, interpolatorName)
        .with(InterpolatedDataProperties.LEFT_X_EXTRAPOLATOR_NAME, leftExtrapolatorName)
        .with(InterpolatedDataProperties.RIGHT_X_EXTRAPOLATOR_NAME, rightExtrapolatorName)
        .get();
    final UnorderedCurrencyPair currenciesTarget = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    return new ValueRequirement(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA, ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(currenciesTarget), surfaceProperties);
  }

  protected static YieldAndDiscountCurve getCurve(final FunctionInputs inputs, final Currency currency, final String curveName, final String curveCalculationConfig) {
    final Object curveObject = inputs.getValue(getCurveRequirement(curveName, currency, curveCalculationConfig));
    if (curveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + curveName + " curve");
    }
    final YieldAndDiscountCurve curve = (YieldAndDiscountCurve) curveObject;
    return curve;
  }
}
