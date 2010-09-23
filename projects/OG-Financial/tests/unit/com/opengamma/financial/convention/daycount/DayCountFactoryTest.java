/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test DayCountFactory.
 */
public class DayCountFactoryTest {

  @Test
  public void testDayCountFactory() {
    DayCount a365f = new ActualThreeSixtyFiveFixedDayCount();
    assertEquals(a365f, DayCountFactory.INSTANCE.getDayCount("A/365F"));
    DayCount oneone = new OneOneDayCount();
    assertEquals(oneone, DayCountFactory.INSTANCE.getDayCount("1/1"));
    DayCount thirtyE = new ThirtyEThreeSixtyDayCount();
    assertEquals(thirtyE, DayCountFactory.INSTANCE.getDayCount("30E/360"));
    assertEquals(thirtyE, DayCountFactory.INSTANCE.getDayCount("EuroBond Basis"));
  }

}
