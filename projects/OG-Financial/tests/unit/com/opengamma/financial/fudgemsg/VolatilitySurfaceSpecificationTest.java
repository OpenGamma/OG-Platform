/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;


import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
import com.opengamma.util.money.Currency;

/**
 * Fudge serialization test for VolatilitySurfaceSpecification
 */
public class VolatilitySurfaceSpecificationTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    BloombergSwaptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", Currency.USD, instrumentProvider);
    AssertJUnit.assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
  }
}
