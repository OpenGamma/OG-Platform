/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
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

/**
 * The function calculating the Black volatility sensitivity to each point to which the option is sensitive..
 */
public class FXDigitalCallSpreadBlackVegaPointFunction extends FXDigitalCallSpreadBlackSingleValuedFunction {

  public FXDigitalCallSpreadBlackVegaPointFunction() {
    super(ValueRequirementNames.CALL_SPREAD_VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    final String spreadName = Iterables.getOnlyElement(desiredValues).getConstraint(CalculationPropertyNamesAndValues.PROPERTY_CALL_SPREAD_VALUE);
    final double spread = Double.parseDouble(spreadName);
    final PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator calculator = new PresentValueBlackVolatilitySensitivityCallSpreadBlackForexCalculator(spread);
    final PresentValueForexBlackVolatilitySensitivity result = fxDigital.accept(calculator, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
