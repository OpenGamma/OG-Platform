/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueBlackVolatilitySensitivityBlackForexCalculator;
import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function calculating the total Black volatility sensitivity. 
 */
public class ForexOptionBlackVegaFunction extends ForexOptionBlackSingleValuedFunction {

  /**
   * The relevant calculator.
   */
  private static final PresentValueBlackVolatilitySensitivityBlackForexCalculator CALCULATOR = PresentValueBlackVolatilitySensitivityBlackForexCalculator.getInstance();

  public ForexOptionBlackVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueForexBlackVolatilitySensitivity result = CALCULATOR.visit(fxOption, data);
    final CurrencyAmount vegaValue = result.toSingleValue();
    return Collections.singleton(new ComputedValue(spec, vegaValue.getAmount()));
  }

}
