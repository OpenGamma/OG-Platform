/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import com.opengamma.analytics.financial.equity.EquityOptionDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 *
 */
public class EquityIndexOptionSpotDeltaFunction extends EquityIndexOptionFunction {

  public EquityIndexOptionSpotDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return model.deltaWrtSpot(derivative, market);
  }

}
