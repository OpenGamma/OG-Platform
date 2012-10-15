/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.IMMDates;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods to generate the premium leg cashflow schedule for a CDS (following the market conventions for CDS)
 */
public class GenerateCreditDefaultSwapPremiumLegSchedule {

  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Act/360");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // -------------------------------------------------------------------------------------------

  // TODO : Add a check if the calendar is 'null' to signify no adjustment of business dates? Overloaded method  
  // TODO : Fix the code for the back stubs (is there a better way of handling this than just duplicating code?) - front stubs seem to work okay
  // TODO : Add the code for the first coupon's
  // TODO : Should convertDatesToDoubles be put somewhere else? To use it, need to create a GenerateCreditDefaultSwapPremiumLegSchedule object which is a bit wasteful
  // TODO : Look at DateAdjuster class for IMM date handling
  // TODO : FrontLong stubs - e.g. startDate = 20/12/2007, effDate = 21/12/2007; first coupon at 20/6/2008. ISDA model first coupon at 20/3/2008 (seems to use start date not eff date)
  // TODO : Remove one of the overloaded convertdatesToDoubles methods

  // -------------------------------------------------------------------------------------------

  // Public method to generate the premium leg cashflow schedule from the input CDS contract specification
  public ZonedDateTime[] constructCreditDefaultSwapPremiumLegSchedule(CreditDefaultSwapDefinition cds) {

    // ------------------------------------------------

    // Check input CDS object is not null
    ArgumentChecker.notNull(cds, "CDS");

    // ------------------------------------------------

    // First, adjust the (user input) effective date for non-business days (currently assuming the user chooses the effective date to be anything they want)
    ZonedDateTime adjustedEffectiveDate = getAdjustedEffectiveDate(cds);

    // ------------------------------------------------

    // Second, adjust the maturity date so that it falls on the next IMM date (if the user specifies this to be so)
    ZonedDateTime adjustedMaturityDate = getAdjustedMaturityDate(cds);

    // ------------------------------------------------

    // Third, construct the schedule of premium leg cashflows given the adjusted effective and adjusted maturity dates and other contract specifications
    ZonedDateTime[] cashflowSchedule = calculateCashflowDates(cds, adjustedEffectiveDate, adjustedMaturityDate);

    // ------------------------------------------------

    // Fourth, business day adjust the previously generated schedule cashflow dates
    cashflowSchedule = businessDayAdjustcashflowSchedule(cds, cashflowSchedule);

    // ------------------------------------------------

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Public method to generate a set of timenodes compliant with the ISDA model (adapted from the RiskCare implementation)
  public double[] constructISDACompliantCashflowSchedule(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve,
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

    ZonedDateTime[] cashflowSchedule = this.constructCreditDefaultSwapPremiumLegSchedule(cds);

    double[] cashflowScheduleAsDoubles = convertTenorsToDoubles(cashflowSchedule, cds.getValuationDate(), ACT_365);

    NavigableSet<Double> allTimePoints = new TreeSet<Double>();

    Double[] x = yieldCurve.getCurve().getXData();

    for (int i = 0; i < x.length; i++) {
      allTimePoints.add(new Double(x[i]));
    }

    x = hazardRateCurve.getCurve().getXData();

    for (int i = 0; i < x.length; i++) {
      allTimePoints.add(new Double(x[i]));
    }

    allTimePoints.add(new Double(startTime));
    allTimePoints.add(new Double(endTime));

    Set<Double> timePointsInRange;

    if (includeSchedule) {

      final int maturityIndex = cashflowSchedule.length - 1;

      if (cds.getProtectionStart()) {
        offset = cds.getProtectionOffset();
      }

      double offsetStartTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cashflowSchedule[1], ACT_365) - offset;
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

    Double[] boxed = new Double[timePointsInRange.size()];
    timePointsInRange.toArray(boxed);

    double[] timePoints = new double[boxed.length];

    for (int i = 0; i < boxed.length; ++i) {
      timePoints[i] = boxed[i].doubleValue();
    }

    return timePoints;
  }

  // -------------------------------------------------------------------------------------------

  // Public method to extract just the adjusted effective date for a CDS according to the contract specifications (don't have to go through the whole schedule gen process)
  public ZonedDateTime getAdjustedEffectiveDate(CreditDefaultSwapDefinition cds) {

    // Check input CDS object is not null
    ArgumentChecker.notNull(cds, "CDS");

    // Set the adjusted effective date to be equal to the (user input) effective date
    ZonedDateTime adjustedEffectiveDate = cds.getEffectiveDate();

    // If required adjust the effective date for non-business days using the appropriate non-business day convention
    if (cds.getAdjustEffectiveDate()) {
      adjustedEffectiveDate = businessDayAdjustDate(cds.getEffectiveDate(), cds.getCalendar(), cds.getBusinessDayAdjustmentConvention());
    }

    return adjustedEffectiveDate;
  }

  // -------------------------------------------------------------------------------------------

  //Public method to extract just the adjusted maturity date for a CDS according to the contract specifications (don't have to go through the whole schedule gen process)
  public ZonedDateTime getAdjustedMaturityDate(CreditDefaultSwapDefinition cds) {

    // Check input CDS object is not null
    ArgumentChecker.notNull(cds, "CDS");

    // Set the adjusted maturity date to be equal to the (user input) maturity date
    ZonedDateTime adjustedMaturityDate = cds.getMaturityDate();

    // If specified by the user adjust the maturity date to fall on the next IMM date
    if (cds.getIMMAdjustMaturityDate()) {
      adjustedMaturityDate = immAdjustDate(cds.getMaturityDate());
    }

    return adjustedMaturityDate;
  }

  // -------------------------------------------------------------------------------------------

  // Public method to convert the input ZonedDateTime tenors into doubles based on the CDS contract properties
  public double[] convertTenorsToDoubles(CreditDefaultSwapDefinition cds, ZonedDateTime[] tenors) {

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(tenors, "tenors");

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {
      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(cds.getValuationDate(), tenors[i], cds.getDayCountFractionConvention());
    }

    return tenorsAsDoubles;
  }

  // Public method to convert the input ZonedDateTime tenors into doubles relative to the specified date based on the daycount convention specified
  public double[] convertTenorsToDoubles(ZonedDateTime[] tenors, ZonedDateTime baselineDate, DayCount dayCountConvention) {

    ArgumentChecker.notNull(dayCountConvention, "Daycount convention");
    ArgumentChecker.notNull(baselineDate, "Baseline date");
    ArgumentChecker.notNull(tenors, "tenors");

    int numberOfTenors = tenors.length;

    double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {
      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(baselineDate, tenors[i], dayCountConvention);
    }

    return tenorsAsDoubles;
  }

  // -------------------------------------------------------------------------------------------

  // Function to return a vector of daycount fractions given an input cashflow schedule
  private double[] calculateDaycountFraction(ZonedDateTime adjustedEffectiveDate, ZonedDateTime[][] cashflowSchedule, DayCount daycountFractionConvention) {

    ArgumentChecker.notNull(adjustedEffectiveDate, "Adjusted effective date");
    ArgumentChecker.notNull(cashflowSchedule, "Cashflow schedule");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount convention");

    int numberOfCashflows = cashflowSchedule.length;

    double[] dcf = new double[numberOfCashflows];

    for (int i = 1; i < numberOfCashflows; i++) {
      dcf[i] = ACT_360.getDayCountFraction(cashflowSchedule[i - 1][0], cashflowSchedule[i][0]);
    }

    return dcf;
  }

  // -------------------------------------------------------------------------------------------

  // Method to business day adjust an input cashflow schedule according to a specified convention
  private ZonedDateTime[] businessDayAdjustcashflowSchedule(CreditDefaultSwapDefinition cds, ZonedDateTime[] cashflowSchedule) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(cashflowSchedule, "Cashflow schedule");

    // -------------------------------------------------------------------------------

    // Get the business day calendar
    Calendar calendar = cds.getCalendar();

    // Get the convention for adjusting non-business days
    BusinessDayConvention businessdayAdjustmentConvention = cds.getBusinessDayAdjustmentConvention();

    int numberOfCashflows = cashflowSchedule.length;

    ZonedDateTime[] adjustedCashflowSchedule = new ZonedDateTime[numberOfCashflows];

    // -------------------------------------------------------------------------------

    // Business day adjust all of the cashflow dates except the final one (which is handled seperately)
    for (int i = 0; i < numberOfCashflows - 1; i++) {
      adjustedCashflowSchedule[i] = businessDayAdjustDate(cashflowSchedule[i], calendar, businessdayAdjustmentConvention);
    }

    // Determine if we need to business day adjust the final cashflow or not
    if (cds.getAdjustMaturityDate()) {
      adjustedCashflowSchedule[numberOfCashflows - 1] = businessDayAdjustDate(cashflowSchedule[numberOfCashflows - 1], calendar, businessdayAdjustmentConvention);
    } else {
      adjustedCashflowSchedule[numberOfCashflows - 1] = cashflowSchedule[numberOfCashflows - 1];
    }

    // -------------------------------------------------------------------------------

    return adjustedCashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the premium leg cashflow dates given the adjusted effective and maturity dates 
  private ZonedDateTime[] calculateCashflowDates(CreditDefaultSwapDefinition cds, ZonedDateTime adjustedEffectiveDate, ZonedDateTime adjustedMaturityDate) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(adjustedEffectiveDate, "Adjusted effective date");
    ArgumentChecker.notNull(adjustedMaturityDate, "Adjusted maturity date");

    // -------------------------------------------------------------------------------

    // Get the coupon stub type
    StubType stubType = cds.getStubType();

    // Get the coupon frequency
    PeriodFrequency couponFrequency = cds.getCouponFrequency();

    // Compute the number of cashflows in the premium leg schedule (based on the adjusted effective and maturity dates and the coupon frequency and stub type)
    int numberOfCashflows = calculateNumberOfPremiumLegCashflows(adjustedEffectiveDate, adjustedMaturityDate, couponFrequency, stubType);

    // Build the cashflow schedule (include the node at the effective date even though there is no cashflow on this date)
    ZonedDateTime[] cashflowSchedule = new ZonedDateTime[numberOfCashflows + 1];

    // -------------------------------------------------------------------------------

    // The stub is at the front of the premium leg schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {

      // Start at the adjusted maturity of the contract
      ZonedDateTime cashflowDate = adjustedMaturityDate;

      // Note the order of the loop and the termination condition (i > 0 not i = 0)
      for (int i = numberOfCashflows; i > 0; i--) {

        // Store the date (note this is at the top of the loop)
        cashflowSchedule[i] = cashflowDate;

        // Step back in time by the specified number of months
        cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());
      }

