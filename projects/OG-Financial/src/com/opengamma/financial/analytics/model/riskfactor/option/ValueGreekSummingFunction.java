/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import java.util.Collections;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;

/**
 * Temporary hack to allow propagation of aggregates in mixed portfolios until [PLAT-366] is resolved.
 */
public class ValueGreekSummingFunction extends FilteringSummingFunction {

  /**
   * @param valueName  the value name to be summed
   */
  public ValueGreekSummingFunction(String valueName) {
    super(valueName, Collections.<String>emptySet());
  }

  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties, SecuritySource securities) {
    return security instanceof EquityOptionSecurity;
  }

}
