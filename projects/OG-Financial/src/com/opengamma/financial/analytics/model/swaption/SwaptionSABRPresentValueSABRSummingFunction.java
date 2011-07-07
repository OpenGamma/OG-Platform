/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption;

import java.util.Collections;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.FilteringSummingFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;

/**
 * Summing function for {@link ValueRequirementNames#PRESENT_VALUE_SABR_ALPHA_SENSITIVITY}
 */
public class SwaptionSABRPresentValueSABRSummingFunction extends FilteringSummingFunction {

  public SwaptionSABRPresentValueSABRSummingFunction() {
    super(ValueRequirementNames.PRESENT_VALUE_SABR_ALPHA_SENSITIVITY, Collections.singleton(ValuePropertyNames.CUBE));
  }

  @Override
  protected boolean isIncluded(FinancialSecurity security, ValueProperties filterProperties) {
    return security instanceof SwaptionSecurity;
  }

}
