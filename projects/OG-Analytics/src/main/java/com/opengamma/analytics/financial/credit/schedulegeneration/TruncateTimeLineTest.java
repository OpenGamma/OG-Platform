/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.time.DateUtils;

/**
 * 
 */
//@Test(groups = TestGroup.UNIT)
public class TruncateTimeLineTest {
  private static final GenerateCreditDefaultSwapIntegrationSchedule CALCULATOR = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final ZonedDateTime[] SORTED_DATE_LIST;
  private static final ZonedDateTime[] UNSORTED_DATE_LIST;
  private static final ZonedDateTime[] CONCATENATED_SORTED_DATE_LIST;
  private static final ZonedDateTime START_DATE = DateUtils.getUTCDate(2013, 3, 20);

  static {
    ZonedDateTime date = START_DATE;
    final List<ZonedDateTime> sortedList = new ArrayList<>();
    final List<ZonedDateTime> unsortedList = new ArrayList<>();
    final List<ZonedDateTime> concatenatedSortedList = new ArrayList<>();
    for (int i = 0; i < 80; i++) {
      sortedList.add(date);
      unsortedList.add(date);
      date = date.plusMonths(3);
    }
    date = START_DATE;
    for (int i = 0; i < 40; i++) {
      concatenatedSortedList.add(date);
      date = date.plusMonths(6);
    }
    date = START_DATE.plusMonths(3);
    for (int i = 0; i < 40; i++) {
      concatenatedSortedList.add(date);
      date = date.plusMonths(3);
    }
    Collections.shuffle(unsortedList);
    final ZonedDateTime[] emptyArray = new ZonedDateTime[80];
    SORTED_DATE_LIST = sortedList.toArray(emptyArray);
    UNSORTED_DATE_LIST = unsortedList.toArray(emptyArray);
    CONCATENATED_SORTED_DATE_LIST = concatenatedSortedList.toArray(emptyArray);
  }

  @Test
  public void testStartDateAndEndDateBracketNoPoints() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2014, 3, 30);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2014, 5, 30);
    testResults(SORTED_DATE_LIST, startDate, endDate);
    testResults(UNSORTED_DATE_LIST, startDate, endDate);
    testResults(CONCATENATED_SORTED_DATE_LIST, startDate, endDate);
  }

  @Test
  public void testStartAndEndDateBracketAllPoints() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2012, 12, 20);
  }

  private void testResults(final ZonedDateTime[] dates, final ZonedDateTime startDate, final ZonedDateTime endDate) {
    final ZonedDateTime[] truncatedDeprecated = CALCULATOR.getTruncatedTimeLineDeprecated(dates, startDate, endDate);
    assertEquals(2, truncatedDeprecated.length);
    assertDateArrayEquals(new ZonedDateTime[] {startDate, endDate }, truncatedDeprecated);
    final ZonedDateTime[] truncated = CALCULATOR.getTruncatedTimeLine(dates, startDate, endDate);
    assertEquals(2, truncated.length);
    assertDateArrayEquals(new ZonedDateTime[] {startDate, endDate }, truncated);
    assertDateArrayEquals(truncatedDeprecated, truncated);
  }

  //  @Test
  //  public void test1() {
  //    int j = 0;
  //    final double startTime = System.currentTimeMillis();
  //    final MersenneTwister64 random = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  //    for (int i = 0; i < 1000000; i++) {
  //      final long d1 = ((long) (random.nextDouble() * 30000) / 1000);
  //      final long d2 = ((long) (random.nextDouble() * 30000) / 1000);
  //      final ZonedDateTime startDate = DateUtils.getUTCDate(2014, 3, 20).plusDays(d1 * 30);
  //      final ZonedDateTime endDate = DateUtils.getUTCDate(2025, 9, 20).plusDays(d2 * 30);
  //      final ZonedDateTime[] truncated = CALCULATOR.getTruncatedTimeLineDeprecated(CONCATENATED_SORTED_DATE_LIST, startDate, endDate);
  //      j += truncated.length;
  //    }
  //    final double endTime = System.currentTimeMillis();
  //    System.err.println("Array with sort time: " + (endTime - startTime) / 1000. / 1000000.);
  //    System.err.println(j);
  //    System.out.println("----------------------------------------------------------------------");
  //  }

  //  @Test
  //  public void test2() {
  //    int j = 0;
  //    final double startTime = System.currentTimeMillis();
  //    final MersenneTwister64 random = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  //    for (int i = 0; i < 1000000; i++) {
  //      final long d1 = ((long) (random.nextDouble() * 30000) / 1000);
  //      final long d2 = ((long) (random.nextDouble() * 30000) / 1000);
  //      final ZonedDateTime startDate = DateUtils.getUTCDate(2014, 3, 20).plusDays(d1 * 30);
  //      final ZonedDateTime endDate = DateUtils.getUTCDate(2025, 9, 20).plusDays(d2 * 30);
  //      final ZonedDateTime[] truncated = CALCULATOR.getTruncatedTimeLine(CONCATENATED_SORTED_DATE_LIST, startDate, endDate);
  //      j += truncated.length;
  //    }
  //    final double endTime = System.currentTimeMillis();
  //    System.err.println("Array with sort time: " + (endTime - startTime) / 1000. / 1000000.);
  //    System.err.println(j);
  //    System.out.println("----------------------------------------------------------------------");
  //  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
