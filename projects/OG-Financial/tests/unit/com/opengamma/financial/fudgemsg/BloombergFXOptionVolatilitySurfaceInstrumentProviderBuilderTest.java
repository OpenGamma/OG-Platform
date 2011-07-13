/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
public class BloombergFXOptionVolatilitySurfaceInstrumentProviderBuilderTest extends FinancialTestBase {
  private static final String PREFIX = "EURJPY";
  private static final String POSTFIX = "Curncy";
  private static final String DATA_FIELD_NAME = "PX_LAST";

  @Test
  public void testCycle() {
    final BloombergFXOptionVolatilitySurfaceInstrumentProvider provider = new BloombergFXOptionVolatilitySurfaceInstrumentProvider(PREFIX, POSTFIX, DATA_FIELD_NAME);
    assertEquals(provider, cycleObject(BloombergFXOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
