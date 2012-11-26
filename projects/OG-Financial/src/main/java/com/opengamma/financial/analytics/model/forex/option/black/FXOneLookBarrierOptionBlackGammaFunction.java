/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import org.apache.commons.lang.Validate;

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
  protected Object computeValues(final Set<ForexOptionVanilla> vanillaOptions, final ForexOptionDataBundle<?> market) {
    Validate.isTrue(market instanceof SmileDeltaTermStructureDataBundle, "FXOneLookBarrierOptionBlackGammaFunction requires a Vol surface with a smile.");
    double sum = 0.0;
    for (final ForexOptionVanilla derivative : vanillaOptions) {
      final CurrencyAmount gammaCcy = derivative.accept(CALCULATOR, market);
      sum += gammaCcy.getAmount();
    }
    return sum;
  }

}
