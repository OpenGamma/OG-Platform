/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import java.util.Set;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class EquityIndexVanillaBarrierOptionSpotGammaFunction extends EquityIndexVanillaBarrierOptionFunction {

  public EquityIndexVanillaBarrierOptionSpotGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Object computeValues(Set<EquityIndexOption> vanillaOptions, EquityOptionDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    double sum = 0.0;
    for (EquityIndexOption derivative : vanillaOptions) {
      sum += model.gammaWrtSpot(derivative, market);
    }
    return sum;
  }

}
