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
import com.opengamma.financial.analytics.model.equity.indexoption.EquityIndexOptionFunction;

/**
 *
 */
public class EquityIndexOptionVegaFunction extends EquityIndexOptionFunction {
  private static final EquityIndexOptionBlackMethod MODEL = EquityIndexOptionBlackMethod.getInstance();

  public EquityIndexOptionVegaFunction() {
    super(ValueRequirementNames.VALUE_VEGA);
  }
  @Override
  protected Object computeValues(EquityIndexOption derivative, EquityOptionDataBundle market) {
    return MODEL.vega(derivative, market);
  }

}
