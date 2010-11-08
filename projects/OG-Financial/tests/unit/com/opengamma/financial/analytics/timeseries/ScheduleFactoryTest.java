/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class ScheduleFactoryTest {
  private static final LocalDate START1 = LocalDate.of(2000, 1, 31);
  private static final LocalDate END1 = LocalDate.of(2002, 1, 31);
  private static final ZonedDateTime START2 = DateUtil.getUTCDate(2000, 1, 31);
  private static final ZonedDateTime END2 = DateUtil.getUTCDate(2002, 1, 31);
  private static final LocalDate START3 = LocalDate.of(2000, 1, 1);
  private static final LocalDate END3 = LocalDate.of(2002, 1, 1);
  private static final ZonedDateTime START4 = DateUtil.getUTCDate(2000, 1, 1);
  private static final ZonedDateTime END4 = DateUtil.getUTCDate(2002, 1, 1);
  private static final Frequency QUARTERLY = SimpleFrequencyFactory.INSTANCE.getFrequency(4);

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartDate1() {
    ScheduleFactory.getSchedule(null, END1, QUARTERLY, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEndDate1() {
    ScheduleFactory.getSchedule(START1, null, QUARTERLY, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFrequency1() {
    ScheduleFactory.getSchedule(START1, END1, null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadFrequency1() {
    final Frequency frequency = new Frequency() {

      @Override
      public String getConventionName() {
        return null;
      }
    };
    ScheduleFactory.getSchedule(START1, END1, frequency, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadPeriodsPerYear1() {
    ScheduleFactory.getSchedule(START1, END1, 5, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWeeklyWithEOMAdjustment1() {
    ScheduleFactory.getSchedule(START1, END1, 52, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDailyWithEOMAdjustment1() {
    ScheduleFactory.getSchedule(START1, END1, 365, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartDate2() {
    ScheduleFactory.getSchedule(null, END2, QUARTERLY, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullEndDate2() {
    ScheduleFactory.getSchedule(START2, null, QUARTERLY, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullFrequency2() {
    ScheduleFactory.getSchedule(START2, END2, null, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadFrequency2() {
    final Frequency frequency = new Frequency() {

      @Override
      public String getConventionName() {
        return null;
      }
    };
    ScheduleFactory.getSchedule(START2, END2, frequency, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadPeriodsPerYear2() {
    ScheduleFactory.getSchedule(START2, END2, 5, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWeeklyWithEOMAdjustment2() {
    ScheduleFactory.getSchedule(START2, END2, 52, true, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDailyWithEOMAdjustment2() {
    ScheduleFactory.getSchedule(START2, END2, 365, true, true);
  }

  @Test
  public void testDaily() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 365, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, 366, true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Daily"), true);
    final LocalDate[] schedule4 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.DAILY, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    assertArrayEquals(schedule1, schedule4);
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 365, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, 366, true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Daily"), true);
    final ZonedDateTime[] schedule8 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.DAILY, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertArrayEquals(schedule5, schedule8);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testWeeklyBackward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 52, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Weekly"), true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.WEEKLY, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 52, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Weekly"), true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.WEEKLY, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testWeeklyForward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 52, false);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Weekly"), false);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.WEEKLY, false);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 52, false);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Weekly"), false);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.WEEKLY, false);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testMonthlyBackward() {
    LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 12, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.MONTHLY, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    schedule1 = ScheduleFactory.getSchedule(START1, END1, 12, true, false);
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), true, false));
    assertArrayEquals(schedule1, ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.MONTHLY, true, false));
    final LocalDate[] schedule4 = ScheduleFactory.getSchedule(START3, END3, 12, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START3, END3, 12, false, true));
    ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 12, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.MONTHLY, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule2.length, schedule5.length);
    for (int i = 0; i < schedule2.length; i++) {
      assertEquals(schedule2[i], schedule5[i].toLocalDate());
    }
    schedule5 = ScheduleFactory.getSchedule(START2, END2, 12, false, false);
    assertArrayEquals(schedule5, ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), false, false));
    assertArrayEquals(schedule5, ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.MONTHLY, false, false));
    final LocalDate[] schedule8 = ScheduleFactory.getSchedule(START3, END3, 12, true, true);
    assertArrayEquals(schedule8, ScheduleFactory.getSchedule(START3, END3, 12, false, true));
  }

  @Test
  public void testMonthlyForward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 12, false);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), false);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.MONTHLY, false);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule4 = ScheduleFactory.getSchedule(START4, END4, 12, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START4, END4, 12, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 12, false);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Monthly"), false);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.MONTHLY, false);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testQuarterlyBackward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 4, false, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Quarterly"), false, true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.QUARTERLY, false, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final LocalDate[] schedule4 = ScheduleFactory.getSchedule(START3, END3, 4, false, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START3, END3, 4, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 4, false, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Quarterly"), false, true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.QUARTERLY, false, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
  }

  @Test
  public void testQuarterlyForward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 4, false);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Quarterly"), false);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.QUARTERLY, false);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule4 = ScheduleFactory.getSchedule(START4, END4, 4, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START4, END4, 4, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 4, false);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Quarterly"), false);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.QUARTERLY, false);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
  }

  @Test
  public void testSemiAnnualBackward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 2, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Semi-Annual"), true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.SEMI_ANNUAL, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final LocalDate[] schedule4 = ScheduleFactory.getSchedule(START3, END3, 2, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START3, END3, 2, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 2, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Semi-Annual"), true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.SEMI_ANNUAL, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testSemiAnnualForward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 2, false);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Semi-Annual"), false);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.SEMI_ANNUAL, false);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule4 = ScheduleFactory.getSchedule(START4, END4, 2, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START4, END4, 2, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 2, false);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Semi-Annual"), false);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.SEMI_ANNUAL, false);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testAnnualBackward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 1, true);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Annual"), true);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.ANNUAL, true);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final LocalDate[] schedule4 = ScheduleFactory.getSchedule(START3, END3, 1, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START3, END3, 1, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 1, true);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Annual"), true);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.ANNUAL, true);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }

  @Test
  public void testAnnualForward() {
    final LocalDate[] schedule1 = ScheduleFactory.getSchedule(START1, END1, 1, false);
    final LocalDate[] schedule2 = ScheduleFactory.getSchedule(START1, END1, SimpleFrequencyFactory.INSTANCE.getFrequency("Annual"), false);
    final LocalDate[] schedule3 = ScheduleFactory.getSchedule(START1, END1, PeriodFrequency.ANNUAL, false);
    assertArrayEquals(schedule1, schedule2);
    assertArrayEquals(schedule1, schedule3);
    final ZonedDateTime[] schedule4 = ScheduleFactory.getSchedule(START4, END4, 1, true, true);
    assertArrayEquals(schedule4, ScheduleFactory.getSchedule(START4, END4, 1, false, true));
    final ZonedDateTime[] schedule5 = ScheduleFactory.getSchedule(START2, END2, 1, false);
    final ZonedDateTime[] schedule6 = ScheduleFactory.getSchedule(START2, END2, SimpleFrequencyFactory.INSTANCE.getFrequency("Annual"), false);
    final ZonedDateTime[] schedule7 = ScheduleFactory.getSchedule(START2, END2, PeriodFrequency.ANNUAL, false);
    assertArrayEquals(schedule5, schedule6);
    assertArrayEquals(schedule5, schedule7);
    assertEquals(schedule1.length, schedule5.length);
    for (int i = 0; i < schedule1.length; i++) {
      assertEquals(schedule1[i], schedule5[i].toLocalDate());
    }
  }
}
