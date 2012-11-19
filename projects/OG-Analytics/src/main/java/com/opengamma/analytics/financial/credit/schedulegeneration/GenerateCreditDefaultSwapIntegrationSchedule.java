/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to generate the integration schedules for instrument legs which require a numerical integration
 * (for example the contingent and accrued legs of a CDS require numerical evaluation of integrals)
 */
public class GenerateCreditDefaultSwapIntegrationSchedule {

  // -------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // -------------------------------------------------------------------------------------------

  // TODO : The integration schedules are returned as doubles. Should we return ZonedDateTime's instead (and then convert back to doubles internally)?
  // TODO : Add check that startTime < endTime in schedule generation routine

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the accrued leg calculation
  public double[] constructCreditDefaultSwapAccruedLegIntegrationSchedule(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve, final HazardRateCurve hazardRateCurve) {

    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");

    // Do we want to include the CDS premium leg cashflow schedule as points
    final boolean includeSchedule = true;

    // Get the start time of the CDS with respect to the current valuation date (can obviously be negative)
    final double startTime = calculateCreditDefaultSwapStartTime(valuationDate, cds, ACT_365);

    // Get the (offset) maturity of the CDS
    final double offsetMaturityTime = calculateCreditDefaultSwapOffsetMaturity(valuationDate, cds, ACT_365);

    // Calculate the schedule of integration timenodes for the accrued leg calculation
    final double[] timeNodes = constructISDACompliantIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, startTime, offsetMaturityTime, includeSchedule);

    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the contingent leg calculation
  public double[] constructCreditDefaultSwapContingentLegIntegrationSchedule(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve, final HazardRateCurve hazardRateCurve) {

    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");

    // Do we want to include the CDS premium leg cashflow schedule as points
    final boolean includeSchedule = false;

    // Calculate the time at which protection starts
    final double protectionStartTime = calculateProtectionStartTime(valuationDate, cds, ACT_365);

    // Calculate the maturity of the CDS with respect to the valuation date
    final double maturity = calculateCreditDefaultSwapMaturity(valuationDate, cds, ACT_365);

    // Calculate the schedule of integration timenodes for the contingent leg calculation
    final double[] timeNodes = constructISDACompliantIntegrationSchedule(valuationDate, cds, yieldCurve, hazardRateCurve, protectionStartTime, maturity, includeSchedule);

    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to construct a set of timenodes compliant with the ISDA model (adapted from the RiskCare implementation)
  private double[] constructISDACompliantIntegrationSchedule(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve, final double startTime, final double endTime, final boolean includeSchedule) {

    // ------------------------------------------------

    // Check input arguments are not null

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard rate curve");

    ArgumentChecker.notNull(startTime, "Start time");
    ArgumentChecker.notNull(endTime, "End time");

    // ------------------------------------------------

    double offset = 0.0;

    final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegCashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    final ZonedDateTime[] cashflowSchedule = premiumLegCashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    final double[] cashflowScheduleAsDoubles = premiumLegCashflowSchedule.convertTenorsToDoubles(cashflowSchedule, valuationDate, ACT_365);

    final NavigableSet<Double> allTimePoints = new TreeSet<Double>();

    Set<Double> timePointsInRange;

    double[] x = yieldCurve.getTimePoints();

    // ------------------------------------------------

    for (final double element : x) {
      allTimePoints.add(new Double(element));
    }

    x = hazardRateCurve.getShiftedTimePoints();

    for (final double element : x) {
      allTimePoints.add(new Double(element));
    }

    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));

    // ------------------------------------------------

    if (includeSchedule) {

      if (cds.getProtectionStart()) {
        offset = cds.getProtectionOffset();
      }

      final double offsetStartTime = TimeCalculator.getTimeBetween(valuationDate, cashflowSchedule[1], ACT_365) - offset;
      allTimePoints.add(new Double(offsetStartTime));

      double periodEndTime = 0.0;
      for (int i = 0; i < cashflowSchedule.length; i++) {

        if (i < cashflowSchedule.length - 1) {
          periodEndTime = cashflowScheduleAsDoubles[i] - offset;
        } else {
          periodEndTime = cashflowScheduleAsDoubles[i];
        }

        allTimePoints.add(new Double(periodEndTime));
      }

      timePointsInRange = allTimePoints.subSet(new Double(offsetStartTime), true, new Double(endTime), true);

    } else {
      timePointsInRange = allTimePoints.subSet(new Double(startTime), true, new Double(endTime), true);
    }

    // ------------------------------------------------

    final Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);

    final double[] timePoints = new double[boxed.length];

    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
    }

    // ------------------------------------------------

    return timePoints;
  }

  // -------------------------------------------------------------------------------------------

  public double calculateCreditDefaultSwapStartTime(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final DayCount dayCount) {

    final double startTime = TimeCalculator.getTimeBetween(valuationDate, cds.getStartDate(), dayCount);

    return startTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the step in time for the CDS (the point at which protection coverage starts)
  public double calculateCreditDefaultSwapStepinTime(final ZonedDateTime valuationDate, final DayCount dayCount) {

    final double stepInTime = TimeCalculator.getTimeBetween(valuationDate, valuationDate.plusDays(1), dayCount);

    return stepInTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset step in time for the CDS
  public double calculateCreditDefaultSwapOffsetStepinTime(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final DayCount dayCount) {

    double offset = 0.0;
    final double stepinTime = calculateCreditDefaultSwapStepinTime(valuationDate, dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    final double offsetStepinTime = stepinTime - offset;

    return offsetStepinTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the start time of the CDS contract with respect to the valuation date (can be negative obviously)
  public double calculateProtectionStartTime(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final DayCount dayCount) {

    final double startTime = calculateCreditDefaultSwapStartTime(valuationDate, cds, dayCount);
    final double stepInTime = calculateCreditDefaultSwapStepinTime(valuationDate, dayCount);

    final double offsetPricingTime = -cds.getProtectionOffset();
    final double offsetStepinTime = stepInTime - cds.getProtectionOffset();

    final double protectionStartTime = Math.max(Math.max(startTime, offsetStepinTime), offsetPricingTime);

    return protectionStartTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the maturity of the CDS with respect to the valuation date
  public double calculateCreditDefaultSwapMaturity(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final DayCount dayCount) {

    final double maturity = TimeCalculator.getTimeBetween(valuationDate, cds.getMaturityDate(), dayCount);

    return maturity;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset maturity for the CDS contract
  public double calculateCreditDefaultSwapOffsetMaturity(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final DayCount dayCount) {

    double offset = 0.0;

    final double maturity = TimeCalculator.getTimeBetween(valuationDate, cds.getMaturityDate(), dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    final double offsetMaturity = maturity + offset;

    return offsetMaturity;
  }

  // -------------------------------------------------------------------------------------------------
}
