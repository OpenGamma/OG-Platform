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

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.interestrate.ConstantSpreadHorizonThetaCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.conversion.ForexSecurityConverter;
import com.opengamma.financial.analytics.model.forex.forward.FXForwardMultiValuedFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexForwardConstantSpreadThetaFunction extends FXForwardMultiValuedFunction {
  private static final ForexSecurityConverter VISITOR = new ForexSecurityConverter();
  private static final int DAYS_TO_MOVE_FORWARD = 1; // TODO Add to Value Properties

  public ForexForwardConstantSpreadThetaFunction() {
    super(ValueRequirementNames.VALUE_THETA);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Currency payCurrency, receiveCurrency;
    if (security instanceof FXForwardSecurity) {
      final FXForwardSecurity forward = (FXForwardSecurity) security;
      payCurrency = forward.getPayCurrency();
      receiveCurrency = forward.getReceiveCurrency();
    } else {
      final NonDeliverableFXForwardSecurity ndf = (NonDeliverableFXForwardSecurity) security;
      payCurrency = ndf.getPayCurrency();
      receiveCurrency = ndf.getReceiveCurrency();
    }
    final ForexDefinition definition = (ForexDefinition) security.accept(VISITOR);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveConfig = desiredValue.getConstraint(PAY_CURVE_CALC_CONFIG);
    final String receiveCurveConfig = desiredValue.getConstraint(RECEIVE_CURVE_CALC_CONFIG);
    final String fullPutCurveName = payCurveName + "_" + payCurrency.getCode();
    final String fullCallCurveName = receiveCurveName + "_" + receiveCurrency.getCode();
    final YieldAndDiscountCurve payFundingCurve = getCurve(inputs, payCurrency, payCurveName, payCurveConfig);
    final YieldAndDiscountCurve receiveFundingCurve = getCurve(inputs, receiveCurrency, receiveCurveName, receiveCurveConfig);
    final YieldAndDiscountCurve[] curves;
    final Map<String, Currency> curveCurrency = new HashMap<String, Currency>();
    curveCurrency.put(fullPutCurveName, payCurrency);
    curveCurrency.put(fullCallCurveName, receiveCurrency);
    final String[] allCurveNames;
    if (FXUtils.isInBaseQuoteOrder(payCurrency, receiveCurrency)) { // To get Base/quote in market standard order.
      curves = new YieldAndDiscountCurve[] {payFundingCurve, receiveFundingCurve};
      allCurveNames = new String[] {fullPutCurveName, fullCallCurveName};
    } else {
      curves = new YieldAndDiscountCurve[] {receiveFundingCurve, payFundingCurve};
      allCurveNames = new String[] {fullCallCurveName, fullPutCurveName};
    }
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(allCurveNames, curves);
    final ValueProperties.Builder properties = getResultProperties(payCurveName, receiveCurveName, payCurveConfig, receiveCurveConfig, target);
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_THETA, target.toSpecification(), properties.get());
    final ConstantSpreadHorizonThetaCalculator calculator = ConstantSpreadHorizonThetaCalculator.getInstance();
    final MultipleCurrencyAmount theta = calculator.getTheta(definition, now, allCurveNames, yieldCurves, DAYS_TO_MOVE_FORWARD);
    return Collections.singleton(new ComputedValue(spec, theta));
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final ValueSpecification spec) {
    throw new NotImplementedException("Should never get here");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(target);
    properties.with(InterestRateFutureConstantSpreadThetaFunction.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunction.THETA_CONSTANT_SPREAD);
    return properties;
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String payCurveName, final String receiveCurveName, final String payCurveCalculationConfig, final String receiveCurveCalculationConfig,
      final ComputationTarget target) {
    final ValueProperties.Builder properties = super.getResultProperties(payCurveName, receiveCurveName, payCurveCalculationConfig, receiveCurveCalculationConfig, target);
    properties.with(InterestRateFutureConstantSpreadThetaFunction.PROPERTY_THETA_CALCULATION_METHOD, InterestRateFutureConstantSpreadThetaFunction.THETA_CONSTANT_SPREAD);
    return properties;
  }
}
