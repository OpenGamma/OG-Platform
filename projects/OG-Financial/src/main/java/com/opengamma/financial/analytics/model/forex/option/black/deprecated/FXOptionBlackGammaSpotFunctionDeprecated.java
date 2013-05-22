/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.GammaSpotBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackValueGammaSpotFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma Spot of Forex options in the Black model.
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXOptionBlackValueGammaSpotFunction
 */
@Deprecated
public class FXOptionBlackGammaSpotFunctionDeprecated extends FXOptionBlackSingleValuedFunctionDeprecated {

  /**
   * The calculator to compute the gamma value.
   */
  private static final GammaSpotBlackForexCalculator CALCULATOR = GammaSpotBlackForexCalculator.getInstance();

  public FXOptionBlackGammaSpotFunctionDeprecated() {
    super(ValueRequirementNames.VALUE_GAMMA_P);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final CurrencyAmount result = fxOption.accept(CALCULATOR, data);
    final double gammaValue = result.getAmount() / 100.0; // FIXME: the 100 should be removed when the scaling is available
    return Collections.singleton(new ComputedValue(spec, gammaValue));
  }

}
