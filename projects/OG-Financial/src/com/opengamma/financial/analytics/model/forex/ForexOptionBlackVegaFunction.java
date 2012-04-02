/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.PresentValueVolatilitySensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ForexOptionBlackVegaFunction extends ForexOptionBlackSingleValuedFunction {
  private static final PresentValueVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueVolatilitySensitivityBlackForexCalculator.getInstance();

  public ForexOptionBlackVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueForexBlackVolatilitySensitivity result = CALCULATOR.visit(fxOption, data);
    final Map<DoublesPair, Double> vega = result.getVega().getMap();
    if (vega.size() != 1) {
      throw new OpenGammaRuntimeException("Expecting only one value for vega; have " + vega);
    }
    final double vegaValue = vega.values().iterator().next();
    return Collections.singleton(new ComputedValue(spec, vegaValue));
  }

}
