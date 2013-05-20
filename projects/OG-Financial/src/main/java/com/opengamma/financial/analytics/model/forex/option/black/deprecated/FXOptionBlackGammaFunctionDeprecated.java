/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black.deprecated;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.forex.calculator.GammaValueBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.forex.option.black.FXOptionBlackValueGammaFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma of Forex options in the Black model.
 * @deprecated Use the version that does not refer to funding or forward curves
 * @see FXOptionBlackValueGammaFunction
 */
@Deprecated
public class FXOptionBlackGammaFunctionDeprecated extends FXOptionBlackSingleValuedFunctionDeprecated {

  /**
   * The calculator to compute the gamma value.
   */
  private static final GammaValueBlackForexCalculator CALCULATOR = GammaValueBlackForexCalculator.getInstance();

  public FXOptionBlackGammaFunctionDeprecated() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative fxOption, final SmileDeltaTermStructureDataBundle data, final ValueSpecification spec) {
    final CurrencyAmount result = fxOption.accept(CALCULATOR, data);
    final double gammaValue = result.getAmount();
    return Collections.singleton(new ComputedValue(spec, gammaValue));
  }

}
