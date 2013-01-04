/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.GammaValueCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma of Forex options in the Call-spread / Black model.
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXDigitalCallSpreadBlackGammaFunctionDeprecated
 */
@Deprecated
public class FXDigitalCallSpreadBlackGammaFunctionDeprecated extends FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated {

  public FXDigitalCallSpreadBlackGammaFunctionDeprecated() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final GammaValueCallSpreadBlackForexCalculator calculator = new GammaValueCallSpreadBlackForexCalculator(spread);
    final CurrencyAmount result = fxDigital.accept(calculator, data);
    final double amount = result.getAmount();
    return Collections.singleton(new ComputedValue(spec, amount));
  }

}
