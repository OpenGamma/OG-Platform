/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class FXDigitalCallSpreadBlackPresentValueFunction extends FXDigitalCallSpreadBlackSingleValuedFunction {

  public FXDigitalCallSpreadBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final String spreadName = Iterables.getOnlyElement(desiredValues).getConstraint(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    final double spread = Double.parseDouble(spreadName);
    final PresentValueCallSpreadBlackForexCalculator calculator = new PresentValueCallSpreadBlackForexCalculator(spread);
    final MultipleCurrencyAmount result = fxDigital.accept(calculator, data);
    final CurrencyAmount ca = result.getCurrencyAmounts()[0];
    final String expectedCurrency = spec.getProperty(ValuePropertyNames.CURRENCY);
    if (!expectedCurrency.equals(ca.getCurrency().getCode())) {
      throw new OpenGammaRuntimeException("Expected currency " + expectedCurrency + " does not equal result currency " + ca.getCurrency());
    }
    final double amount = ca.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
