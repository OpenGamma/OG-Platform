/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.PresentValueForexCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexForwardPresentValueFunction extends ForexForwardFunction {
  private static final PresentValueForexCalculator CALCULATOR = PresentValueForexCalculator.getInstance();

  public ForexForwardPresentValueFunction(final String payFundingCurveName, final String payForwardCurveName, final String receiveFundingCurveName, final String receiveForwardCurveName) {
    super(payFundingCurveName, payForwardCurveName, receiveFundingCurveName, receiveForwardCurveName);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxForward, data);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ForexUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }
  

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_PRESENT_VALUE, target.toSpecification(), properties));
  }
}
