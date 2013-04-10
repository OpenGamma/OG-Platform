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
    // Calculate the time at which protection starts
    double protectionStartTime = TimeCalculator.getTimeBetween(valuationDate, startDate, ACT_365);
    final double protectionEndTime = TimeCalculator.getTimeBetween(valuationDate, endDate, ACT_365);
    // Calculate the maturity of the CDS with respect to the valuation date
    if (Double.compare(protectionStartTime, -0.0) == 0) {
      protectionStartTime = 0;
    }
    // Calculate the schedule of integration timenodes for the contingent leg calculation
    return constructISDACompliantIntegrationSchedule(cds, yieldCurve, hazardRateCurve, protectionStartTime, protectionEndTime);
  }

  // Method to construct a set of timenodes compliant with the ISDA model (adapted from the good RiskCare implementation)

  private Double[] constructISDACompliantIntegrationSchedule(final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve, final double startTime,
      final double endTime) {
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard rate curve");
    ArgumentChecker.notNull(startTime, "Start time");
    ArgumentChecker.notNull(endTime, "End time");
    // TODO : Check ordering of startTime and endTime
    // TODO : Move these lines into the includeSchedule code as they are only used there
    // All the timenodes in the list
    final double[] yieldCurveTimes = yieldCurve.getTimePoints();
    final double[] hazardRateCurveTimes = hazardRateCurve.getShiftedTimePoints();
    final int nYieldCurveTimes = yieldCurveTimes.length;
    final int nHazardRateCurveTimes = hazardRateCurveTimes.length;
    final double[] allTimePoints = new double[nYieldCurveTimes + nHazardRateCurveTimes];
    System.arraycopy(yieldCurveTimes, 0, allTimePoints, 0, nYieldCurveTimes);
    System.arraycopy(hazardRateCurveTimes, 0, allTimePoints, nYieldCurveTimes, nHazardRateCurveTimes);
    return ScheduleUtils.getTruncatedTimeLine(allTimePoints, startTime, endTime, false, 1e-15);
  }
}
