/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FixedIncomeStripFudgeEncodingTest extends FinancialTestBase {

  @Test
  public void testCycle() {
    FixedIncomeStrip strip = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.DAY, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.YEAR, 3, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.LIBOR, Tenor.DAY, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.EURIBOR, Tenor.DAY, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.SIX_MONTHS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.FRA_6M, Tenor.NINE_MONTHS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.YEAR, 3, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.THREE_YEARS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.SWAP_6M, Tenor.THREE_YEARS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.SWAP_12M, Tenor.THREE_YEARS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.TENOR_SWAP, Tenor.THREE_YEARS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.BASIS_SWAP, Tenor.THREE_YEARS, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, IndexType.Libor, IndexType.Libor, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
    strip = new FixedIncomeStrip(StripInstrumentType.OIS_SWAP, Tenor.THREE_YEARS, "DEFAULT");
    assertEquals(strip, cycleObject(FixedIncomeStrip.class, strip));
  }

}
