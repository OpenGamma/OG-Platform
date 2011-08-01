/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;

/**
 * Summing function for swaptions SABR model parameter sensitivities
 */
public class SwaptionSABRSummingFunction extends FilteringSummingFunction {

  public SwaptionSABRSummingFunction(String valueName) {
    super(valueName, Collections.singleton(ValuePropertyNames.CUBE));
  }

  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties, SecuritySource securities) {
    return security instanceof SwaptionSecurity;
  }

}
