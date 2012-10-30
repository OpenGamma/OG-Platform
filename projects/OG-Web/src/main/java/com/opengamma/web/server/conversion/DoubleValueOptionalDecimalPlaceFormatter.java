/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.math.BigDecimal;

/**
 * 
 */
public class DoubleValueOptionalDecimalPlaceFormatter extends DoubleValueFormatter {
 
  public DoubleValueOptionalDecimalPlaceFormatter() {
    super(false);
  }
  
  @Override
  protected BigDecimal process(BigDecimal value) {
    return value;
  }

}
