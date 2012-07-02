/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.PresentValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.ForexDigitalOptionCallSpreadBlackPresentValueFunction;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see ForexDigitalOptionCallSpreadBlackPresentValueFunction
 */
@Deprecated
public class ForexDigitalOptionCallSpreadBlackPresentValueFunctionDeprecated extends ForexDigitalOptionCallSpreadBlackSingleValuedFunctionDeprecated {

  public ForexDigitalOptionCallSpreadBlackPresentValueFunctionDeprecated() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final PresentValueCallSpreadBlackForexCalculator calculator = new PresentValueCallSpreadBlackForexCalculator(spread);
    final MultipleCurrencyAmount result = calculator.visit(fxDigital, data);
    ArgumentChecker.isTrue(result.size() == 1, "result size must be one; have {}", result.size());
    final CurrencyAmount ca = result.getCurrencyAmounts()[0];
    final double amount = ca.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
