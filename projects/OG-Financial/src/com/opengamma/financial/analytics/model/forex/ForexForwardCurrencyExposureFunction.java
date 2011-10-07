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
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.forex.calculator.CurrencyExposureForexCalculator;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexForwardCurrencyExposureFunction extends ForexForwardFunction {
  private static final CurrencyExposureForexCalculator CALCULATOR = CurrencyExposureForexCalculator.getInstance();

  public ForexForwardCurrencyExposureFunction(final String payFundingCurveName, final String payForwardCurveName, final String receiveFundingCurveName, final String receiveForwardCurveName) {
    super(payFundingCurveName, payForwardCurveName, receiveFundingCurveName, receiveForwardCurveName);
  }

  @Override
  protected Set<ComputedValue> getResult(final Forex fxForward, final YieldCurveBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxForward, data);
    final int n = result.size();
    final Currency[] keys = new Currency[n];
    final double[] values = new double[n];
    int i = 0;
    for (final CurrencyAmount ca : result) {
      keys[i] = ca.getCurrency();
      values[i++] = ca.getAmount();
    }
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, new CurrencyLabelledMatrix1D(keys, values)));
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPayFundingCurveName())
                                                              .with(ValuePropertyNames.RECEIVE_CURVE, getReceiveFundingCurveName()).get();
    Set<ValueSpecification> temp = Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.toSpecification(),
        properties));
    return temp;
  }

}
