/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.callspreadblack.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.GammaSpotCallSpreadBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.callspreadblack.FXDigitalCallSpreadBlackGammaSpotFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma Spot of Forex options in the Call-spread / Black model.
 * @deprecated Use the versiont that does not refer to funding or forward curves
 * @see FXDigitalCallSpreadBlackGammaSpotFunction
 */
@Deprecated
public class FXDigitalCallSpreadBlackGammaSpotFunctionDeprecated extends FXDigitalCallSpreadBlackSingleValuedFunctionDeprecated {

  public FXDigitalCallSpreadBlackGammaSpotFunctionDeprecated() {
    super(ValueRequirementNames.VALUE_GAMMA_P);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxDigital, final double spread, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final GammaSpotCallSpreadBlackForexCalculator calculator = new GammaSpotCallSpreadBlackForexCalculator(spread);
    final CurrencyAmount result = fxDigital.accept(calculator, data);
    final double gammaSpot = result.getAmount() / 100.0; // FIXME: the 100 should be removed when the scaling is available
    return Collections.singleton(new ComputedValue(spec, gammaSpot));
  }

}
