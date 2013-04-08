/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import static com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleTestUtils.assertDateArrayEquals;
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
  private static final GenerateCreditDefaultSwapIntegrationSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapIntegrationSchedule();
  private static final ZonedDateTime[] SORTED_DATE_LIST;
  private static final int N;
  private static final ZonedDateTime[] UNSORTED_DATE_LIST;
  private static final ZonedDateTime[] CONCATENATED_SORTED_DATE_LIST;
  //TODO add tests for repeated dates
  static {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2013, 3, 20);
    ZonedDateTime date = startDate;
    final List<ZonedDateTime> sortedList = new ArrayList<>();
    final List<ZonedDateTime> unsortedList = new ArrayList<>();
    final List<ZonedDateTime> concatenatedSortedList = new ArrayList<>();
    for (int i = 0; i < 80; i++) {
      sortedList.add(date);
      unsortedList.add(date);
      date = date.plusMonths(3);
    }
    date = startDate;
    for (int i = 0; i < 40; i++) {
      concatenatedSortedList.add(date);
      date = date.plusMonths(6);
    }
    date = startDate.plusMonths(3);
    for (int i = 0; i < 40; i++) {
      concatenatedSortedList.add(date);
      date = date.plusMonths(6);
    }
    Collections.shuffle(unsortedList);
    SORTED_DATE_LIST = sortedList.toArray(new ZonedDateTime[sortedList.size()]);
    UNSORTED_DATE_LIST = unsortedList.toArray(new ZonedDateTime[unsortedList.size()]);
    CONCATENATED_SORTED_DATE_LIST = concatenatedSortedList.toArray(new ZonedDateTime[concatenatedSortedList.size()]);
    N = SORTED_DATE_LIST.length;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStart() {
    ScheduleUtils.getTruncatedTimeLine(SORTED_DATE_LIST, null, DateUtils.getUTCDate(2015, 1, 21), true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullEnd() {
    ScheduleUtils.getTruncatedTimeLine(SORTED_DATE_LIST, DateUtils.getUTCDate(2015, 1, 21), null, true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullList() {
    ScheduleUtils.getTruncatedTimeLine(null, DateUtils.getUTCDate(2015, 1, 21), DateUtils.getUTCDate(2017, 1, 21), true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartAfterEnd() {
    ScheduleUtils.getTruncatedTimeLine(SORTED_DATE_LIST, DateUtils.getUTCDate(2017, 1, 21), DateUtils.getUTCDate(2015, 1, 21), true);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStartEqualsEnd() {
    ScheduleUtils.getTruncatedTimeLine(SORTED_DATE_LIST, DateUtils.getUTCDate(2015, 1, 21), DateUtils.getUTCDate(2015, 1, 21), true);
  }

  @Test
  public void testStartDateAndEndDateBracketNoPoints() {
    final ZonedDateTime startDate = DateUtils.getUTCDate(2014, 3, 30);
    final ZonedDateTime endDate = DateUtils.getUTCDate(2014, 5, 30);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[] {startDate, endDate };
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testStartAndEndDateBracketAllPoints() {
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusMonths(9);
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 1].plusMonths(3);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N + 2];
    System.arraycopy(SORTED_DATE_LIST, 0, expectedResult, 1, N);
    expectedResult[0] = startDate;
    expectedResult[N + 1] = endDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testNonCoincidentStartDateWithinList() {
    final int n = 6;
    final ZonedDateTime startDate = SORTED_DATE_LIST[n].minusDays(4);
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 1].plusMonths(3);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N - n + 2];
    System.arraycopy(SORTED_DATE_LIST, n, expectedResult, 1, N - n);
    expectedResult[0] = startDate;
    expectedResult[expectedResult.length - 1] = endDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testCoincidentStartDateWithinList() {
    final int n = 12;
    final ZonedDateTime startDate = SORTED_DATE_LIST[n];
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 1].plusMonths(3);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N - n + 1];
    System.arraycopy(SORTED_DATE_LIST, n, expectedResult, 0, N - n);
    expectedResult[expectedResult.length - 1] = endDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testNonCoincidentEndDateWithinList() {
    final int n = 13;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusMonths(3);
    final ZonedDateTime endDate = SORTED_DATE_LIST[n].minusMonths(1);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[n + 2];
    System.arraycopy(SORTED_DATE_LIST, 0, expectedResult, 1, n);
    expectedResult[0] = startDate;
    expectedResult[expectedResult.length - 1] = endDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testCoincidentEndDateWithinList() {
    final int n = 45;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusMonths(4);
    final ZonedDateTime endDate = SORTED_DATE_LIST[n - 1];
    final ZonedDateTime[] expectedResult = new ZonedDateTime[n + 1];
    System.arraycopy(SORTED_DATE_LIST, 0, expectedResult, 1, n);
    expectedResult[0] = startDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testBothDatesNonCoincidentWithinList() {
    final ZonedDateTime startDate = SORTED_DATE_LIST[1].minusDays(2);
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 4].plusDays(6);
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N - 2];
    System.arraycopy(SORTED_DATE_LIST, 1, expectedResult, 1, N - 3);
    expectedResult[0] = startDate;
    expectedResult[expectedResult.length - 1] = endDate;
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testBothDatesCoincidentWithinList() {
    final ZonedDateTime startDate = SORTED_DATE_LIST[1];
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 4];
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N - 4];
    System.arraycopy(SORTED_DATE_LIST, 1, expectedResult, 0, N - 4);
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test
  public void testEqualDatesInOriginalList() {
    final ZonedDateTime[] duplicateDates = new ZonedDateTime[N * 2];
    System.arraycopy(SORTED_DATE_LIST, 0, duplicateDates, 0, N);
    System.arraycopy(SORTED_DATE_LIST, 0, duplicateDates, N, N);
    final ZonedDateTime startDate = SORTED_DATE_LIST[0];
    final ZonedDateTime endDate = SORTED_DATE_LIST[N - 1];
    final ZonedDateTime[] expectedResult = new ZonedDateTime[N];
    System.arraycopy(SORTED_DATE_LIST, 0, expectedResult, 0, N);
    testResults(expectedResult, UNSORTED_DATE_LIST, startDate, endDate, false);
    testResults(expectedResult, SORTED_DATE_LIST, startDate, endDate, true);
    testResults(expectedResult, CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
  }

  @Test(enabled = false)
  public void testDeprecatedUnsorted() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.getTruncatedTimeLineDeprecated(UNSORTED_DATE_LIST, startDate, endDate);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated unsorted:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testDeprecatedConcatenated() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.getTruncatedTimeLineDeprecated(CONCATENATED_SORTED_DATE_LIST, startDate, endDate);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated concatenated:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testDeprecatedSorted() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.getTruncatedTimeLineDeprecated(SORTED_DATE_LIST, startDate, endDate);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated sorted:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testDeprecatedCommon() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final ZonedDateTime[] input = new ZonedDateTime[CONCATENATED_SORTED_DATE_LIST.length + 2];
    System.arraycopy(CONCATENATED_SORTED_DATE_LIST, 0, input, 0, SORTED_DATE_LIST.length);
    input[SORTED_DATE_LIST.length] = startDate;
    input[SORTED_DATE_LIST.length + 1] = endDate;
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.getTruncatedTimeLineDeprecated(input, startDate, endDate);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated common:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testARefactoredUnsorted() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ScheduleUtils.getTruncatedTimeLine(UNSORTED_DATE_LIST, startDate, endDate, false);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored unsorted:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testARefactoredConcatenated() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ScheduleUtils.getTruncatedTimeLine(CONCATENATED_SORTED_DATE_LIST, startDate, endDate, false);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored concatenated:\t" + (endTime - startTime) / j * 1000);
  }

  @Test(enabled = false)
  public void testARefactoredSorted() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ScheduleUtils.getTruncatedTimeLine(SORTED_DATE_LIST, startDate, endDate, true);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored sorted:\t" + (endTime - startTime) / j * 1000000);
  }

  @Test(enabled = false)
  public void testARefactoredCommon() {
    int j = 0;
    final ZonedDateTime startDate = SORTED_DATE_LIST[0].minusDays(7);
    final ZonedDateTime endDate = SORTED_DATE_LIST[SORTED_DATE_LIST.length - 1].plusDays(7);
    final ZonedDateTime[] input = new ZonedDateTime[CONCATENATED_SORTED_DATE_LIST.length + 2];
    System.arraycopy(CONCATENATED_SORTED_DATE_LIST, 0, input, 0, SORTED_DATE_LIST.length);
    input[SORTED_DATE_LIST.length] = startDate;
    input[SORTED_DATE_LIST.length + 1] = endDate;
    final double startTime = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
      ScheduleUtils.getTruncatedTimeLine(input, startDate, endDate, false);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored common:\t" + (endTime - startTime) / j * 1000);
  }

  private void testResults(final ZonedDateTime[] expectedResult, final ZonedDateTime[] dates, final ZonedDateTime startDate, final ZonedDateTime endDate,
      final boolean sorted) {
    final ZonedDateTime[] truncatedDeprecated = DEPRECATED_CALCULATOR.getTruncatedTimeLineDeprecated(dates, startDate, endDate);
    assertEquals(expectedResult.length, truncatedDeprecated.length);
    assertDateArrayEquals(expectedResult, truncatedDeprecated);
    final ZonedDateTime[] truncated = ScheduleUtils.getTruncatedTimeLine(dates, startDate, endDate, sorted);
    assertEquals(expectedResult.length, truncated.length);
    assertDateArrayEquals(expectedResult, truncated);
    assertDateArrayEquals(truncatedDeprecated, truncated);
  }

}
