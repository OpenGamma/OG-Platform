/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import static com.opengamma.financial.analytics.model.forex.option.black.FXOptionFunctionUtils.getSurfaceRequirement;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_DAYS_TO_MOVE_FORWARD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.PROPERTY_THETA_CALCULATION_METHOD;
import static com.opengamma.financial.analytics.model.horizon.ThetaPropertyNamesAndValues.THETA_CONSTANT_SPREAD_VOLATILITY_SURFACE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.Clock;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.horizon.VolatilitySurfaceConstantSpreadCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.volatility.surface.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.model.InterpolatedDataProperties;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackMultiValuedFunction;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionFunctionUtils;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
public class FXOptionBlackVolatilitySurfaceConstantSpreadThetaFunction extends FXOptionBlackMultiValuedFunction {
  private static final VolatilitySurfaceConstantSpreadCalculator CALCULATOR = VolatilitySurfaceConstantSpreadCalculator.getInstance();

  public FXOptionBlackVolatilitySurfaceConstantSpreadThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
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
    final String daysForward = desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
    final Object baseQuotePairsObject = inputs.getValue(ValueRequirementNames.CURRENCY_PAIRS);
    if (baseQuotePairsObject == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair data");
    }
    final CurrencyPairs baseQuotePairs = (CurrencyPairs) baseQuotePairsObject;
    final CurrencyPair baseQuotePair = baseQuotePairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote pair for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    final YieldAndDiscountCurve putFundingCurve = FXOptionFunctionUtils.getCurveForCurrency(inputs, putCurrency, putCurveName, putCurveConfig);
    final YieldAndDiscountCurve callFundingCurve = FXOptionFunctionUtils.getCurveForCurrency(inputs, callCurrency, callCurveName, callCurveConfig);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<>();
    curveCurrency.put(fullPutCurveName, putCurrency);
    curveCurrency.put(fullCallCurveName, callCurrency);
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    if (baseQuotePair.getBase().equals(putCurrency)) { // To get Base/quote in market standard order.
      ccy1 = putCurrency;
      ccy2 = callCurrency;
      curves = new YieldAndDiscountCurve[] {putFundingCurve, callFundingCurve};
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {callFundingCurve, putFundingCurve};
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName};
      ccy1 = callCurrency;
      ccy2 = putCurrency;
    }
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final Object spotObject = inputs.getValue(ValueRequirementNames.SPOT_RATE);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot requirement");
    }
    final double spot = (Double) spotObject;
    final ValueRequirement fxVolatilitySurfaceRequirement = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation smiles = (SmileDeltaTermStructureParametersStrikeInterpolation) volatilitySurfaceObject;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final ValueProperties.Builder properties = getResultProperties(target, desiredValue, baseQuotePair);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get());
    final YieldCurveBundle curvesWithFX = new YieldCurveBundle(fxMatrix, curveCurrency, yieldCurves.getCurvesMap());
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(curvesWithFX, smiles, Pairs.of(ccy1, ccy2));
    final ForexSecurityConverter converter = new ForexSecurityConverter(baseQuotePairs);
    final ForexOptionVanillaDefinition definition = (ForexOptionVanillaDefinition) security.accept(converter);
    final MultipleCurrencyAmount theta = CALCULATOR.getTheta(definition, now, allCurveNames, smileBundle, Integer.parseInt(daysForward));
    return Collections.singleton(new ComputedValue(spec, HorizonUtils.getNonZeroValue(theta)));
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Set<String> daysForwardNames = desiredValue.getConstraints().getValues(PROPERTY_DAYS_TO_MOVE_FORWARD);
    if (daysForwardNames == null || daysForwardNames.size() != 1) {
      return null;
    }
    return super.getRequirements(context, target, desiredValue);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    throw new NotImplementedException("Should never get here");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target)
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD_VOLATILITY_SURFACE)
        .withAny(PROPERTY_DAYS_TO_MOVE_FORWARD);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final ValueRequirement desiredValue, final CurrencyPair baseQuotePair) {
    final String daysForward = desiredValue.getConstraint(PROPERTY_DAYS_TO_MOVE_FORWARD);
    final ValueProperties.Builder properties = super.getResultProperties(target, desiredValue, baseQuotePair)
        .with(PROPERTY_THETA_CALCULATION_METHOD, THETA_CONSTANT_SPREAD_VOLATILITY_SURFACE)
        .with(PROPERTY_DAYS_TO_MOVE_FORWARD, daysForward);
    return properties;
  }
}