      // Append the timenode at the adjusted effective date at the beginning of the cashflow schedule vector
      cashflowSchedule[0] = adjustedEffectiveDate;
    }

    // -------------------------------------------------------------------------------

    // The stub is at the back of the premium leg schedule
    if (stubType == StubType.BACKSHORT || stubType == StubType.BACKLONG) {

      // Start at the adjusted effective date of the contract
      ZonedDateTime cashflowDate = adjustedEffectiveDate;

      for (int i = 0; i < numberOfCashflows; i++) {

        // Store the date (note this is at the top of the loop)
        cashflowSchedule[i] = cashflowDate;

        // Step forward in time by the specified number of months
        cashflowDate = cashflowDate.plus(couponFrequency.getPeriod());
      }
    }

    // -------------------------------------------------------------------------------

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to determine if a date supplied is an IMM date
  private boolean isAnIMMDate(ZonedDateTime date) {

    ArgumentChecker.notNull(date, "date");

    boolean returnValue = false;

    // Get the year of the date supplied
    final int year = date.getYear();

    // Construct an object with the IMM dates for 'year'
    IMMDates immDates = new IMMDates(year);

    // Test if 'date' is equal to one of the IMM dates
    if (date.equals(immDates.getImmDateMarch()) || date.equals(immDates.getImmDateJune()) || date.equals(immDates.getImmDateSeptember()) || date.equals(immDates.getImmDateDecember())) {
      returnValue = true;
    }

    return returnValue;
  }

  // -------------------------------------------------------------------------------------------

  // Method to adjust the specified maturity date to the next IMM date
  private ZonedDateTime immAdjustDate(ZonedDateTime maturityDate) {

    // Check that the input date is not null
    ArgumentChecker.notNull(maturityDate, "Maturity date");

    // Start at the current maturityDate
    ZonedDateTime immAdjustedMaturityDate = maturityDate;

    // Get the year of the contract maturity
    final int year = maturityDate.getYear();

    // Construct an IMM object with the IMM dates for 'year'
    IMMDates immDates = new IMMDates(year);

    // -------------------------------------------------------------------

    // First of all check that the maturity date isn't one of the IMM dates for 'year'

    if (maturityDate.equals(immDates.getImmDateMarch())) {

      immAdjustedMaturityDate = immDates.getImmDateMarch();

      return immAdjustedMaturityDate;
    }

    if (maturityDate.equals(immDates.getImmDateJune())) {

      immAdjustedMaturityDate = immDates.getImmDateJune();

      return immAdjustedMaturityDate;
    }

    if (maturityDate.equals(immDates.getImmDateSeptember())) {

      immAdjustedMaturityDate = immDates.getImmDateSeptember();

      return immAdjustedMaturityDate;
    }

    if (maturityDate.equals(immDates.getImmDateDecember())) {

      immAdjustedMaturityDate = immDates.getImmDateDecember();

      return immAdjustedMaturityDate;
    }

    // -------------------------------------------------------------------

    // Determine where the maturity date is in relation to the IMM dates

    // Is the maturity date between 20/12 of the previous year and 20/3 of the current year
    if (maturityDate.isAfter(immDates.getImmDatePreviousDecember()) && maturityDate.isBefore(immDates.getImmDateMarch())) {

      immAdjustedMaturityDate = immDates.getImmDateMarch();

      return immAdjustedMaturityDate;
    }

    // Is the maturity date between 20/3 of the current year and 20/6 of the current year
    if (maturityDate.isAfter(immDates.getImmDateMarch()) && maturityDate.isBefore(immDates.getImmDateJune())) {

      immAdjustedMaturityDate = immDates.getImmDateJune();

      return immAdjustedMaturityDate;
    }

    // Is the maturity date between 20/6 of the current year and 20/9 of the current year
    if (maturityDate.isAfter(immDates.getImmDateJune()) && maturityDate.isBefore(immDates.getImmDateSeptember())) {

      immAdjustedMaturityDate = immDates.getImmDateSeptember();

      return immAdjustedMaturityDate;
    }

    // Is the maturity date between 20/9 of the current year and 20/12 of the current year
    if (maturityDate.isAfter(immDates.getImmDateSeptember()) && maturityDate.isBefore(immDates.getImmDateDecember())) {

      immAdjustedMaturityDate = immDates.getImmDateDecember();

      return immAdjustedMaturityDate;
    }

    // Is the maturity date between 20/12 of the current year and 20/3 of the next year
    if (maturityDate.isAfter(immDates.getImmDateDecember()) && maturityDate.isBefore(immDates.getImmDateNextMarch())) {

      immAdjustedMaturityDate = immDates.getImmDateNextMarch();

      return immAdjustedMaturityDate;
    }

    // -------------------------------------------------------------------

    return immAdjustedMaturityDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to take an input 'date' and adjust it to a business day (if necessary) according to the specified adjustment convention
  private ZonedDateTime businessDayAdjustDate(ZonedDateTime date, Calendar calendar, BusinessDayConvention businessdayAdjustmentConvention) {

    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "Business day adjustment");

    int deltaDays = 1;

    // Set the date to be adjusted to be the input date
    ZonedDateTime adjustedDate = date;

    // If using the 'following' convention, the adjusted date is after the input date
    if (businessdayAdjustmentConvention.equals(BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following"))) {
      deltaDays = 1;
    }

    // If using the 'preceeding' convention, the adjusted date is before the input date
    if (businessdayAdjustmentConvention.equals(BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding"))) {
      deltaDays = -1;
    }

    // Adjust the input date until it falls on a business day
    while (!calendar.isWorkingDay(adjustedDate.toLocalDate())) {
      adjustedDate = adjustedDate.plusDays(deltaDays);
    }

    return adjustedDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the number of premium leg cashflows given the adjusted effective and maturity dates and the coupon frequency
  private int calculateNumberOfPremiumLegCashflows(ZonedDateTime adjustedEffectiveDate, ZonedDateTime adjustedMaturityDate, PeriodFrequency couponFrequency, StubType stubType) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(adjustedEffectiveDate, "Adjusted effective date");
    ArgumentChecker.notNull(adjustedMaturityDate, "Adjusted maturity date");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(stubType, "Stub type");

    // -------------------------------------------------------------------------------

    int numberOfCashflows = 0;

    // -------------------------------------------------------------------------------

    // The stub is at the front of the premium leg schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {

      // Start at the adjusted maturity date of the contract
      ZonedDateTime cashflowDate = adjustedMaturityDate;

      // Step back in time in steps of size determined by the coupon frequency until we get to the first valid date after the adjusted effective date
      while (cashflowDate.isAfter(adjustedEffectiveDate)) {

        // Step back in time
        cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());

        // Increment the counter
        numberOfCashflows++;
      }

      // If the first coupon is long then remove one of the cashflows
      if (stubType == StubType.FRONTLONG) {
        numberOfCashflows--;
      }
    }

    // -------------------------------------------------------------------------------

    /*
    // The stub is at the end of the premium leg schedule
    if (stubType == StubType.BACKSHORT || stubType == StubType.BACKLONG) {

      // Start at the adjusted effective date of the contract
      ZonedDateTime cashflowDate = adjustedEffectiveDate;

      // Step forward in time in steps of size determined by the coupon frequency until we get to the last valid date before the adjusted maturity date
      while (cashflowDate.isBefore(adjustedMaturityDate)) {

        // Step forward in time
        cashflowDate = cashflowDate.plus(couponFrequency.getPeriod());

        // Increment the counter
        numberOfCashflows++;
      }
    }
    */

    // -------------------------------------------------------------------------------

    return numberOfCashflows;
  }

  // -------------------------------------------------------------------------------------------
}
