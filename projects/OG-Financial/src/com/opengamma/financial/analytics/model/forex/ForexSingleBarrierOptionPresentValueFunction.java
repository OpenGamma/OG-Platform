/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.CurrencyLabelledMatrix1D;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueBlackForexCalculator;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexSingleBarrierOptionPresentValueFunction extends ForexSingleBarrierOptionFunction {
  private static final PresentValueBlackForexCalculator CALCULATOR = PresentValueBlackForexCalculator.getInstance();

  public ForexSingleBarrierOptionPresentValueFunction(final String putCurveName, final String callCurveName, final String surfaceName) {
    super(putCurveName, callCurveName, surfaceName, ValueRequirementNames.FX_PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxOption, data);
    final int n = result.size();
    final Currency[] keys = new Currency[n];
    final double[] values = new double[n];
    int i = 0;
    for (final CurrencyAmount ca : result) {
      keys[i] = ca.getCurrency();
      values[i++] = ca.getAmount();
    }
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallCurveName())
        .with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, new CurrencyLabelledMatrix1D(keys, values)));
  }

}
