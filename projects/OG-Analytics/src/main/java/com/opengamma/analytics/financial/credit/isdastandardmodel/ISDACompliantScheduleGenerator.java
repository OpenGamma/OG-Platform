/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ISDACompliantScheduleGenerator {

  private static final DayCount ACT365 = DayCounts.ACT_365;

  /**
   * This mimics JpmcdsRiskyTimeLine from the ISDA model in c
   * @param startDate start date
   * @param endDate end date
   * @param disCurveDates all the points in the discount curve
   * @param spreadCurveDates all the points in the risky curve
   * @return An ascending array of dates that is the unique combination of all the input dates (including startDate and endDate) that are not strictly
   * before startDate or after EndDate (hence startDate will be the first entry and enddate the last). 
   */
  public static LocalDate[] getIntegrationNodesAsDates(final LocalDate startDate, final LocalDate endDate, final LocalDate[] disCurveDates, final LocalDate[] spreadCurveDates) {
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(endDate, "null endDate");
    ArgumentChecker.noNulls(disCurveDates, "nulls in disCurveDates");
    ArgumentChecker.noNulls(spreadCurveDates, "nulls in spreadCurveDates");

    ArgumentChecker.isTrue(endDate.isAfter(startDate), "endDate of {} is not after startDate of {}", endDate.toString(), startDate.toString());

    final int nDisCurvePoints = disCurveDates.length;
    final int nSpreadCurvePoints = spreadCurveDates.length;

    final LinkedHashSet<LocalDate> set = new LinkedHashSet<>(2 + nDisCurvePoints + nSpreadCurvePoints);
    set.add(startDate);
    for (final LocalDate date : disCurveDates) {
      set.add(date);
    }
    for (final LocalDate date : spreadCurveDates) {
      set.add(date);
    }
    set.add(endDate);

    final int n = set.size();
    final LocalDate[] res = new LocalDate[n];
    set.toArray(res);
    Arrays.sort(res);

    // remove dates strictly before startDate and strictly after endDate
    int a = 0;
    int b = n - 1;
    while (res[a].isBefore(startDate)) {
      a++;
    }
    while (res[b].isAfter(endDate)) {
      b--;
    }
    final int newLength = b - a + 1;
    if (newLength == n) {
      return res; // nothing got chopped off
    }

    final LocalDate[] res2 = new LocalDate[newLength];
    System.arraycopy(res, a, res2, 0, newLength);
    return res2;
  }

  /**
   * This calls getIntegrationNodesAsDates to get an array of dates then calculates the year fraction from today to those points using 
   * ACT/365
   * @param today the date to measure year-fractions from. Must NOT have today after startDate 
   * @param startDate start date
   * @param endDate end date
   * @param disCurveDates all the points in the discount curve
   * @param spreadCurveDates all the points in the risky curve
   * @return An ascending array of times from today @see getIntegrationNodesAsDates
   */
  public static double[] getIntegrationNodesAsTimes(final LocalDate today, final LocalDate startDate, final LocalDate endDate, final LocalDate[] disCurveDates, final LocalDate[] spreadCurveDates) {

    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.isFalse(today.isAfter(startDate), "today is after startDate");

    final LocalDate[] dates = getIntegrationNodesAsDates(startDate, endDate, disCurveDates, spreadCurveDates);
    return getYearFractionFromToday(today, dates);
  }

  /**
   * Truncate an sort (ascending) array of dates so the the interior values are strictly after startDate and strictly before endEnd,
   * and startDate and endDate becomes to first and last entries 
   * @param startDate This will be the first value in the list 
   * @param endDate This will be the last value in the list
   * @param dateList Must be sorted 
   * @return dates between startDate and endDate
   */
  public static LocalDate[] truncateList(final LocalDate startDate, final LocalDate endDate, final LocalDate[] dateList) {
    ArgumentChecker.notNull(startDate, "null startDate");
    ArgumentChecker.notNull(endDate, "null endDate");
    ArgumentChecker.noNulls(dateList, "nulls in dateList");
    ArgumentChecker.isTrue(endDate.isAfter(startDate), "require enddate after startDate");
    final int n = dateList.length;
    if (n == 0) {
      return new LocalDate[] {startDate, endDate };
    }

    final List<LocalDate> temp = new ArrayList<>(n + 2);
    for (final LocalDate d : dateList) {
      if (d.isAfter(startDate) && d.isBefore(endDate)) {
        temp.add(d);
      }
    }

    final int m = temp.size();
    final LocalDate[] tArray = new LocalDate[m];
    temp.toArray(tArray);
    final LocalDate[] res = new LocalDate[m + 2];
    res[0] = startDate;
    System.arraycopy(tArray, 0, res, 1, m);
    res[m + 1] = endDate;
    return res;
  }

  /**
   * Year fractions from a fixed date to a set of dates using ACT/365
   * @param today the date to measure from 
   * @param dates set of dates to measure to 
   * @return set of yearfractions (array of double)
   */
  public static double[] getYearFractionFromToday(final LocalDate today, final LocalDate[] dates) {
    return getYearFractionFromToday(today, dates, ACT365);
  }

  /**
   * Year fractions from a fixed date to a set of dates using the specified day-count
   * @param today the date to measure from 
   * @param dates set of dates to measure to 
   * @param dayCount The day-count
   * @return  set of yearfractions (array of double)
   */
  public static double[] getYearFractionFromToday(final LocalDate today, final LocalDate[] dates, final DayCount dayCount) {
    ArgumentChecker.notNull(today, "null today");
    ArgumentChecker.noNulls(dates, "nulls in dates");
    ArgumentChecker.notNull(dayCount, "null dayCount");

    final int n = dates.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = dayCount.getDayCountFraction(today, dates[i]);
    }

    return res;
  }

}
