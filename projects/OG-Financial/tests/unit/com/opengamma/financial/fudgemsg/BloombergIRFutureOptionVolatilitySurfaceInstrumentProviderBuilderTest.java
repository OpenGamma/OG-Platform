/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderBuilderTest extends FinancialTestBase {
  private static final String FUTURE_OPTION_PREFIX = "ED";
  private static final String POSTFIX = "Comdty";

  @Test
  public void testCycle() {
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider provider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX);
    assertEquals(provider, cycleObject(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
