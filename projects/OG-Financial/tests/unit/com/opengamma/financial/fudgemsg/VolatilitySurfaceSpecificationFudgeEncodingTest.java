/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.core.security.SecurityUtils;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.BloombergEquityOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Fudge serialization test for VolatilitySurfaceSpecification
 */
public class VolatilitySurfaceSpecificationFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    BloombergSwaptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, instrumentProvider);
    AssertJUnit.assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy", MarketDataRequirementNames.MARKET_VALUE);
    spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, instrumentProvider);
    AssertJUnit.assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, instrumentProvider);
    AssertJUnit.assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    AssertJUnit.assertFalse(spec.equals(
        cycleObject(VolatilitySurfaceSpecification.class,
                    new VolatilitySurfaceSpecification("DEFAULT",
                                                       Currency.USD,
                                                       new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy")))));
  }
  
  @Test
  public void testEOCycle() {
    BloombergEquityOptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergEquityOptionVolatilitySurfaceInstrumentProvider("DJX", "Index", MarketDataRequirementNames.IMPLIED_VOLATILITY);
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", UniqueId.of(SecurityUtils.BLOOMBERG_TICKER_WEAK.getName(), "DJX Index"), instrumentProvider);
    AssertJUnit.assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
    AssertJUnit.assertFalse(spec.equals(
        cycleObject(VolatilitySurfaceSpecification.class,
            new VolatilitySurfaceSpecification("DEFAULT", UniqueId.of(SecurityUtils.BLOOMBERG_TICKER.getName(), "DJX Index"),
                new BloombergEquityOptionVolatilitySurfaceInstrumentProvider("DJX", "Index", MarketDataRequirementNames.MID_IMPLIED_VOLATILITY)))));
  }
}
