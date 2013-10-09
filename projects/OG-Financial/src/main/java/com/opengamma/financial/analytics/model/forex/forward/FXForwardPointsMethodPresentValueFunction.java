/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.forward;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.ForexForwardPointsMethod;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueProperties.Builder;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fxforwardcurve.FXForwardCurveDefinition;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.analytics.model.fx.FXForwardPointsPVFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an FX forward using the FX forward rates directly.
 * @deprecated Use {@link FXForwardPointsPVFunction}
 */
@Deprecated
public class FXForwardPointsMethodPresentValueFunction extends FXForwardPointsMethodFunction {
  private static final ForexForwardPointsMethod CALCULATOR = ForexForwardPointsMethod.getInstance();

  public FXForwardPointsMethodPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final DoublesCurve forwardPoints, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final FunctionExecutionContext executionContext,
      final FXForwardCurveDefinition fxForwardCurveDefinition) {
    final MultipleCurrencyAmount mca = CALCULATOR.presentValue(fxForward, data, forwardPoints);
    if (mca.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting a single value for present value");
    }
    final CurrencyAmount ca = mca.getCurrencyAmounts()[0];
    final String currency = ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode();
    if (!ca.getCurrency().getCode().equals(currency)) {
      throw new OpenGammaRuntimeException("Property currency did not match result currency");
    }
    final ValueProperties properties = getResultProperties(Iterables.getOnlyElement(desiredValues), currency).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ca.getAmount()));
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS)
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG)
        .withAny(ValuePropertyNames.FORWARD_CURVE_NAME)
        .withAny(ValuePropertyNames.CURRENCY);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target, final String payCurveName, final String receiveCurveName,
      final String payCurveCalculationConfig, final String receiveCurveCalculationConfig, final String forwardCurveName) {
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.FORWARD_CURVE_NAME, forwardCurveName)
        .with(ValuePropertyNames.CURRENCY, ((FinancialSecurity) target.getSecurity()).accept(ForexVisitors.getReceiveCurrencyVisitor()).getCode());
  }

  protected ValueProperties.Builder getResultProperties(final ValueRequirement desiredValue, final String currency) {
    final String payCurveName = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE);
    final String receiveCurveName = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE);
    final String payCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG);
    final String receiveCurveCalculationConfig = desiredValue.getConstraint(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG);
    final String forwardCurveName = desiredValue.getConstraint(ValuePropertyNames.FORWARD_CURVE_NAME);
    return createValueProperties()
        .with(ValuePropertyNames.CALCULATION_METHOD, CalculationPropertyNamesAndValues.FORWARD_POINTS)
        .with(ValuePropertyNames.PAY_CURVE, payCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, receiveCurveName)
        .with(ValuePropertyNames.PAY_CURVE_CALCULATION_CONFIG, payCurveCalculationConfig)
        .with(ValuePropertyNames.RECEIVE_CURVE_CALCULATION_CONFIG, receiveCurveCalculationConfig)
        .with(ValuePropertyNames.FORWARD_CURVE_NAME, forwardCurveName)
        .with(ValuePropertyNames.CURRENCY, currency);
  }

  @Override
  protected Builder getResultProperties(final ValueRequirement desiredValue, final ComputationTarget target) {
    throw new UnsupportedOperationException();
  }
}
