/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.vannavolga;

import static com.opengamma.financial.analytics.model.YieldCurveFunctionUtils.getCurveRequirement;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.CALL_CURVE_CALC_CONFIG;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackFunction.PUT_CURVE_CALC_CONFIG;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionFunctionUtils.getCurveForCurrency;
import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionFunctionUtils.getSurfaceRequirement;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureVannaVolgaDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParameters;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
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
 *
 */
public abstract class FXOptionVannaVolgaFunction extends AbstractFunction.NonCompiledInvoker {
  /** Property name for the delta to use */
  public static final String PROPERTY_OTM_DELTA = "DeltaOTM";
  /** The name of the calculation method */
  public static final String VANNA_VOLGA_METHOD = "VannaVolgaMethod";
  private static final Logger s_logger = LoggerFactory.getLogger(FXOptionVannaVolgaFunction.class);
  private final String _valueRequirementName;

  public FXOptionVannaVolgaFunction(final String valueRequirementName) {
    ArgumentChecker.notNull(valueRequirementName, "value requirement name");
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = getResultProperties(target);
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
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
      s_logger.error("Need one surface name; have " + surfaceNames);
    }
    final Set<String> deltas = constraints.getValues(PROPERTY_OTM_DELTA);
    if (deltas == null || deltas.size() != 1) {
      return null;
    }
    final String surfaceName = Iterables.getOnlyElement(surfaceNames);
    final String interpolatorName = Iterables.getOnlyElement(interpolatorNames);
    final String leftExtrapolatorName = Iterables.getOnlyElement(leftExtrapolatorNames);
    final String rightExtrapolatorName = Iterables.getOnlyElement(rightExtrapolatorNames);
    final String putCurveName = Iterables.getOnlyElement(putCurveNames);
    final String putCurveCalculationConfig = Iterables.getOnlyElement(putCurveCalculationConfigs);
    final String callCurveName = Iterables.getOnlyElement(callCurveNames);
    final String callCurveCalculationConfig = Iterables.getOnlyElement(callCurveCalculationConfigs);
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency putCurrency = security.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = security.accept(ForexVisitors.getCallCurrencyVisitor());
    final ValueRequirement putFundingCurve = getCurveRequirement(ComputationTargetSpecification.of(putCurrency), putCurveName, putCurveCalculationConfig);
    final ValueRequirement callFundingCurve = getCurveRequirement(ComputationTargetSpecification.of(callCurrency), callCurveName, callCurveCalculationConfig);
    final ValueRequirement fxVolatilitySurface = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final UnorderedCurrencyPair currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency);
    final ValueRequirement spotRequirement = ConventionBasedFXRateFunction.getSpotRateRequirement(currencyPair);
    final ValueRequirement pairQuoteRequirement = new ValueRequirement(ValueRequirementNames.CURRENCY_PAIRS, ComputationTargetType.PRIMITIVE, currencyPair.getUniqueId());
    return Sets.newHashSet(putFundingCurve, callFundingCurve, fxVolatilitySurface, spotRequirement, pairQuoteRequirement);
  }

  protected abstract ValueProperties getResultProperties(ComputationTarget target);

  protected abstract ValueProperties getResultProperties(ComputationTarget target, ValueRequirement desiredValue, CurrencyPair baseQuotePair);

  protected ValueSpecification getSpecification(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final ValueProperties properties = getResultProperties(target, desiredValue, baseQuotePair);
    return new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
  }

  protected String[] getCurveNames(final Currency putCurrency, final String putCurveName, final Currency callCurrency, final String callCurveName,
      final CurrencyPairs baseQuotePairs) {
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      return new String[] {fullPutCurveName, fullCallCurveName };
    }
    return new String[] {fullCallCurveName, fullPutCurveName };
  }

  protected InstrumentDerivative getDerivative(final FinancialSecurity security, final String[] allCurveNames, final CurrencyPairs baseQuotePairs, final ZonedDateTime now) {
    final InstrumentDefinition<?> definition = security.accept(new ForexSecurityConverter(baseQuotePairs));
    return definition.toDerivative(now);
  }

  protected SmileDeltaTermStructureVannaVolgaDataBundle getSmiles(final Currency putCurrency, final Currency callCurrency, final String[] allCurveNames,
      final CurrencyPairs baseQuotePairs, final String deltaName, final FunctionInputs inputs) {
    final double delta = Double.parseDouble(deltaName);
    if (Double.compare(delta, 0.5) == 0) {
      throw new OpenGammaRuntimeException("Asking for OTM smile at delta = 50; this is the ATM value");
    }
    final Object volatilitySurfaceObject = inputs.getValue(ValueRequirementNames.STANDARD_VOLATILITY_SURFACE_DATA);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get volatility surface data");
    }
    final SmileDeltaTermStructureParameters allSmiles = (SmileDeltaTermStructureParameters) volatilitySurfaceObject;
    final double[] uniqueDeltas = allSmiles.getDelta();
    final int putDeltaIndex = Arrays.binarySearch(uniqueDeltas, delta);
    if (putDeltaIndex < 0) {
      throw new OpenGammaRuntimeException("Could not get OTM smile for delta = " + delta);
    }
    final SmileDeltaParameters[] surface = new SmileDeltaParameters[allSmiles.getNumberExpiration()];
    final int atmIndex = (allSmiles.getNumberStrike() - 1) / 2;
    final double[] deltas = {delta };
    for (int i = 0; i < allSmiles.getNumberExpiration(); i++) {
      final SmileDeltaParameters parameters = allSmiles.getSmileForTime(i);
      final double timeToExpiration = parameters.getTimeToExpiry();
      final double[] volatilities = new double[3];
      final double[] allVolatilities = parameters.getVolatility();
      volatilities[0] = allVolatilities[putDeltaIndex];
      volatilities[1] = allVolatilities[atmIndex];
      volatilities[2] = allVolatilities[2 * atmIndex - putDeltaIndex];
      surface[i] = new SmileDeltaParameters(timeToExpiration, deltas, volatilities);
    }
    final Interpolator1D interpolator = allSmiles.getTimeInterpolator();
    final SmileDeltaTermStructureParameters resultSmiles = new SmileDeltaTermStructureParameters(surface, interpolator);
    final Currency ccy1;
    final Currency ccy2;
    final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot requirement");
    }
    final double spot = (Double) spotObject;
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    YieldAndDiscountCurve[] curves;
    final YieldAndDiscountCurve putFundingCurve = getCurveForCurrency(inputs, putCurrency);
    final YieldAndDiscountCurve callFundingCurve = getCurveForCurrency(inputs, callCurrency);
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, callFundingCurve };
      curveCurrency.put(allCurveNames[0], putCurrency);
      curveCurrency.put(allCurveNames[1], callCurrency);
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, putFundingCurve };
      ccy1 = callCurrency;
      ccy2 = putCurrency;
      curveCurrency.put(allCurveNames[1], putCurrency);
      curveCurrency.put(allCurveNames[0], callCurrency);
    }
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final YieldCurveBundle curvesWithFX = new YieldCurveBundle(fxMatrix, curveCurrency, yieldCurves.getCurvesMap());
    final Pair<Currency, Currency> ccyPair = Pairs.of(ccy1, ccy2);
    return new SmileDeltaTermStructureVannaVolgaDataBundle(curvesWithFX, resultSmiles, ccyPair);
  }

  protected String getValueRequirementName() {
    return _valueRequirementName;
  }
}
