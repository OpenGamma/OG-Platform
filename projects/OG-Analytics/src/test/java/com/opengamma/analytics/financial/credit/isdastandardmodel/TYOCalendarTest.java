/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.threeten.bp.Month.MARCH;
import static org.threeten.bp.Month.SEPTEMBER;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TYOCalendarTest {

  @Test
  public void test() {
    final TYOCalendar tyo = new TYOCalendar("TYO");

    assertFalse("" + LocalDate.of(2009, MARCH, 20).getDayOfWeek(), tyo.isWorkingDay(LocalDate.of(2009, MARCH, 20)));
    assertFalse(tyo.isWorkingDay(LocalDate.of(2009, MARCH, 21)));
    assertTrue(tyo.isWorkingDay(LocalDate.of(2009, MARCH, 23)));
    assertFalse(tyo.isWorkingDay(LocalDate.of(2032, SEPTEMBER, 20)));
  }

}
