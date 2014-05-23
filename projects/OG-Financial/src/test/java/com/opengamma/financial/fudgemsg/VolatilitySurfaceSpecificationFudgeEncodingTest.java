/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergEquityOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class VolatilitySurfaceSpecificationFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testSwaptionCycle() {
    BloombergSwaptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, SurfaceAndCubeQuoteType.PAY_RECEIVE_DELTA, instrumentProvider);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy", MarketDataRequirementNames.MARKET_VALUE);
    spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, SurfaceAndCubeQuoteType.PAY_RECEIVE_DELTA, instrumentProvider);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, SurfaceAndCubeQuoteType.PAY_RECEIVE_DELTA, instrumentProvider);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    assertFalse(spec.equals(cycleObject(VolatilitySurfaceSpecification.class, new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, SurfaceAndCubeQuoteType.PAY_RECEIVE_DELTA, new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy")))));
  }

  @Test
  public void testEOCycle() {
    final BloombergEquityOptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergEquityOptionVolatilitySurfaceInstrumentProvider("DJX", "Index", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index"),
        SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE, instrumentProvider);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    spec = new VolatilitySurfaceSpecification("DEFAULT", UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index"), SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE, SurfaceAndCubeQuoteType.CALL_DELTA, new EuropeanExerciseType(), instrumentProvider, true);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    assertFalse(spec.equals(cycleObject(VolatilitySurfaceSpecification.class, new VolatilitySurfaceSpecification("DEFAULT", UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER.getName(), "DJX Index"), SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE, new BloombergEquityOptionVolatilitySurfaceInstrumentProvider("DJX", "Index", MarketDataRequirementNames.MID_IMPLIED_VOLATILITY)))));
  }
}
