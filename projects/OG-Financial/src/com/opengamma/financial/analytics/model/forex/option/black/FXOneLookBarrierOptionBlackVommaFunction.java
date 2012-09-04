/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.forex.method.ForexOptionVanillaBlackSmileMethod;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.money.CurrencyAmount;

/**
 *
 */
public class FXOneLookBarrierOptionBlackVommaFunction extends FXOneLookBarrierOptionBlackFunction {

  public FXOneLookBarrierOptionBlackVommaFunction() {
    super(ValueRequirementNames.VALUE_VOMMA);
  }

  /** The pricing method, Black (Garman-Kohlhagen) */
  private static final ForexOptionVanillaBlackSmileMethod METHOD = ForexOptionVanillaBlackSmileMethod.getInstance();

  @Override
  protected Object computeValues(Set<ForexOptionVanilla> vanillaOptions, ForexOptionDataBundle<?> market) {
    double sum = 0.0;
    for (ForexOptionVanilla derivative : vanillaOptions) {
      if (market instanceof SmileDeltaTermStructureDataBundle) {
        final CurrencyAmount vommaCcy = METHOD.vomma(derivative, market);
        sum += vommaCcy.getAmount();
      }
      throw new OpenGammaRuntimeException("Can only calculate vomma for surfaces with smiles");
    }
    return sum;
  }

}
