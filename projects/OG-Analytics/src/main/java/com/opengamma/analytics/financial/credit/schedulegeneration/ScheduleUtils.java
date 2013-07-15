/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import it.unimi.dsi.fastutil.doubles.DoubleLinkedOpenHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;

/**
 * Class containing utility functions associated with schedule generation for credit instruments
 */
public final class ScheduleUtils {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to check the logic of calculateWorkday more thoroughly

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private ScheduleUtils() {
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Function to compute the next business day that is spotDays after baseDate (takes into account we may have e.g. weekends in between working days)

  public static ZonedDateTime calculateWorkday(
      final CreditDefaultSwapDefinition cds,
      final ZonedDateTime baseDate,
      final int spotDays) {

    ArgumentChecker.notNegative(spotDays, "Cash settlement days");

    ZonedDateTime requiredDate = baseDate;

    if (spotDays > 0) {
      int n = 0;

      for (int i = 0; i < spotDays; i++) {

        requiredDate = requiredDate.plusDays(1);

        if (!cds.getCalendar().isWorkingDay(requiredDate.toLocalDate())) {
          n++;
        }
      }

      requiredDate = requiredDate.plusDays(n);

      while (!cds.getCalendar().isWorkingDay(requiredDate.toLocalDate())) {
        requiredDate = requiredDate.plusDays(1);
      }
    }

    return requiredDate;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public static ZonedDateTime[] getTruncatedTimeLine(final ZonedDateTime[] allDates, final ZonedDateTime startDate, final ZonedDateTime endDate,
      final boolean sorted) {
    ArgumentChecker.notNull(allDates, "all dates");
    ArgumentChecker.notNull(startDate, "start date");
    ArgumentChecker.notNull(endDate, "end date");
    ArgumentChecker.isTrue(startDate.isBefore(endDate), "Start date {} must be before end date {}", startDate, endDate);
    final int n = allDates.length;
    if (n == 0) {
      return new ZonedDateTime[] {startDate, endDate };
    }
    final HashSet<ZonedDateTime> truncated = new LinkedHashSet<>(n + 2, 1);
    final LocalDate startDateAsLocal = startDate.toLocalDate();
    final LocalDate endDateAsLocal = endDate.toLocalDate();
    for (final ZonedDateTime date : allDates) {
      final LocalDate localDate = date.toLocalDate();
      if (!(localDate.isBefore(startDateAsLocal) || localDate.isAfter(endDateAsLocal))) {
        truncated.add(date);
      }
    }
    final int truncatedSize = truncated.size();
    if (truncatedSize == 0) {
      return new ZonedDateTime[] {startDate, endDate };
    }
    final ZonedDateTime[] truncatedArray = truncated.toArray(new ZonedDateTime[truncatedSize]);
    if (!sorted) {
      Arrays.sort(truncatedArray);
    }
    if (truncatedArray[0].equals(startDate)) {
      if (truncatedArray[truncatedSize - 1].equals(endDate)) {
        return truncatedArray;
      }
      final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 0, truncatedSize);
      result[truncatedSize] = endDate;
      return result;
    }
    if (truncatedArray[truncatedSize - 1].equals(endDate)) {
      final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
      result[0] = startDate;
      return result;
    }
    final ZonedDateTime[] result = new ZonedDateTime[truncatedSize + 2];
    System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
    result[0] = startDate;
    result[truncatedSize + 1] = endDate;
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public static double[] getTruncatedTimeLine(final double[] allTimes, final double startTime, final double endTime, final boolean sorted,
      final double tolerance) {
    ArgumentChecker.notNull(allTimes, "all dates");
    ArgumentChecker.isTrue(startTime < endTime, "Start time {} must be before end time {}", startTime, endTime);
    final int n = allTimes.length;
    if (n == 0) {
      return new double[] {startTime, endTime };
    }
    final DoubleLinkedOpenHashSet truncated = new DoubleLinkedOpenHashSet(n + 2, 1);
    for (final Double time : allTimes) {
      if (time > startTime && time < endTime) {
        truncated.add(time);
      }
    }
    final int truncatedSize = truncated.size();
    if (truncatedSize == 0) {
      return new double[] {startTime, endTime };
    }
    final double[] truncatedArray = truncated.toDoubleArray();
    if (!sorted) {
      Arrays.sort(truncatedArray);
    }
    if (CompareUtils.closeEquals(truncatedArray[0], startTime, tolerance)) {
      if (CompareUtils.closeEquals(truncatedArray[truncatedSize - 1], endTime, tolerance)) {
        return truncatedArray;
      }
      final double[] result = new double[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 0, truncatedSize);
      result[truncatedSize] = endTime;
      return result;
    }
    if (CompareUtils.closeEquals(truncatedArray[truncatedSize - 1], endTime, tolerance)) {
      final double[] result = new double[truncatedSize + 1];
      System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
      result[0] = startTime;
      return result;
    }
    final double[] result = new double[truncatedSize + 2];
    System.arraycopy(truncatedArray, 0, result, 1, truncatedSize);
    result[0] = startTime;
    result[truncatedSize + 1] = endTime;
    return result;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public static ZonedDateTime[] constructISDACompliantAccruedLegIntegrationSchedule(final ISDAYieldCurveAndHazardRateCurveProvider curves,
      final ZonedDateTime startDate, final ZonedDateTime endDate) {
    final ZonedDateTime[] yieldCurveDates = curves.getYieldCurve().getCurveDates();
    final ZonedDateTime[] hazardCurveDates = curves.getHazardRateCurve().getCurveTenors();
    final int nYieldCurveDates = yieldCurveDates.length;
    final int nHazardCurveDates = hazardCurveDates.length;
    final int total = nYieldCurveDates + nHazardCurveDates;
    final ZonedDateTime[] result = new ZonedDateTime[total];
    System.arraycopy(yieldCurveDates, 0, result, 0, nYieldCurveDates);
    System.arraycopy(hazardCurveDates, 0, result, nYieldCurveDates, nHazardCurveDates);
    return ScheduleUtils.getTruncatedTimeLine(result, startDate, endDate, false);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
