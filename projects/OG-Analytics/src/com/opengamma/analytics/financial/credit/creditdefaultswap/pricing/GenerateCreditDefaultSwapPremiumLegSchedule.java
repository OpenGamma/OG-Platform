/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.CouponFrequency;
import com.opengamma.analytics.financial.credit.ScheduleGenerationMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 *  Class containing methods to generate the premium leg cashflow schedule for a CDS (following the market conventions for CDS)
 */
public class GenerateCreditDefaultSwapPremiumLegSchedule {

  // -------------------------------------------------------------------------------------------

  // Method to generate the premium leg cashflow schedule from the (unadjusted) CDS contract specification
  public ZonedDateTime[] constructCreditDefaultSwapPremiumLegSchedule(CreditDefaultSwapDefinition cds) {

    // Extract the relevant CDS contract parameters necessary to build the premium schedule

    ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime effectiveDate = cds.getEffectiveDate();
    ZonedDateTime maturityDate = cds.getMaturityDate();

    Calendar calendar = cds.getCalendar();
    ScheduleGenerationMethod scheduleGenerationMethod = cds.getScheduleGenerationMethod();
    CouponFrequency couponFrequency = cds.getCouponFrequency();
    String daycountFractionConvention = cds.getDayCountFractionConvention();
    String businessdayAdjustmentConvention = cds.getBusinessDayAdjustmentConvention();
    boolean adjustMaturityDate = cds.getAdjustMaturityDate();

    // TODO : Add a check if the calendar is 'null' to signify no adjustment of business dates?

    // ------------------------------------------------

    // First, adjust the (user input) effective date for non-business days (currently assuming the user chooses the effective date for a legacy CDS)
    ZonedDateTime adjustedEffectiveDate = businessDayAdjustDate(effectiveDate, calendar, businessdayAdjustmentConvention);

    // ------------------------------------------------

    // Second, adjust the maturity date so that it falls on the next IMM date
    ZonedDateTime immAdjustedMaturityDate = immAdjustMaturityDate(maturityDate);

    // ------------------------------------------------

    // Third, construct the schedule of premium leg cashflows given the adjusted effective and maturity dates
    ZonedDateTime[] cashflowSchedule = calculateCashflowDates(adjustedEffectiveDate, immAdjustedMaturityDate, couponFrequency);

    // ------------------------------------------------

    // Fourth, business day adjust the generated schedule cashflow dates
    cashflowSchedule = businessDayAdjustcashflowSchedule(cashflowSchedule, calendar, businessdayAdjustmentConvention);

    // ------------------------------------------------

    // TODO : Change the dcf to the appropriate enum when we build it (and change the variable above)
    double[] daycountFraction = calculateDaycountFraction(adjustedEffectiveDate, cashflowSchedule, daycountFractionConvention);

    // ------------------------------------------------

    // Finally, adjust the IMM adjusted maturity date so that it falls on the following business day (if required)
    if (adjustMaturityDate) {
      // ZonedDateTime businessDayAdjustedMaturityDate = businessDayAdjustMaturityDate(immAdjustedMaturityDate, calendar, adjustMaturityDate);
    }

    // ------------------------------------------------

    System.out.println("Start Date (Unadjusted user input) = " + startDate);
    System.out.println("Effective Date (Unadjusted user input) = " + effectiveDate);
    System.out.println("Maturity Date (Unadjusted user input) = " + maturityDate);
    System.out.println("");
    System.out.println("Effective Date (bda) = " + adjustedEffectiveDate);
    System.out.println("Maturity Date (IMM adjusted) = " + immAdjustedMaturityDate);

    // ------------------------------------------------

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  double[] calculateDaycountFraction(ZonedDateTime adjustedEffectiveDate, ZonedDateTime[] cashflowSchedule, String daycountFractionConvention) {

    // TODO : Replace the string daycountFractionConvention with an enum

    int numberOfCashflows = cashflowSchedule.length;

    double[] dcf = new double[numberOfCashflows];

    // TODO : There must be a better way of calculating the difference between two dates (although this seems to give the right answer)!
    for (int i = 1; i < numberOfCashflows; i++) {
      dcf[i] = ((cashflowSchedule[i].toEpochSeconds() - cashflowSchedule[i - 1].toEpochSeconds()) / (24.0 * 60.0 * 60.0)) / 360.0;

      System.out.println("i = " + i + ", adj date = " + cashflowSchedule[i] + ", dcf = " + dcf[i]);
    }

    return dcf;
  }

  // -------------------------------------------------------------------------------------------

  // Method to business day adjust an input cashflow schedule
  ZonedDateTime[] businessDayAdjustcashflowSchedule(ZonedDateTime[] cashflowSchedule, Calendar calendar, String businessdayAdjustmentConvention) {

    int numberOfCashflows = cashflowSchedule.length;

    ZonedDateTime[] adjustedCashflowSchedule = new ZonedDateTime[numberOfCashflows];

    for (int i = 0; i < numberOfCashflows; i++) {
      adjustedCashflowSchedule[i] = businessDayAdjustDate(cashflowSchedule[i], calendar, businessdayAdjustmentConvention);

      System.out.println("i = " + i + ", adj date = " + adjustedCashflowSchedule[i]);
    }

    System.out.println("");

    return adjustedCashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to calculate the premium leg cashflow dates given the adjusted effective and maturity dates 
  ZonedDateTime[] calculateCashflowDates(ZonedDateTime adjustedEffectiveDate, ZonedDateTime immAdjustedMaturityDate, CouponFrequency couponFrequency) {

    // TODO : Add code to utilize schedule construction convention

    // The amount (in months) by which to jump between cashflows 
    int deltaMonths = 3;

    // Determine the jump amount according to the contract specifications
    switch (couponFrequency) {
      case MONTHLY:
        deltaMonths = 1;
        break;

      case QUARTERLY:
        deltaMonths = 3;
        break;

      case SEMIANNUAL:
        deltaMonths = 6;
        break;

      case ANNUAL:
        deltaMonths = 12;
        break;

      default:
        deltaMonths = 3;
        break;
    }

    // Compute the number of cashflows in the premium leg schedule (based on the adjusted dates)
    int numberOfCashflows = calculateNumberOfPremiumLegCashflows(adjustedEffectiveDate, immAdjustedMaturityDate, deltaMonths);

    // Build the cashflow schedule (include the node at the effective date even though there is no cashflow on this date)
    ZonedDateTime[] cashflowSchedule = new ZonedDateTime[numberOfCashflows + 1];

    // Start at the IMM adjusted maturity of the contract
    ZonedDateTime cashflowDate = immAdjustedMaturityDate;

    for (int i = numberOfCashflows; i > 0; i--) {

      System.out.println("Cashflow number " + i + " on unadj IMM date " + cashflowDate);

      // Store the date (note this is at the top of the loop)
      cashflowSchedule[i] = cashflowDate;

      // Step back in time by the specified number of months
      cashflowDate = cashflowDate.minusMonths(deltaMonths);
    }

    // Append the timenode at the adjusted effective date at the beginning of the cashflow schedule vector
    cashflowSchedule[0] = adjustedEffectiveDate;

    System.out.println("Cashflow number " + 0 + " on adj effective date " + cashflowSchedule[0]);

    System.out.println("");

    return cashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Method to determine if a date supplied is an IMM date
  boolean isAnIMMDate(ZonedDateTime date) {

    boolean returnValue = false;

    // Get the year of the date supplied
    final int year = date.getYear();

    // TODO : Extract these out into a seperate IMMDate class (this is clearly not a good way of doing this)
    final ZonedDateTime immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    final ZonedDateTime immDateJune = DateUtils.getUTCDate(year, 6, 20);
    final ZonedDateTime immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    final ZonedDateTime immDateDecember = DateUtils.getUTCDate(year, 12, 20);

    if (date.equals(immDateMarch) || date.equals(immDateJune) || date.equals(immDateSeptember) || date.equals(immDateDecember)) {
      returnValue = true;
    }

    return returnValue;
  }

  // -------------------------------------------------------------------------------------------

  // Method to adjust the specified maturity date to the next IMM date
  ZonedDateTime immAdjustMaturityDate(ZonedDateTime maturityDate) {

    // Start at the current maturityDate
    ZonedDateTime immAdjustedMaturityDate = maturityDate;

    // Get the year of the contract maturity
    final int year = maturityDate.getYear();

    // TODO : Extract these out into a seperate IMMDate class
    final ZonedDateTime immDatePreviousDecember = DateUtils.getUTCDate(year - 1, 12, 20);
    final ZonedDateTime immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    final ZonedDateTime immDateJune = DateUtils.getUTCDate(year, 6, 20);
    final ZonedDateTime immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    final ZonedDateTime immDateDecember = DateUtils.getUTCDate(year, 12, 20);
    final ZonedDateTime immDateNextMarch = DateUtils.getUTCDate(year + 1, 3, 20);

    // -------------------------------------------------------------------

    // First of all check that the maturity date isn't one of the IMM dates for 'year'

    // Is the maturity date equal to the March IMM date of 'year'
    if (maturityDate.equals(immDateMarch)) {
      immAdjustedMaturityDate = immDateMarch;
    }

    // Is the maturity date equal to the June IMM date of 'year'
    if (maturityDate.equals(immDateJune)) {
      immAdjustedMaturityDate = immDateJune;
    }

    // Is the maturity date equal to the Seotember IMM date of 'year'
    if (maturityDate.equals(immDateSeptember)) {
      immAdjustedMaturityDate = immDateSeptember;
    }

    // Is the maturity date equal to the December IMM date of 'year'
    if (maturityDate.equals(immDateDecember)) {
      immAdjustedMaturityDate = immDateDecember;
    }

    // -------------------------------------------------------------------

    // Determine where the maturity date is in relation to the IMM dates

    // Is the maturity date between 20/12 of the previous year and 20/3 of the current year
    if (maturityDate.isAfter(immDatePreviousDecember) && maturityDate.isBefore(immDateMarch)) {
      immAdjustedMaturityDate = immDateMarch;
    }

    // Is the maturity date between 20/3 of the current year and 20/6 of the current year
    if (maturityDate.isAfter(immDateMarch) && maturityDate.isBefore(immDateJune)) {
      immAdjustedMaturityDate = immDateJune;
    }

    // Is the maturity date between 20/6 of the current year and 20/9 of the current year
    if (maturityDate.isAfter(immDateJune) && maturityDate.isBefore(immDateSeptember)) {
      immAdjustedMaturityDate = immDateSeptember;
    }

    // Is the maturity date between 20/9 of the current year and 20/12 of the current year
    if (maturityDate.isAfter(immDateSeptember) && maturityDate.isBefore(immDateDecember)) {
      immAdjustedMaturityDate = immDateDecember;
    }

    // Is the maturity date between 20/12 of the current year and 20/3 of the next year
    if (maturityDate.isAfter(immDateDecember) && maturityDate.isBefore(immDateNextMarch)) {
      immAdjustedMaturityDate = immDateNextMarch;
    }

    // -------------------------------------------------------------------

    return immAdjustedMaturityDate;
  }

  // -------------------------------------------------------------------------------------------

  ZonedDateTime businessDayAdjustDate(ZonedDateTime date, Calendar calendar, String businessdayAdjustmentConvention) {

    ZonedDateTime adjustedDate = date;

    // TODO : Include code for other business day adjustment conventions

    // Will change this to take into account the contract specifications
    int delta = 1;

    while (!calendar.isWorkingDay(adjustedDate.toLocalDate())) {
      adjustedDate = adjustedDate.plusDays(delta);
    }

    return adjustedDate;
  }

  // -------------------------------------------------------------------------------------------

  // Function to calculate the number of premium leg cashflows given the adjusted effective and maturity dates
  int calculateNumberOfPremiumLegCashflows(ZonedDateTime adjustedEffectiveDate, ZonedDateTime immAdjustedMaturityDate, int deltaMonths) {

    // TODO : Add code to step by other numbers of months

    int numberOfCashflows = 0;

    // Start at the IMM adjusted maturity of the contract
    ZonedDateTime cashflowDate = immAdjustedMaturityDate;

    // Step back in time in steps of size deltaMonths until we get to the first valid date after the adjusted effective date
    while (cashflowDate.isAfter(adjustedEffectiveDate)) {

      // Step back in time
      cashflowDate = cashflowDate.minusMonths(deltaMonths);

      // Increment the counter
      numberOfCashflows++;
    }

    return numberOfCashflows;
  }

  // -------------------------------------------------------------------------------------------
}
