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

  private static final DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // -------------------------------------------------------------------------------------------

  // TODO : The integration schedules are returned as doubles. Should we return ZonedDateTime's instead (and then convert back to doubles internally)?
  // TODO : Add check that startTime < endTime in schedule generation routine

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the accrued leg calculation 
  public double[] constructCreditDefaultSwapAccruedLegIntegrationSchedule(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");

    // Do we want to include the CDS premium leg cashflow schedule as points
    boolean includeSchedule = true;

    // Get the start time of the CDS with respect to the current valuation date (can obviously be negative)
    double startTime = calculateCreditDefaultSwapStartTime(cds, dayCount);

    // Get the (offset) maturity of the CDS
    double offsetMaturityTime = calculateCreditDefaultSwapOffsetMaturity(cds, dayCount);

    // Calculate the schedule of integration timenodes for the accrued leg calculation
    double[] timeNodes = constructISDACompliantIntegrationSchedule(cds, yieldCurve, hazardRateCurve, startTime, offsetMaturityTime, includeSchedule);

    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the time nodes used to approximate the integral in the contingent leg calculation
  public double[] constructCreditDefaultSwapContingentLegIntegrationSchedule(LegacyCreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // Check input objects are not null
    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield Curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard Rate Curve");

    // Do we want to include the CDS premium leg cashflow schedule as points
    boolean includeSchedule = false;

    // Calculate the time at which protection starts
    double protectionStartTime = calculateProtectionStartTime(cds, dayCount);

    // Calculate the maturity of the CDS with respect to the valuation date
    double maturity = calculateCreditDefaultSwapMaturity(cds, dayCount);

    // Calculate the schedule of integration timenodes for the contingent leg calculation
    double[] timeNodes = constructISDACompliantIntegrationSchedule(cds, yieldCurve, hazardRateCurve, protectionStartTime, maturity, includeSchedule);

    return timeNodes;
  }

  // -------------------------------------------------------------------------------------------

  // Method to construct a set of timenodes compliant with the ISDA model (adapted from the RiskCare implementation)
  private double[] constructISDACompliantIntegrationSchedule(CreditDefaultSwapDefinition cds, ISDACurve yieldCurve, HazardRateCurve hazardRateCurve,
      double startTime, double endTime, boolean includeSchedule) {

    // ------------------------------------------------

    // Check input arguments are not null

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(yieldCurve, "Yield curve");
    ArgumentChecker.notNull(hazardRateCurve, "Hazard rate curve");

    ArgumentChecker.notNull(startTime, "Start time");
    ArgumentChecker.notNull(endTime, "End time");

    // ------------------------------------------------

    double offset = 0.0;

    GenerateCreditDefaultSwapPremiumLegSchedule premiumLegCashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    ZonedDateTime[] cashflowSchedule = premiumLegCashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    double[] cashflowScheduleAsDoubles = premiumLegCashflowSchedule.convertTenorsToDoubles(cashflowSchedule, cds.getValuationDate(), dayCount);

    NavigableSet<Double> allTimePoints = new TreeSet<Double>();

    Set<Double> timePointsInRange;

    double[] x = yieldCurve.getTimePoints();

    // ------------------------------------------------

    for (int i = 0; i < x.length; i++) {
      allTimePoints.add(new Double(x[i]));
    }

    x = hazardRateCurve.getShiftedTimePoints();

    for (int i = 0; i < x.length; i++) {
      allTimePoints.add(new Double(x[i]));
    }

    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));

    // ------------------------------------------------

    if (includeSchedule) {

      if (cds.getProtectionStart()) {
        offset = cds.getProtectionOffset();
      }

      double offsetStartTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cashflowSchedule[1], dayCount) - offset;
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

    Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);

    double[] timePoints = new double[boxed.length];

    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
    }

    // ------------------------------------------------

    return timePoints;
  }

  // -------------------------------------------------------------------------------------------

  public double calculateCreditDefaultSwapStartTime(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double startTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getStartDate(), dayCount);

    return startTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the step in time for the CDS (the point at which protection coverage starts)
  public double calculateCreditDefaultSwapStepinTime(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double stepInTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getValuationDate().plusDays(1), dayCount);

    return stepInTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset step in time for the CDS
  public double calculateCreditDefaultSwapOffsetStepinTime(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double offset = 0.0;
    double stepinTime = calculateCreditDefaultSwapStepinTime(cds, dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    double offsetStepinTime = stepinTime - offset;

    return offsetStepinTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the start time of the CDS contract with respect to the valuation date (can be negative obviously)
  public double calculateProtectionStartTime(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double startTime = calculateCreditDefaultSwapStartTime(cds, dayCount);
    double stepInTime = calculateCreditDefaultSwapStepinTime(cds, dayCount);

    double offsetPricingTime = -cds.getProtectionOffset();
    double offsetStepinTime = stepInTime - cds.getProtectionOffset();

    double protectionStartTime = Math.max(Math.max(startTime, offsetStepinTime), offsetPricingTime);

    return protectionStartTime;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the maturity of the CDS with respect to the valuation date
  public double calculateCreditDefaultSwapMaturity(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double maturity = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getMaturityDate(), dayCount);

    return maturity;
  }

  // -------------------------------------------------------------------------------------------------

  // Calculate the offset maturity for the CDS contract
  public double calculateCreditDefaultSwapOffsetMaturity(LegacyCreditDefaultSwapDefinition cds, DayCount dayCount) {

    double offset = 0.0;

    double maturity = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getMaturityDate(), dayCount);

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    double offsetMaturity = maturity + offset;

    return offsetMaturity;
  }

  // -------------------------------------------------------------------------------------------------
}
