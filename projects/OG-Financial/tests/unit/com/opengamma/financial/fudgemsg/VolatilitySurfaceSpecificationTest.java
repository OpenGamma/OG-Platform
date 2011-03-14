/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;


import static org.junit.Assert.*;

import org.junit.Test;

import com.opengamma.core.common.CurrencyUnit;
import com.opengamma.financial.analytics.volatility.surface.BloombergSwaptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;

/**
 * Fudge serialization test for VolatilitySurfaceSpecification
 */
public class VolatilitySurfaceSpecificationTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    BloombergSwaptionVolatilitySurfaceInstrumentProvider instrumentProvider = new BloombergSwaptionVolatilitySurfaceInstrumentProvider("US", "SV", true, false, " Curncy");
    VolatilitySurfaceSpecification spec = new VolatilitySurfaceSpecification("DEFAULT", CurrencyUnit.USD, instrumentProvider);
    assertEquals(spec, cycleObject(VolatilitySurfaceSpecification.class, spec));
  }
}
