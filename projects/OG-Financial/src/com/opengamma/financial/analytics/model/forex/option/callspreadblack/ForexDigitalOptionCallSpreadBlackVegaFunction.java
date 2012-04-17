/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ForexDigitalOptionCallSpreadBlackVegaFunction extends ForexDigitalOptionCallSpreadBlackSingleValuedFunction {

  public ForexDigitalOptionCallSpreadBlackVegaFunction() {
    super(ValueRequirementNames.CALL_SPREAD_VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexOptionDigital fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator calculator = new PresentValueVolatilitySensitivityCallSpreadBlackForexCalculator(spread);
    final PresentValueForexBlackVolatilitySensitivity result = calculator.visit(fxDigital, data);
    return Collections.singleton(new ComputedValue(spec, result));
  }

}
