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
 * The <b>forward</b> value of the index, ie the fair strike of a forward agreement paying the index value at maturity,
 * as seen from the selected market data
 */
public class EquityIndexOptionForwardValueFunction extends EquityIndexOptionFunction {

  public EquityIndexOptionForwardValueFunction() {
    super(ValueRequirementNames.FORWARD);
  }

  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    EquityIndexOptionBlackMethod model = EquityIndexOptionBlackMethod.getInstance();
    return model.forwardIndexValue(derivative, market);
  }

}
