/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate the contingent integration schedules
 */
public class ISDAContingentLegIntegrationScheduleGenerator {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  /**
   * Method to calculate the time nodes used to approximate the integral in the contingent leg calculation
   * @param valuationDate The valuation date 
   * @param startDate The start date 
   * @param endDate The end date
   * @param cds {@link CreditDefaultSwapDefinition}
   * @param curves {@link ISDAYieldCurveAndHazardRateCurveProvider}
   * @return The time nodes
   */
  public double[] constructCreditDefaultSwapContingentLegIntegrationSchedule(final ZonedDateTime valuationDate, final ZonedDateTime startDate,
      final ZonedDateTime endDate, final CreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider curves) {
    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(curves, "curves");
    // Calculate the time at which protection starts
    double protectionStartTime = TimeCalculator.getTimeBetween(valuationDate, startDate, ACT_365);
    final double protectionEndTime = TimeCalculator.getTimeBetween(valuationDate, endDate, ACT_365);
    // Calculate the maturity of the CDS with respect to the valuation date
    if (Double.compare(protectionStartTime, -0.0) == 0) {
      protectionStartTime = 0;
    }
    return constructISDACompliantIntegrationSchedule(curves, protectionStartTime, protectionEndTime);
  }

  private double[] constructISDACompliantIntegrationSchedule(final ISDAYieldCurveAndHazardRateCurveProvider curves, final double startTime,
      final double endTime) {
    final double[] yieldCurveTimes = curves.getYieldCurve().getTimePoints();
    final double[] hazardRateCurveTimes = curves.getHazardRateCurve().getShiftedTimePoints();
    final int nYieldCurveTimes = yieldCurveTimes.length;
    final int nHazardRateCurveTimes = hazardRateCurveTimes.length;
    final double[] allTimePoints = new double[nYieldCurveTimes + nHazardRateCurveTimes];
    System.arraycopy(yieldCurveTimes, 0, allTimePoints, 0, nYieldCurveTimes);
    System.arraycopy(hazardRateCurveTimes, 0, allTimePoints, nYieldCurveTimes, nHazardRateCurveTimes);
    return ScheduleUtils.getTruncatedTimeLine(allTimePoints, startTime, endTime, false, 1e-15);
  }

}
