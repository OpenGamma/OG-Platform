/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.schedule;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Abstract test.
 */
@Test(groups = TestGroup.UNIT)
public abstract class ScheduleCalculatorTestCase {
  private static final LocalDate START1 = LocalDate.of(2000, 1, 1);
  private static final ZonedDateTime START2 = DateUtils.getUTCDate(2000, 1, 1);
  private static final LocalDate END1 = LocalDate.of(2000, 12, 1);
  private static final ZonedDateTime END2 = DateUtils.getUTCDate(2000, 12, 1);

  public abstract Schedule getScheduleCalculator();

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate1() {
    getScheduleCalculator().getSchedule(null, END1, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStartDate2() {
    getScheduleCalculator().getSchedule(null, END2, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDate1() {
    getScheduleCalculator().getSchedule(START1, null, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEndDate2() {
    getScheduleCalculator().getSchedule(START2, null, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAfterEnd1() {
    getScheduleCalculator().getSchedule(END1, START1, true, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAfterEnd2() {
    getScheduleCalculator().getSchedule(END2, START2, true, true);
  }

}
