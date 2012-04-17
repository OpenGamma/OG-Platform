/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.GammaValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma of Forex options in the Black model.
 */
public class ForexDigitalOptionCallSpreadBlackGammaFunction extends ForexDigitalOptionCallSpreadBlackSingleValuedFunction {

  public ForexDigitalOptionCallSpreadBlackGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final ForexOptionDigital fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final GammaValueCallSpreadBlackForexCalculator calculator = new GammaValueCallSpreadBlackForexCalculator(spread);
    final CurrencyAmount result = calculator.visit(fxDigital, data);
    final double amount = result.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
