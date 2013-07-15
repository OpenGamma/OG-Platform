/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
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

  // -------------------------------------------------------------------------------------------

  private static final DayCount ACT_360 = DayCountFactory.INSTANCE.getDayCount("Act/360");
  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // -------------------------------------------------------------------------------------------

  // TODO : Add a check if the calendar is 'null' to signify no adjustment of business dates? Overloaded method
  // TODO : Fix the code for the back stubs (is there a better way of handling this than just duplicating code?) - front stubs seem to work okay
  // TODO : Add the code for the first coupon's
  // TODO : Should convertDatesToDoubles be put somewhere else? To use it, need to create a GenerateCreditDefaultSwapPremiumLegSchedule object which is a bit wasteful
  // TODO : Look at TemporalAdjuster class for IMM date handling
  // TODO : FrontLong stubs - e.g. startDate = 20/12/2007, effDate = 21/12/2007; first coupon at 20/6/2008. ISDA model first coupon at 20/3/2008 (seems to use start date not eff date)
  // TODO : Remove one of the overloaded convertdatesToDoubles methods
  // TODO : Rewrite and integrate constructISDACompliantCashflowSchedule into the code better

  // TODO : Need to hook in the flags for adjusting various dates again

  // TODO : Add WORKDAY equivalent function

  // -------------------------------------------------------------------------------------------

  /**
   * Calculate Business Day Adjusted (BDA) cash flow dates on a CDS premium leg 
   * @param cds The CDS
   * @return array-of-arrays of ZonedDateTime. This 'matrix' has 4 columns: The first is the BDA cash flow times (the first and last entries are the start and end dates - which are 
   * Unadjusted); the second is the accrual start date (which is usually the previous cash flow time), <b>note</b> the first of these is null; 
   * the third is the accrual end date (which is usually the same as the cash flow time), <b>note</b> the first of these is null;
   *  the fourth is the accrual pay date (which is usually the same as the cash flow time), <b>note</b> the first of these is null; 
   *  @deprecated is will be replaced once it is fully tested 
   */
  @Deprecated 
  public ZonedDateTime[][] constructISDACompliantCreditDefaultSwapPremiumLegSchedule(final CreditDefaultSwapDefinition cds) {

    ArgumentChecker.notNull(cds, "CDS");    
   
    int totalDates = 0;
    // NOTE : We have hacked this to have a maximum number of possible cashflows of 1000 - should sort this out
    //TODO This should be replaced by a container 
    final ZonedDateTime[] tempCashflowSchedule = new ZonedDateTime[1000];

    ZonedDateTime date;
    final ZonedDateTime startDate = cds.getStartDate();
    final ZonedDateTime endDate = cds.getMaturityDate();

    final boolean protectStart = cds.getProtectionStart();

    final StubType stubType = cds.getStubType();

    // ------------------------------------------------

    // TODO : Check this logic
    /*
    if (protectStart) {
      ArgumentChecker.isTrue(startDate.isBefore(endDate), null);
    } else {
      ArgumentChecker.isTrue(endDate.isAfter(startDate), null);
    }
     */

    if (protectStart && endDate.equals(startDate)) {
      // TODO : Add code for when there are only two dates and break out of routine
    }

    // ------------------------------------------------

    // ---------------

    // Is the stub at the front end of the payment schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {

      int i = 0;

      date = endDate;

      //These are the unadjusted dates rolling back from the end date
      while (date.isAfter(startDate)) {
        tempCashflowSchedule[i] = date;
        i++;
        totalDates++;
        date = date.minus(cds.getCouponFrequency().getPeriod());
      }

      // TODO : Check totalDates > 0
      // TODO : Check that date <= startDate

      // TODO : Check the FRONTSHORT/FRONTLONG logic here
      if (date.isEqual(startDate) || totalDates == 1 || stubType == StubType.FRONTSHORT) {
        totalDates++;
        tempCashflowSchedule[i] = startDate;
      } else {
        tempCashflowSchedule[i - 1] = startDate;
      }

    }

    // ---------------

    // TODO : Add the code for the back stub
    // Is the stub at the back end of the payment schedule
    if (stubType == StubType.BACKSHORT || stubType == StubType.BACKLONG) {

    }

    // ---------------

    // TODO : Need to check that totalDates >= 2
    // TODO : Need to check that number of schedule dates == totalDates

    // ---------------

    final ZonedDateTime[] cashflowSchedule = new ZonedDateTime[totalDates];

    //write the unadjusted dates in acceding order 
    for (int i = 0; i < totalDates; i++) {
      cashflowSchedule[i] = tempCashflowSchedule[totalDates - 1 - i];
    }

    // TODO : Need to make bdaCashflowSchedule a matrix returning acc start/end dates as well as pay dates. This is because
    // TODO : if the maturity of the CDS falls on a non-business day, the pay date is bda, but the final accrual date is not bda

    final ZonedDateTime[][] tempBDACashflowSchedule = new ZonedDateTime[cashflowSchedule.length][4];

    // Fill up the first column
    //R White - Comment: first and last dates are not adjusted 
    tempBDACashflowSchedule[0][0] = cashflowSchedule[0];
    tempBDACashflowSchedule[tempBDACashflowSchedule.length - 1][0] = cashflowSchedule[cashflowSchedule.length - 1];
    for (int i = 1; i < tempBDACashflowSchedule.length - 1; i++) {
      tempBDACashflowSchedule[i][0] = businessDayAdjustDate(cashflowSchedule[i], cds.getCalendar(), cds.getBusinessDayAdjustmentConvention());
    }
   
    // Now fill up the acc start/end and pay dates

    // This is based on the code in the ISDA model

    ZonedDateTime prevDate = cashflowSchedule[0];
    ZonedDateTime prevDateAdj = prevDate;

    // Note the start index
    for (int i = 1; i < cashflowSchedule.length; i++) {

      ZonedDateTime nextDate = cashflowSchedule[i];
      //R White Review: this is exactly the same adjustment as made above 
      ZonedDateTime nextDateAdj = businessDayAdjustDate(cashflowSchedule[i], cds.getCalendar(), cds.getBusinessDayAdjustmentConvention());

      // accStartDate
      tempBDACashflowSchedule[i][1] = prevDateAdj;

      // accEndDate
      tempBDACashflowSchedule[i][2] = nextDateAdj;

      // payDate
      tempBDACashflowSchedule[i][3] = nextDateAdj;

      prevDate = nextDate; //this assignment is never used in the loop 
      prevDateAdj = nextDateAdj;
    }

    if (protectStart) {
      tempBDACashflowSchedule[cashflowSchedule.length - 1][2] = prevDate.plusDays(1);
    } else {
      tempBDACashflowSchedule[cashflowSchedule.length - 1][2] = prevDate;
    }

    // ------------------------------------------------

    //final ZonedDateTime[] bdaCashflowSchedule = new ZonedDateTime[cashflowSchedule.length];

    // The first accrual date is not bda
    //bdaCashflowSchedule[0] = cashflowSchedule[0];

    /*
    for (int i = 1; i < bdaCashflowSchedule.length - 1; i++) {
      bdaCashflowSchedule[i] = businessDayAdjustDate(cashflowSchedule[i], cds.getCalendar(), cds.getBusinessDayAdjustmentConvention());
    }
    */

    // Careful of this - we are not modifying the maturity date of the CDS
    /*
    if (protectStart) {
      bdaCashflowSchedule[bdaCashflowSchedule.length - 1] = cashflowSchedule[cashflowSchedule.length - 1].plusDays(1);
    }
     */

    // The final accrual date is not bda
    // Remember if protectStart = TRUE then there is an extra day of accrued that is not captured here
    //bdaCashflowSchedule[bdaCashflowSchedule.length - 1] = cashflowSchedule[cashflowSchedule.length - 1];

    // ------------------------------------------------

    //return bdaCashflowSchedule;

    return tempBDACashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // Public method to generate the premium leg cashflow schedule from the input CDS contract specification
  public ZonedDateTime[] constructCreditDefaultSwapPremiumLegSchedule(final CreditDefaultSwapDefinition cds) {

    // ------------------------------------------------

    // Check input CDS object is not null
    ArgumentChecker.notNull(cds, "CDS");

    // ------------------------------------------------

    // First, adjust the (user input) effective date for non-business days (currently assuming the user chooses the effective date to be anything they want)
    final ZonedDateTime adjustedEffectiveDate = getAdjustedEffectiveDate(cds);

    // ------------------------------------------------

    // Second, adjust the maturity date so that it falls on the next IMM date (if the user specifies this to be so)
    final ZonedDateTime adjustedMaturityDate = getAdjustedMaturityDate(cds);

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

  // Public method to extract just the adjusted effective date for a CDS according to the contract specifications (don't have to go through the whole schedule gen process)
  public ZonedDateTime getAdjustedEffectiveDate(final CreditDefaultSwapDefinition cds) {

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
  public ZonedDateTime getAdjustedMaturityDate(final CreditDefaultSwapDefinition cds) {

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
  public double[] convertTenorsToDoubles(final ZonedDateTime valuationDate, final CreditDefaultSwapDefinition cds, final ZonedDateTime[] tenors) {

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(tenors, "tenors");

    final int numberOfTenors = tenors.length;

    final double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {
      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(valuationDate, tenors[i], cds.getDayCountFractionConvention());
    }

    return tenorsAsDoubles;
  }

  // -------------------------------------------------------------------------------------------

  // Public method to convert the input ZonedDateTime tenors into doubles relative to the specified date based on the daycount convention specified
  public double[] convertTenorsToDoubles(final ZonedDateTime[] tenors, final ZonedDateTime baselineDate, final DayCount dayCountConvention) {

    ArgumentChecker.notNull(dayCountConvention, "Daycount convention");
    ArgumentChecker.notNull(baselineDate, "Baseline date");
    ArgumentChecker.notNull(tenors, "tenors");

    final int numberOfTenors = tenors.length;

    final double[] tenorsAsDoubles = new double[numberOfTenors];

    for (int i = 0; i < numberOfTenors; i++) {
      tenorsAsDoubles[i] = TimeCalculator.getTimeBetween(baselineDate, tenors[i], dayCountConvention);
    }

    return tenorsAsDoubles;
  }

  // -------------------------------------------------------------------------------------------

  // Function to return a vector of daycount fractions given an input cashflow schedule
  private double[] calculateDaycountFraction(final ZonedDateTime adjustedEffectiveDate, final ZonedDateTime[][] cashflowSchedule, final DayCount daycountFractionConvention) {

    ArgumentChecker.notNull(adjustedEffectiveDate, "Adjusted effective date");
    ArgumentChecker.notNull(cashflowSchedule, "Cashflow schedule");
    ArgumentChecker.notNull(daycountFractionConvention, "Daycount convention");

    final int numberOfCashflows = cashflowSchedule.length;

    final double[] dcf = new double[numberOfCashflows];

    for (int i = 1; i < numberOfCashflows; i++) {
      dcf[i] = ACT_360.getDayCountFraction(cashflowSchedule[i - 1][0], cashflowSchedule[i][0]);
    }

    return dcf;
  }

  // -------------------------------------------------------------------------------------------

  // Method to business day adjust an input cashflow schedule according to a specified convention
  private ZonedDateTime[] businessDayAdjustcashflowSchedule(final CreditDefaultSwapDefinition cds, final ZonedDateTime[] cashflowSchedule) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(cashflowSchedule, "Cashflow schedule");

    // -------------------------------------------------------------------------------

    // Get the business day calendar
    final Calendar calendar = cds.getCalendar();

    // Get the convention for adjusting non-business days
    final BusinessDayConvention businessdayAdjustmentConvention = cds.getBusinessDayAdjustmentConvention();

    // Do we business day adjust the final cashflow
    final boolean adjustedMaturityDateConvention = cds.getAdjustMaturityDate();

    final int numberOfCashflows = cashflowSchedule.length;

    final ZonedDateTime[] adjustedCashflowSchedule = new ZonedDateTime[numberOfCashflows];

    // -------------------------------------------------------------------------------

    // TODO : Reverse the order of the dates in the cashflow schedule

    // Business day adjust all of the cashflow dates except the first one (which is not bda) and the final one (which handled seperately)
    //for (int i = 1; i < numberOfCashflows - 2; i++)
    for (int i = 0; i < numberOfCashflows - 1; i++) {
      adjustedCashflowSchedule[i] = businessDayAdjustDate(cashflowSchedule[i], calendar, businessdayAdjustmentConvention);
    }

    /*
    if (cds.getProtectionStart()) {
      adjustedCashflowSchedule[0] = cashflowSchedule[0].plusDays(1);
    } else {
      adjustedCashflowSchedule[0] = cashflowSchedule[0];
    }
     */

    // Determine if we need to business day adjust the final cashflow or not
    if (adjustedMaturityDateConvention) {
      adjustedCashflowSchedule[numberOfCashflows - 1] = businessDayAdjustDate(cashflowSchedule[numberOfCashflows - 1], calendar, businessdayAdjustmentConvention);
    } else {
      adjustedCashflowSchedule[numberOfCashflows - 1] = cashflowSchedule[numberOfCashflows - 1];
    }

    // -------------------------------------------------------------------------------

    return adjustedCashflowSchedule;
  }

  // -------------------------------------------------------------------------------------------

  // TODO : Need to sort out the front stubs

  // Method to calculate the premium leg cashflow dates given the adjusted effective and maturity dates
  private ZonedDateTime[] calculateCashflowDates(final CreditDefaultSwapDefinition cds, final ZonedDateTime adjustedEffectiveDate, final ZonedDateTime adjustedMaturityDate) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(cds, "CDS");
    ArgumentChecker.notNull(adjustedEffectiveDate, "Adjusted effective date");
    ArgumentChecker.notNull(adjustedMaturityDate, "Adjusted maturity date");

    // TODO : Check that mat date is after start date

    // -------------------------------------------------------------------------------

    final ZonedDateTime startDate = cds.getStartDate();

    // Get the coupon stub type
    final StubType stubType = cds.getStubType();

    // Get the coupon frequency
    final PeriodFrequency couponFrequency = cds.getCouponFrequency();

    // Compute the number of cashflows in the premium leg schedule (based on the adjusted effective and maturity dates and the coupon frequency and stub type)
    final int numberOfCashflows = calculateNumberOfPremiumLegCashflows(adjustedEffectiveDate, adjustedMaturityDate, couponFrequency, stubType);

    //final int numberOfCashflows = calculateNumberOfPremiumLegCashflows(startDate, adjustedMaturityDate, couponFrequency, stubType);

    // TODO : Need to check if there are less than two cashflow dates

    // Build the cashflow schedule (include the node at the effective date even though there is no cashflow on this date)
    final ZonedDateTime[] cashflowSchedule = new ZonedDateTime[numberOfCashflows + 1];

    // -------------------------------------------------------------------------------

    // The stub is at the front of the premium leg schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {

      // Start at the adjusted maturity of the contract
      ZonedDateTime cashflowDate = adjustedMaturityDate;

      /*
      int i = 0;

      cashflowSchedule[i] = cashflowDate;

      while (cashflowDate.isAfter(startDate)) {
        i++;
        cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());
        cashflowSchedule[i] = cashflowDate;
      }
       */

      // Note the order of the loop and the termination condition (i > 0 not i = 0)
      for (int i = numberOfCashflows; i > 0; i--) {

        // Store the date (note this is at the top of the loop)
        cashflowSchedule[i] = cashflowDate;

        // Step back in time by the specified number of months
        cashflowDate = cashflowDate.minus(couponFrequency.getPeriod());
      }

      // TODO : Sort this out

      if (stubType == StubType.FRONTSHORT) {
        // Append the timenode at the adjusted effective date at the beginning of the cashflow schedule vector
        cashflowSchedule[0] = adjustedEffectiveDate;
      }

      if (stubType == StubType.FRONTLONG) {
        cashflowSchedule[0] = cashflowDate.minus(couponFrequency.getPeriod());
      }

    }

    // -------------------------------------------------------------------------------

    // TODO : Test this code
    // TODO : Add the appendage at the end

    // The stub is at the back of the premium leg schedule
    if (stubType == StubType.BACKSHORT || stubType == StubType.BACKLONG) {

      // Start at the adjusted effective date of the contract
      ZonedDateTime cashflowDate = adjustedEffectiveDate;

      /*
      int i = 0;

      cashflowSchedule[i] = cashflowDate;

      while (cashflowDate.isBefore(adjustedMaturityDate)) {
        i++;
        cashflowDate = cashflowDate.plus(couponFrequency.getPeriod());
        cashflowSchedule[i] = cashflowDate;
      }
       */

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
  public boolean isAnIMMDate(final ZonedDateTime date) {

    ArgumentChecker.notNull(date, "date");

    boolean returnValue = false;

    // Get the year of the date supplied
    final int year = date.getYear();

    // Construct an object with the IMM dates for 'year'
    final IMMDates immDates = new IMMDates(year);

    // Test if 'date' is equal to one of the IMM dates
    if (date.equals(immDates.getImmDateMarch()) || date.equals(immDates.getImmDateJune()) || date.equals(immDates.getImmDateSeptember()) || date.equals(immDates.getImmDateDecember())) {
      returnValue = true;
    }

    return returnValue;
  }

  // -------------------------------------------------------------------------------------------

  // Method to adjust the specified maturity date to the next IMM date
  public ZonedDateTime immAdjustDate(final ZonedDateTime date) {

    // Check that the input date is not null
    ArgumentChecker.notNull(date, "Maturity date");

    // Start at the current maturityDate
    ZonedDateTime immAdjustedDate = date;

    // Get the year of the contract maturity
    final int year = date.getYear();

    // Construct an IMM object with the IMM dates for 'year'
    final IMMDates immDates = new IMMDates(year);

    // -------------------------------------------------------------------

    // First of all check that the maturity date isn't one of the IMM dates for 'year'

    if (date.equals(immDates.getImmDateMarch())) {

      immAdjustedDate = immDates.getImmDateMarch();

      return immAdjustedDate;
    }

    if (date.equals(immDates.getImmDateJune())) {

      immAdjustedDate = immDates.getImmDateJune();

      return immAdjustedDate;
    }

    if (date.equals(immDates.getImmDateSeptember())) {

      immAdjustedDate = immDates.getImmDateSeptember();

      return immAdjustedDate;
    }

    if (date.equals(immDates.getImmDateDecember())) {

      immAdjustedDate = immDates.getImmDateDecember();

      return immAdjustedDate;
    }

    // -------------------------------------------------------------------

    // Determine where the maturity date is in relation to the IMM dates

    // Is the maturity date between 20/12 of the previous year and 20/3 of the current year
    if (date.isAfter(immDates.getImmDatePreviousDecember()) && date.isBefore(immDates.getImmDateMarch())) {

      immAdjustedDate = immDates.getImmDateMarch();

      return immAdjustedDate;
    }

    // Is the maturity date between 20/3 of the current year and 20/6 of the current year
    if (date.isAfter(immDates.getImmDateMarch()) && date.isBefore(immDates.getImmDateJune())) {

      immAdjustedDate = immDates.getImmDateJune();

      return immAdjustedDate;
    }

    // Is the maturity date between 20/6 of the current year and 20/9 of the current year
    if (date.isAfter(immDates.getImmDateJune()) && date.isBefore(immDates.getImmDateSeptember())) {

      immAdjustedDate = immDates.getImmDateSeptember();

      return immAdjustedDate;
    }

    // Is the maturity date between 20/9 of the current year and 20/12 of the current year
    if (date.isAfter(immDates.getImmDateSeptember()) && date.isBefore(immDates.getImmDateDecember())) {

      immAdjustedDate = immDates.getImmDateDecember();

      return immAdjustedDate;
    }

    // Is the maturity date between 20/12 of the current year and 20/3 of the next year
    if (date.isAfter(immDates.getImmDateDecember()) && date.isBefore(immDates.getImmDateNextMarch())) {

      immAdjustedDate = immDates.getImmDateNextMarch();

      return immAdjustedDate;
    }

    // -------------------------------------------------------------------

    return immAdjustedDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to take an input 'date' and adjust it to a business day (if necessary) according to the specified adjustment convention
  public ZonedDateTime businessDayAdjustDate(final ZonedDateTime date, final Calendar calendar, final BusinessDayConvention businessdayAdjustmentConvention) {

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
  private int calculateNumberOfPremiumLegCashflows(
      final ZonedDateTime startDate,
      final ZonedDateTime endDate,
      final PeriodFrequency couponFrequency,
      final StubType stubType) {

    // -------------------------------------------------------------------------------

    ArgumentChecker.notNull(startDate, "Start date");
    ArgumentChecker.notNull(endDate, "End date");
    ArgumentChecker.notNull(couponFrequency, "Coupon frequency");
    ArgumentChecker.notNull(stubType, "Stub type");

    // -------------------------------------------------------------------------------

    int numberOfCashflows = 0;

    // -------------------------------------------------------------------------------

    // The stub is at the front of the premium leg schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {

      // Start at the adjusted maturity date of the contract
      ZonedDateTime cashflowDate = endDate;

      // Step back in time in steps of size determined by the coupon frequency until we get to the first valid date after the adjusted effective date
      while (cashflowDate.isAfter(startDate)) {

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

    // The stub is at the end of the premium leg schedule
    if (stubType == StubType.BACKSHORT || stubType == StubType.BACKLONG) {

      // Start at the adjusted effective date of the contract
      ZonedDateTime cashflowDate = startDate;

      // Step forward in time in steps of size determined by the coupon frequency until we get to the last valid date before the adjusted maturity date
      while (cashflowDate.isBefore(endDate)) {

        // Step forward in time
        cashflowDate = cashflowDate.plus(couponFrequency.getPeriod());

        // Increment the counter
        numberOfCashflows++;
      }
    }

    // -------------------------------------------------------------------------------

    return numberOfCashflows;
  }

  // -------------------------------------------------------------------------------------------
}
