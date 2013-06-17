/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.ISDAYieldCurveAndHazardRateCurveProvider;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.schedulegeneration.ScheduleUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate the accrued integration schedules
 */
public class ISDAAccruedLegIntegrationScheduleGenerator {

  // TODO : Add check that startTime < endTime in schedule generation routine

  // -------------------------------------------------------------------------------------------

  /**
   * Method to calculate the time nodes used to approximate the integral in the accrued leg calculation
   * @param cds {@link CreditDefaultSwapDefinition}
   * @param curves {@link ISDAYieldCurveAndHazardRateCurveProvider}
   * @return The time nodes
   */
  public ZonedDateTime[] constructCreditDefaultSwapAccruedLegIntegrationSchedule(final CreditDefaultSwapDefinition cds, final ISDAYieldCurveAndHazardRateCurveProvider curves) {
    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(curves, "curves");
    // Do we want to include the CDS premium leg cashflow schedule as points
    final ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime endDate = cds.getMaturityDate();
    if (cds.getProtectionStart()) {
      endDate = endDate.plusDays(1);
    }
    // Calculate the schedule of integration timenodes for the accrued leg calculation
    return ScheduleUtils.constructISDACompliantAccruedLegIntegrationSchedule(curves, startDate, endDate);
  }

}
