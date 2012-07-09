/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Returns the spot gamma wrt the spot underlying, ie the 2nd order sensitivity of the present value to the spot value of the underlying,
 *          $\frac{\partial^2 (PV)}{\partial S^2}$
 */
public class EquityIndexOptionSpotGammaFunction extends EquityIndexOptionFunction {

  /**
   * @param valueRequirementName
   */
  public EquityIndexOptionSpotGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Object computeValues(final EquityIndexOption derivative, final EquityOptionDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return model.gammaWrtSpot(derivative, market);
  }

  @Override
  protected ValueProperties.Builder createValueProperties(final ComputationTarget target) {
    return super.createValueProperties(target).with(ValuePropertyNames.CURRENCY, getEquityIndexOptionSecurity(target).getCurrency().getCode());
  }

}
