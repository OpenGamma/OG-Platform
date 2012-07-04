/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.indexoption;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Vanna wrt the spot underlying, ie the 2nd order cross-sensitivity of the present value to the spot underlying and implied vol,
 *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
 */
public class EquityIndexOptionSpotVannaFunction extends EquityIndexOptionFunction {

  /**
   * @param valueRequirementName
   */
  public EquityIndexOptionSpotVannaFunction() {
    super(ValueRequirementNames.VALUE_VANNA);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return model.vannaWrtSpot(derivative, market);
  }

}
