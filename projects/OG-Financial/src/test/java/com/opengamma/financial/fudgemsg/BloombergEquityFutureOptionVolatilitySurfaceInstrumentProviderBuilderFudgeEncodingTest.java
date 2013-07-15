/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergEquityFutureOptionVolatilitySurfaceInstrumentProviderBuilderFudgeEncodingTest extends FinancialTestBase {

  private static final Double CALL_ABOVE_STRIKE = 150.0;
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String FUTURE_OPTION_PREFIX = "DJX";
  private static final String POSTFIX = "Index";
  private static final String EXCHANGE = "OSE";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID_WEAK.getName();

  @Test
  public void testCycle() {
    BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider provider = new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE);
    assertEquals(provider, cycleObject(BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
    provider = new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE, SCHEME);
    assertEquals(provider, cycleObject(BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
