/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class GeneralRollDateAdjusterTest {
  private static final TemporalAdjuster ADJUSTER_MONTHLY = MonthlyIMMRollDateAdjuster.getAdjuster();
  private static final TemporalAdjuster ADJUSTER_QUARTERLY = QuarterlyIMMRollDateAdjuster.getAdjuster();
  private static final TemporalAdjuster ROLL_DATE_ADJUSTER_MONTHLY = new GeneralRollDateAdjuster(1, ADJUSTER_MONTHLY);
  private static final TemporalAdjuster ROLL_DATE_ADJUSTER_QUARTERLY = new GeneralRollDateAdjuster(1, ADJUSTER_QUARTERLY);

  @Test
  public void testFirstQuarter() {
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate immDate = LocalDate.of(2013, 3, 20);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_QUARTERLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testSecondQuarter() {
    LocalDate date = LocalDate.of(2013, 3, 21);
    final LocalDate immDate = LocalDate.of(2013, 6, 19);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_QUARTERLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testThirdQuarterBefore() {
    LocalDate date = LocalDate.of(2013, 6, 20);
    final LocalDate immDate = LocalDate.of(2013, 9, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_QUARTERLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testFourthQuarter() {
    LocalDate date = LocalDate.of(2013, 9, 19);
    final LocalDate immDate = LocalDate.of(2013, 12, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_QUARTERLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testYearEnd() {
    LocalDate date = LocalDate.of(2013, 12, 19);
    final LocalDate endOfYear = LocalDate.of(2013, 12, 31);
    final LocalDate immDate = LocalDate.of(2014, 3, 19);
    while (!date.isAfter(endOfYear)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_QUARTERLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testFirstMonth() {
    LocalDate date = LocalDate.of(2013, 1, 1);
    final LocalDate immDate = LocalDate.of(2013, 1, 16);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testSecondMonth() {
    LocalDate date = LocalDate.of(2013, 1, 17);
    final LocalDate immDate = LocalDate.of(2013, 2, 20);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testThirdMonth() {
    LocalDate date = LocalDate.of(2013, 2, 21);
    final LocalDate immDate = LocalDate.of(2013, 3, 20);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testFourthMonth() {
    LocalDate date = LocalDate.of(2013, 3, 21);
    final LocalDate immDate = LocalDate.of(2013, 4, 17);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testFifthMonth() {
    LocalDate date = LocalDate.of(2013, 4, 18);
    final LocalDate immDate = LocalDate.of(2013, 5, 15);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testSixthMonth() {
    LocalDate date = LocalDate.of(2013, 5, 16);
    final LocalDate immDate = LocalDate.of(2013, 6, 19);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testSeventhMonth() {
    LocalDate date = LocalDate.of(2013, 6, 20);
    final LocalDate immDate = LocalDate.of(2013, 7, 17);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testEighthMonth() {
    LocalDate date = LocalDate.of(2013, 7, 18);
    final LocalDate immDate = LocalDate.of(2013, 8, 21);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testNinthMonth() {
    LocalDate date = LocalDate.of(2013, 8, 22);
    final LocalDate immDate = LocalDate.of(2013, 9, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testTenthMonth() {
    LocalDate date = LocalDate.of(2013, 9, 19);
    final LocalDate immDate = LocalDate.of(2013, 10, 16);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testEleventhMonth() {
    LocalDate date = LocalDate.of(2013, 10, 17);
    final LocalDate immDate = LocalDate.of(2013, 11, 20);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testTwelfthMonth() {
    LocalDate date = LocalDate.of(2013, 11, 21);
    final LocalDate immDate = LocalDate.of(2013, 12, 18);
    while (!date.isAfter(immDate)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

  @Test
  public void testYearEndMonthly() {
    LocalDate date = LocalDate.of(2013, 12, 19);
    final LocalDate endOfYear = LocalDate.of(2013, 12, 31);
    final LocalDate immDate = LocalDate.of(2014, 1, 15);
    while (!date.isAfter(endOfYear)) {
      assertEquals(immDate, ROLL_DATE_ADJUSTER_MONTHLY.adjustInto(date));
      date = date.plusDays(1);
    }
  }

}
