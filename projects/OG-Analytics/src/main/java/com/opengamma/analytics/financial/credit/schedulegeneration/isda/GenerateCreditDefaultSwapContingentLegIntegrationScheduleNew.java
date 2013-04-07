/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class GenerateCreditDefaultSwapContingentLegIntegrationScheduleNew {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");
  private static final GenerateCreditDefaultSwapPremiumLegScheduleNew SCHEDULE_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegScheduleNew();

  public Double[] constructCreditDefaultSwapContingentLegIntegrationSchedule(
      final ZonedDateTime valuationDate,
      final ZonedDateTime startDate,
      final ZonedDateTime endDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {
    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");
    // Do we want to include the CDS premium leg cashflow schedule as points
    final boolean includeSchedule = false;
    // Calculate the time at which protection starts
    double protectionStartTime = TimeCalculator.getTimeBetween(valuationDate, startDate, ACT_365);
    final double protectionEndTime = TimeCalculator.getTimeBetween(valuationDate, endDate, ACT_365);
    // Calculate the maturity of the CDS with respect to the valuation date
    if (Double.compare(protectionStartTime, -0.0) == 0) {
      protectionStartTime = 0;
    }
    // Calculate the schedule of integration timenodes for the contingent leg calculation
    return constructISDACompliantIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, protectionStartTime, /*maturity*/protectionEndTime, includeSchedule);
  }

  // Method to construct a set of timenodes compliant with the ISDA model (adapted from the good RiskCare implementation)

  private Double[] constructISDACompliantIntegrationSchedule(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final double startTime,
      final double endTime,
      final boolean includeSchedule) {
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard rate curve");
    ArgumentChecker.notNull(startTime, "Start time");
    ArgumentChecker.notNull(endTime, "End time");
    // TODO : Check ordering of startTime and endTime
    // TODO : Move these lines into the includeSchedule code as they are only used there
    final ZonedDateTime[] cashflowSchedule = SCHEDULE_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final double[] cashflowScheduleAsDoubles = SCHEDULE_CALCULATOR.convertTenorsToDoubles(cashflowSchedule, valuationDate, ACT_365);



    // ------------------------------------------------
    // All the timenodes in the list
    final double[] yieldCurveTimes = yieldCurve.getTimePoints();
    final double[] hazardRateCurveTimes = hazardRateCurve.getShiftedTimePoints();
    final int nYieldCurveTimes = yieldCurveTimes.length;
    final int nHazardRateCurveTimes = hazardRateCurveTimes.length;
    final double[] allTimePoints = new double[nYieldCurveTimes + nHazardRateCurveTimes];
    System.arraycopy(yieldCurveTimes, 0, allTimePoints, 0, nYieldCurveTimes);
    System.arraycopy(hazardRateCurveTimes, 0, allTimePoints, nYieldCurveTimes, nHazardRateCurveTimes);
    //    final NavigableSet<Double> allTimePoints = new TreeSet<>();
    //    // The subset of timenodes in the range over which protection extends
    //    Set<Double> timePointsInRange;
    //    // Add the timenodes on the rates curve
    //    double[] x = yieldCurve.getTimePoints();
    //    for (final double element : x) {
    //      allTimePoints.add(new Double(element));
    //    }
    //    // Add the timenodes on the hazard rate curve
    //    x = hazardRateCurve.getShiftedTimePoints();
    //    for (final double element : x) {
    //      allTimePoints.add(new Double(element));
    //    }
    //    // TODO : There is a known bug observed when adding endTime to the list, if endTime is very close (numerically) to one of the entries in x
    //    // TODO : This leads to two numbers which differ at ~O(10^-15). This causes an error in the contingent leg calc leading to a NaN value
    //    // Add the timenodes at the times when protection starts and ends
    //    allTimePoints.add(new Double(startTime));
    //    allTimePoints.add(new Double(endTime));
    //    if (includeSchedule) {
    //      double offset = 0.0;
    //      if (cds.getProtectionStart()) {
    //        offset = cds.getProtectionOffset();
    //      }
    //      final double offsetStartTime = TimeCalculator.getTimeBetween(valuationDate, cashflowSchedule[1], ACT_365) - offset;
    //      allTimePoints.add(new Double(offsetStartTime));
    //      double periodEndTime = 0.0;
    //      for (int i = 0; i < cashflowSchedule.length; i++) {
    //        if (i < cashflowSchedule.length - 1) {
    //          periodEndTime = cashflowScheduleAsDoubles[i] - offset;
    //        } else {
    //          periodEndTime = cashflowScheduleAsDoubles[i];
    //        }
    //        allTimePoints.add(new Double(periodEndTime));
    //      }
    //      timePointsInRange = allTimePoints.subSet(new Double(offsetStartTime), true, new Double(endTime), true);
    //    } else {
    //      timePointsInRange = allTimePoints.subSet(new Double(startTime), true, new Double(endTime), true);
    //    }
    return ScheduleUtils.getTruncatedTimeLine(allTimePoints, startTime, endTime, false, 1e-15);
    //    final Double[] boxed = new Double[timePointsInRange.size()];
    //    timePointsInRange.toArray(boxed);
    //    final double[] timePoints = new double[boxed.length];
    //    for (int i = 0; i < boxed.length; ++i) {
    //      timePoints[i] = boxed[i].doubleValue();
    //    }
    //    return timePoints;
  }
}
