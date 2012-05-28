/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;

/**
 * 
 */
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProviderBuilderFudgeEncodingTest extends FinancialTestBase{
  private static final Double CALL_ABOVE_STRIKE = 150.0;
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String FUTURE_OPTION_PREFIX = "DJX";
  private static final String POSTFIX = "Index";

  @Test
  public void testCycle() {
    final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider provider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE);
    assertEquals(provider, cycleObject(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
