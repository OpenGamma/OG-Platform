/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.GammaValueBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FXOneLookBarrierOptionBlackGammaFunction extends FXOneLookBarrierOptionBlackFunction {

  public FXOneLookBarrierOptionBlackGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  /** The calculator to compute the gamma value. */
  private static final GammaValueBlackForexCalculator CALCULATOR = GammaValueBlackForexCalculator.getInstance();
  @Override
  protected Object computeValues(Set<ForexOptionVanilla> vanillaOptions, ForexOptionDataBundle<?> market) {
    double sum = 0.0;
    for (ForexOptionVanilla derivative : vanillaOptions) {
      if (market instanceof SmileDeltaTermStructureDataBundle) {
        final CurrencyAmount gammaCcy = CALCULATOR.visit(derivative, market);
        sum += gammaCcy.getAmount();
      }
      throw new OpenGammaRuntimeException("Can only calculate gamma for surfaces with smiles");
    }
    return sum;
  }

}
