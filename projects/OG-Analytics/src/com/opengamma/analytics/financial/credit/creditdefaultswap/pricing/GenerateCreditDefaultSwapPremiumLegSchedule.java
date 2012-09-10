/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.IMMDates;
import com.opengamma.analytics.financial.credit.ScheduleGenerationMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
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

  // -------------------------------------------------------------------------------------------

  // TODO : Add a check if the calendar is 'null' to signify no adjustment of business dates? Overloaded method
  // TODO : This is all coded for the case of building the schedule backwards from the maturity date - need to generalise  
  // TODO : In businessDayAdjustDate add code to determine if we move forward or backward to find the next business day
  // TODO : Add the calculations for the accrual begin/end periods
  // TODO : Eventually replace the n x 3 array returned

  // -------------------------------------------------------------------------------------------

  // Public method to generate the premium leg cashflow schedule from the (unadjusted) CDS contract specification
  public ZonedDateTime[][] constructCreditDefaultSwapPremiumLegSchedule(CreditDefaultSwapDefinition cds) {

    // Check input CDS object is not null
    ArgumentChecker.notNull(cds, "CDS field");

    // ------------------------------------------------

    // The return array is organised as below

    // ZonedDateTime[][0] The premium leg cashflow dates
    // ZonedDateTime[][1] The accrual period begin dates
    // ZonedDateTime[][2] The accrual period end dates

    // ------------------------------------------------

    // Extract the relevant CDS contract parameters necessary to build the premium schedule

    ZonedDateTime effectiveDate = cds.getEffectiveDate();
    ZonedDateTime maturityDate = cds.getMaturityDate();

    Calendar calendar = cds.getCalendar();
    ScheduleGenerationMethod scheduleGenerationMethod = cds.getScheduleGenerationMethod();
    PeriodFrequency couponFrequency = cds.getCouponFrequency();

    DayCount daycountFractionConvention = cds.getDayCountFractionConvention();
    BusinessDayConvention businessdayAdjustmentConvention = cds.getBusinessDayAdjustmentConvention();

    boolean adjustMaturityDate = cds.getAdjustMaturityDate();

    // ------------------------------------------------

    // First, adjust the (user input) effective date for non-business days (currently assuming the user chooses the effective date for a legacy CDS)
    ZonedDateTime adjustedEffectiveDate = businessDayAdjustDate(effectiveDate, calendar, businessdayAdjustmentConvention);

    // ------------------------------------------------

    // Second, adjust the maturity date so that it falls on the next IMM date
    ZonedDateTime immAdjustedMaturityDate = immAdjustMaturityDate(maturityDate);

    // ------------------------------------------------

    // Third, construct the schedule of premium leg cashflows given the adjusted effective and maturity dates
    ZonedDateTime[][] cashflowSchedule = calculateCashflowDates(adjustedEffectiveDate, immAdjustedMaturityDate, couponFrequency);

    // ------------------------------------------------

    // Fourth, business day adjust the generated schedule cashflow dates
    cashflowSchedule = businessDayAdjustcashflowSchedule(cashflowSchedule, calendar, businessdayAdjustmentConvention);

    // ------------------------------------------------

    // Calculate the fraction of a year between adjacent cashflows (might do this on the fly in the valuation routine)
    double[] daycountFraction = calculateDaycountFraction(adjustedEffectiveDate, cashflowSchedule, daycountFractionConvention);

    // ------------------------------------------------

    // Finally, adjust the IMM adjusted maturity date so that it falls on the following business day (if required)
    if (adjustMaturityDate) {
      // ZonedDateTime businessDayAdjustedMaturityDate = businessDayAdjustMaturityDate(immAdjustedMaturityDate, calendar, adjustMaturityDate);
    }

    // ------------------------------------------------

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Keep all these as private as they are internal methods of the cashflow schedule generation class

  // Function to return a vector of daycount fractions given an input cashflow schedule
  private double[] calculateDaycountFraction(ZonedDateTime adjustedEffectiveDate, ZonedDateTime[][] cashflowSchedule, DayCount daycountFractionConvention) {

    int numberOfCashflows = cashflowSchedule.length;

    double[] dcf = new double[numberOfCashflows];

    for (int i = 1; i < numberOfCashflows; i++) {
      dcf[i] = ACT_360.getDayCountFraction(cashflowSchedule[i - 1][0], cashflowSchedule[i][0]);
    }

    return dcf;
  }

  // -------------------------------------------------------------------------------------------

  // Method to business day adjust an input cashflow schedule
  private ZonedDateTime[][] businessDayAdjustcashflowSchedule(ZonedDateTime[][] cashflowSchedule, Calendar calendar, BusinessDayConvention businessdayAdjustmentConvention) {

    int numberOfCashflows = cashflowSchedule.length;

    ZonedDateTime[][] adjustedCashflowSchedule = new ZonedDateTime[numberOfCashflows][3];

    for (int i = 0; i < numberOfCashflows; i++) {
      adjustedCashflowSchedule[i][0] = businessDayAdjustDate(cashflowSchedule[i][0], calendar, businessdayAdjustmentConvention);
    }

    return adjustedCashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the premium leg cashflow dates given the adjusted effective and maturity dates 
  private ZonedDateTime[][] calculateCashflowDates(ZonedDateTime adjustedEffectiveDate, ZonedDateTime immAdjustedMaturityDate, PeriodFrequency couponFrequency) {

    // Compute the number of cashflows in the premium leg schedule (based on the adjusted dates and the coupon frequency)
    int numberOfCashflows = calculateNumberOfPremiumLegCashflows(adjustedEffectiveDate, immAdjustedMaturityDate, couponFrequency);

    // Build the cashflow schedule (include the node at the effective date even though there is no cashflow on this date)
    ZonedDateTime[][] cashflowSchedule = new ZonedDateTime[numberOfCashflows + 1][3];

    // Start at the IMM adjusted maturity of the contract
    ZonedDateTime cashflowDate = immAdjustedMaturityDate;

    // Note the order of the loop
    for (int i = numberOfCashflows; i > 0; i--) {

      // Store the date (note this is at the top of the loop)
      cashflowSchedule[i][0] = cashflowDate;

      // Step back in time by the specified number of months
      cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());
    }

    // Append the timenode at the adjusted effective date at the beginning of the cashflow schedule vector
    cashflowSchedule[0][0] = adjustedEffectiveDate;

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to determine if a date supplied is an IMM date
  private boolean isAnIMMDate(ZonedDateTime date) {

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
  private ZonedDateTime immAdjustMaturityDate(ZonedDateTime maturityDate) {

    // Start at the current maturityDate
    ZonedDateTime immAdjustedMaturityDate = maturityDate;

    // Get the year of the contract maturity
    final int year = maturityDate.getYear();

    // Construct an object with the IMM dates for 'year'
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

    ZonedDateTime adjustedDate = date;

    //if (businessdayAdjustmentConvention == BusinessDayConvention)

    int deltaDays = 1;

    while (!calendar.isWorkingDay(adjustedDate.toLocalDate())) {
      adjustedDate = adjustedDate.plusDays(deltaDays);
    }

    return adjustedDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the number of premium leg cashflows given the adjusted effective and maturity dates and the coupon frequency
  private int calculateNumberOfPremiumLegCashflows(ZonedDateTime adjustedEffectiveDate, ZonedDateTime immAdjustedMaturityDate, PeriodFrequency couponFrequency) {

    int numberOfCashflows = 0;

    // Start at the IMM adjusted maturity of the contract
    ZonedDateTime cashflowDate = immAdjustedMaturityDate;

    // Step back in time in steps of size determined by the coupon frequency until we get to the first valid date after the adjusted effective date
    while (cashflowDate.isAfter(adjustedEffectiveDate)) {

      // Step back in time
      cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());

      // Increment the counter
      numberOfCashflows++;
    }

    return numberOfCashflows;
  }

  // -------------------------------------------------------------------------------------------
}
