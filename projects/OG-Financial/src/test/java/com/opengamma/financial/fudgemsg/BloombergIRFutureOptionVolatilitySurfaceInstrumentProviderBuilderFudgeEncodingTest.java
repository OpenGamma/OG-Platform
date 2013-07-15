/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergIRFutureOptionVolatilitySurfaceInstrumentProviderBuilderFudgeEncodingTest extends FinancialTestBase {

  private static final double CALL_ABOVE_STRIKE = 99.;
  private static final String DATA_FIELD_NAME = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  private static final String FUTURE_OPTION_PREFIX = "ED";
  private static final String POSTFIX = "Comdty";
  private static final String EXCHANGE = "EUX";
  private static final String SCHEME = ExternalSchemes.BLOOMBERG_BUID.getName();

  @Test
  public void testCycle() {
    BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider provider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE);
    assertEquals(provider, cycleObject(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
    provider = new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(FUTURE_OPTION_PREFIX, POSTFIX,
        DATA_FIELD_NAME, CALL_ABOVE_STRIKE, EXCHANGE, SCHEME);
    assertEquals(provider, cycleObject(BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider.class, provider));
  }
}
