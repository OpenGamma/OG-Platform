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
import com.opengamma.financial.forex.calculator.CurrencyExposureBlackForexCalculator;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class ForexSingleBarrierOptionCurrencyExposureFunction extends ForexSingleBarrierOptionFunction {
  private static final CurrencyExposureBlackForexCalculator CALCULATOR = CurrencyExposureBlackForexCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxSingleBarrierOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target,
      final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName, final String callForwardCurveName, final String surfaceName) {
    final MultipleCurrencyAmount result = CALCULATOR.visit(fxSingleBarrierOption, data);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.PAY_CURVE, putFundingCurveName)
        .with(ValuePropertyNames.RECEIVE_CURVE, callFundingCurveName)
        .with(ValuePropertyNames.SURFACE, surfaceName).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, ForexUtils.getMultipleCurrencyAmountAsMatrix(result)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.PAY_CURVE)
        .withAny(ValuePropertyNames.RECEIVE_CURVE)
        .withAny(ValuePropertyNames.SURFACE).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_CURRENCY_EXPOSURE, target.toSpecification(), properties));
  }
}
