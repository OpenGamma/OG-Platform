/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;


/**
 * 
 */
public class SABRHaganVolatilityFunctionTest extends SABRVolatilityFunctionTestCase {
  private static final SABRHaganVolatilityFunction FUNCTION = new SABRHaganVolatilityFunction();

  @Override
  protected VolatilityFunctionProvider<SABRFormulaData> getFunction() {
    return FUNCTION;
  }

}
