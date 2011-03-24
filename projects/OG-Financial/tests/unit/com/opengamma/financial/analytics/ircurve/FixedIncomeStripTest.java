/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.Period;

import org.testng.annotations.Test;

import com.opengamma.util.time.Tenor;

/**
 * Test FixedIncomeStrip.
 */
public class FixedIncomeStripTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor3_nullType() {
    new FixedIncomeStrip(null, new Tenor(Period.ofYears(5)), "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor3_nullTenor() {
    new FixedIncomeStrip(StripInstrumentType.SWAP, null, "Test");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor3_nullName() {
    new FixedIncomeStrip(StripInstrumentType.SWAP, new Tenor(Period.ofYears(5)), null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor3_future() {
    new FixedIncomeStrip(StripInstrumentType.FUTURE, new Tenor(Period.ofYears(5)), "Test");
  }

}
