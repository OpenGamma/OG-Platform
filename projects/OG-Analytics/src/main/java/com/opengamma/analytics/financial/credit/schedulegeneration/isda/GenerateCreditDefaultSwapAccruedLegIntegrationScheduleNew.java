/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate the integration schedules for instrument legs which require a numerical integration
 * (for example the contingent and accrued legs of a CDS require numerical evaluation of integrals)
 */
public class GenerateCreditDefaultSwapAccruedLegIntegrationScheduleNew {
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // TODO : Add check that startTime < endTime in schedule generation routine

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the accrued leg calculation

  public ZonedDateTime[] constructCreditDefaultSwapAccruedLegIntegrationSchedule(final CreditDefaultSwapDefinition cds, final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {
    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");
    // Do we want to include the CDS premium leg cashflow schedule as points
    final ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime endDate = cds.getMaturityDate();
    if (cds.getProtectionStart()) {
      endDate = endDate.plusDays(1);
    }
    // Calculate the schedule of integration timenodes for the accrued leg calculation
    return constructISDACompliantAccruedLegIntegrationSchedule(yieldCurve, hazardRateCurve, startDate, endDate);
  }

  // This is a total hack just to get the accrued leg calculation working - need to re-merge with the other functions
  // This is actually how I want to re-write the other function

  private ZonedDateTime[] constructISDACompliantAccruedLegIntegrationSchedule(final ISDADateCurve yieldCurve, final HazardRateCurve hazardRateCurve,
      final ZonedDateTime startDate, final ZonedDateTime endDate) {
    final ZonedDateTime[] yieldCurveDates = yieldCurve.getCurveDates();
    final ZonedDateTime[] hazardCurveDates = hazardRateCurve.getCurveTenors();
    final int nYieldCurveDates = yieldCurveDates.length;
    final int nHazardCurveDates = hazardCurveDates.length;
    final int total = nYieldCurveDates + nHazardCurveDates;
    final ZonedDateTime[] result = new ZonedDateTime[total];
    System.arraycopy(yieldCurveDates, 0, result, 0, nYieldCurveDates);
    System.arraycopy(hazardCurveDates, 0, result, nYieldCurveDates, nHazardCurveDates);
    return ScheduleUtils.getTruncatedTimeLine(result, startDate, endDate, false);
  }

}
