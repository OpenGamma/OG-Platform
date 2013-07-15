/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.calculator.GammaSpotCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.CalculationPropertyNamesAndValues;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma Spot of Forex options in the Call-spread / Black model.
 */
public class FXDigitalCallSpreadBlackGammaSpotFunction extends FXDigitalCallSpreadBlackSingleValuedFunction {

  public FXDigitalCallSpreadBlackGammaSpotFunction() {
    super(ValueRequirementNames.VALUE_GAMMA_P);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final String spreadName = Iterables.getOnlyElement(desiredValues).getConstraint(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    final double spread = Double.parseDouble(spreadName);
    final GammaSpotCallSpreadBlackForexCalculator calculator = new GammaSpotCallSpreadBlackForexCalculator(spread);
    final CurrencyAmount result = fxDigital.accept(calculator, data);
    final double gammaSpot = result.getAmount() / 100.0; // FIXME: the 100 should be removed when the scaling is available
    return Collections.singleton(new ComputedValue(spec, gammaSpot));
  }

}
