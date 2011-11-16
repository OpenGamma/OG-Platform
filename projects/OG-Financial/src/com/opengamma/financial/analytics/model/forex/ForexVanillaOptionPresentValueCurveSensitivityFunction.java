/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.PresentValueCurveSensitivityBlackForexCalculator;
import com.opengamma.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class ForexVanillaOptionPresentValueCurveSensitivityFunction extends ForexVanillaOptionFunction {
  private static final PresentValueCurveSensitivityBlackForexCalculator CALCULATOR = PresentValueCurveSensitivityBlackForexCalculator.getInstance();

  public ForexVanillaOptionPresentValueCurveSensitivityFunction(final String putFundingCurveName, final String putForwardCurveName, final String callFundingCurveName,
      final String callForwardCurveName, final String surfaceName) {
    super(putFundingCurveName, putForwardCurveName, callFundingCurveName, callForwardCurveName, surfaceName);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final FunctionInputs inputs, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName(), getPutForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName(), getCallForwardCurveName()).with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties);
    final MultipleCurrencyInterestRateCurveSensitivity result = CALCULATOR.visit(fxOption, data);
    Validate.isTrue(result.getCurrencies().size() == 1, "Only one currency");
    final Currency ccy = result.getCurrencies().iterator().next();
    return Collections.singleton(new ComputedValue(spec, result.getSensitivity(ccy)));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, getPutFundingCurveName(), getPutForwardCurveName())
        .with(ValuePropertyNames.RECEIVE_CURVE, getCallFundingCurveName(), getCallForwardCurveName()).with(ValuePropertyNames.SURFACE, getSurfaceName()).get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.FX_CURVE_SENSITIVITIES, target.toSpecification(), properties));
  }
}
