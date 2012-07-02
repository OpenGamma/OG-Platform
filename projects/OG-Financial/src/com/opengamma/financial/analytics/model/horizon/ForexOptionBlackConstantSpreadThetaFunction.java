/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.horizon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.analytics.financial.forex.definition.ForexOptionDigitalDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureParametersStrikeInterpolation;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
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
import com.opengamma.financial.analytics.model.forex.option.black.ForexOptionBlackMultiValuedFunction;
import com.opengamma.financial.analytics.model.horizon.deprecated.InterestRateFutureConstantSpreadThetaFunctionDeprecated;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class ForexOptionBlackConstantSpreadThetaFunction extends ForexOptionBlackMultiValuedFunction {
  private static final ForexSecurityConverter VISITOR = new ForexSecurityConverter();
  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties

  public ForexOptionBlackConstantSpreadThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
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
    final String fullPutCurveName = putCurveName + "_" + putCurrency.getCode();
    final String fullCallCurveName = callCurveName + "_" + callCurrency.getCode();
    final YieldAndDiscountCurve putFundingCurve = getCurve(inputs, putCurrency, putCurveName, putCurveConfig);
    final YieldAndDiscountCurve callFundingCurve = getCurve(inputs, callCurrency, callCurveName, callCurveConfig);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    curveCurrency.put(fullPutCurveName, putCurrency);
    curveCurrency.put(fullCallCurveName, callCurrency);
    final String[] allCurveNames;
    final Currency ccy1;
    final Currency ccy2;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) { // To get Base/quote in market standard order.
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
    final ValueRequirement spotRequirement = security.accept(ForexVisitors.getSpotIdentifierVisitor());
    final Object spotObject = inputs.getValue(spotRequirement);
    if (spotObject == null) {
      throw new OpenGammaRuntimeException("Could not get spot requirement " + spotRequirement);
    }
    final double spot = (Double) spotObject;
    final ValueRequirement fxVolatilitySurfaceRequirement = getSurfaceRequirement(surfaceName, putCurrency, callCurrency, interpolatorName, leftExtrapolatorName, rightExtrapolatorName);
    final Object volatilitySurfaceObject = inputs.getValue(fxVolatilitySurfaceRequirement);
    if (volatilitySurfaceObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + fxVolatilitySurfaceRequirement);
    }
    final SmileDeltaTermStructureParametersStrikeInterpolation smiles = (SmileDeltaTermStructureParametersStrikeInterpolation) volatilitySurfaceObject;
    final FXMatrix fxMatrix = new FXMatrix(ccy1, ccy2, spot);
    final ValueProperties.Builder properties = getResultProperties(putCurveName, callCurveName, putCurveConfig, callCurveConfig, surfaceName, interpolatorName,
        leftExtrapolatorName, rightExtrapolatorName, target);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get());
    final SmileDeltaTermStructureDataBundle smileBundle = new SmileDeltaTermStructureDataBundle(fxMatrix, curveCurrency, yieldCurves, smiles, Pair.of(ccy1, ccy2));
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    if (security instanceof FXOptionSecurity) {
      final ForexOptionVanillaDefinition definition = (ForexOptionVanillaDefinition) security.accept(VISITOR);
      final MultipleCurrencyAmount theta = calculator.getTheta(definition, now, allCurveNames, smileBundle, DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(spec, theta));
    } else if (security instanceof FXDigitalOptionSecurity) {
      final ForexOptionDigitalDefinition definition = (ForexOptionDigitalDefinition) security.accept(VISITOR);
      final MultipleCurrencyAmount theta = calculator.getTheta(definition, now, allCurveNames, smileBundle, PresentValueBlackForexCalculator.getInstance(), DAYS_TO_MOVE_FORWARD);
      return Collections.singleton(new ComputedValue(spec, theta));
    }
    throw new OpenGammaRuntimeException("Should never get here; canApplyTo specifies vanilla and digital options only");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity
        || target.getSecurity() instanceof FXDigitalOptionSecurity;
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    throw new NotImplementedException("Should never get here");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    properties.with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String putCurveName, final String callCurveName, final String putCurveConfig, final String callCurveConfig,
      final String surfaceName, final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName, final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(putCurveName, callCurveName, putCurveConfig, callCurveConfig, surfaceName,
        interpolatorName, leftExtrapolatorName, rightExtrapolatorName, target);
    properties.with(InterestRateFutureConstantSpreadThetaFunctionDeprecated.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunctionDeprecated.THETA_CONSTANT_SPREAD);
    return properties;
  }
}
