/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;
import org.threeten.bp.Period;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test FixedIncomeStrip.
 */
@Test(groups = TestGroup.UNIT)
public class FixedIncomeStripTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor1_nullType() {
    new FixedIncomeStrip(null, Tenor.of(Period.ofYears(5)), "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor2_nullType() {
    new FixedIncomeStrip(null, Tenor.of(Period.ofYears(5)), 1, "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor1_nullTenor() {
    new FixedIncomeStrip(StripInstrumentType.FRA_3M, null, "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor2_nullTenor() {
    new FixedIncomeStrip(StripInstrumentType.FRA_3M, null, 3, "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor1_nullName() {
    new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofYears(5)), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor2_nullName() {
    new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofYears(5)), 4, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor1_future() {
    new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(5)), "Test");
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testSwapNumberOfFutures() {
    new FixedIncomeStrip(StripInstrumentType.SWAP, Tenor.of(Period.ofYears(5)), "Test").getNumberOfFuturesAfterTenor();
  }

  @Test
  public void testComparator() {
    FixedIncomeStrip strip1 = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.of(Period.ofDays(1)), "Test");
    FixedIncomeStrip strip2 = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.of(Period.ofDays(7)), "Test");
    FixedIncomeStrip strip3 = new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofMonths(3)), "Test");
    FixedIncomeStrip strip4 = new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofMonths(6)), "Test");
    FixedIncomeStrip strip5 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(1)), 1, "Test");
    FixedIncomeStrip strip6 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(1)), 2, "Test");
    FixedIncomeStrip strip7 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(2)), "Test");
    FixedIncomeStrip strip8 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(4)), "Test");
    FixedIncomeStrip strip9 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(7)), "Test");
    FixedIncomeStrip[] array = new FixedIncomeStrip[] {strip1, strip2, strip3, strip4, strip5, strip6, strip7, strip8, strip9 };
    Set<FixedIncomeStrip> set = new TreeSet<>();
    set.add(strip1);
    set.add(strip9);
    set.add(strip2);
    set.add(strip8);
    set.add(strip4);
    set.add(strip7);
    set.add(strip3);
    set.add(strip5);
    set.add(strip6);
    Iterator<FixedIncomeStrip> iter = set.iterator();
    AssertJUnit.assertEquals(array.length, set.size());
    for (final FixedIncomeStrip strip : array) {
      AssertJUnit.assertTrue(set.contains(strip));
      AssertJUnit.assertEquals(iter.next(), strip);
    }
    strip1 = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.of(Period.ofDays(1)), "Test");
    strip2 = new FixedIncomeStrip(StripInstrumentType.CASH, Tenor.of(Period.ofDays(7)), "Test");
    strip3 = new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofMonths(3)), "Test");
    strip4 = new FixedIncomeStrip(StripInstrumentType.FRA_3M, Tenor.of(Period.ofMonths(6)), "Test");
    strip5 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(0)), 4, "Test");
    strip6 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(0)), 6, "Test");
    strip7 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(2)), "Test");
    strip8 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(6)), "Test");
    strip9 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(7)), "Test");
    array = new FixedIncomeStrip[] {strip1, strip2, strip3, strip4, strip5, strip6, strip7, strip8, strip9 };
    set = new TreeSet<>();
    set.add(strip1);
    set.add(strip9);
    set.add(strip2);
    set.add(strip8);
    set.add(strip4);
    set.add(strip7);
    set.add(strip3);
    set.add(strip5);
    set.add(strip6);
    iter = set.iterator();
    for (final FixedIncomeStrip strip : array) {
      AssertJUnit.assertTrue(set.contains(strip));
      AssertJUnit.assertEquals(iter.next(), strip);
    }
    strip1 = new FixedIncomeStrip(StripInstrumentType.BASIS_SWAP, Tenor.SIX_MONTHS, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, IndexType.Libor, IndexType.Libor, "Test");
    strip2 = new FixedIncomeStrip(StripInstrumentType.BASIS_SWAP, Tenor.SEVEN_MONTHS, Tenor.THREE_MONTHS, Tenor.SIX_MONTHS, IndexType.Libor, IndexType.Libor, "Test");
    strip3 = new FixedIncomeStrip(StripInstrumentType.BASIS_SWAP, Tenor.SEVEN_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, IndexType.Libor, IndexType.Libor, "Test");
    strip4 = new FixedIncomeStrip(StripInstrumentType.BASIS_SWAP, Tenor.SEVEN_MONTHS, Tenor.SIX_MONTHS, Tenor.THREE_MONTHS, IndexType.Libor, IndexType.Libor, "Test");
    strip5 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(1)), "Test");
    strip6 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(1)), 1, "Test");
    strip7 = new FixedIncomeStrip(StripInstrumentType.FUTURE, Tenor.of(Period.ofYears(2)), 2, "Test");
    strip8 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(6)), "Test");
    strip9 = new FixedIncomeStrip(StripInstrumentType.SWAP_3M, Tenor.of(Period.ofYears(7)), "Test");
    array = new FixedIncomeStrip[] {strip1, strip2, strip3, strip4, strip5, strip6, strip7, strip8, strip9 };
    set = new TreeSet<>();
    set.add(strip1);
    set.add(strip2);
    set.add(strip3);
    set.add(strip4);
    set.add(strip5);
    set.add(strip6);
    set.add(strip7);
    set.add(strip8);
    set.add(strip9);
    iter = set.iterator();
    for (final FixedIncomeStrip strip : array) {
      AssertJUnit.assertTrue(set.contains(strip));
      AssertJUnit.assertEquals(iter.next(), strip);
    }
  }
}
