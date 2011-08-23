/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.time.Tenor;

public class FixedIncomeStripTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    final FixedIncomeStrip strip = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.DAY, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    final FixedIncomeStrip futureStrip = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.YEAR, 3, "DEFAULT");
    assertEquals(futureStrip, cycleObject(FixedIncomeStrip.class, futureStrip));
  }

}
