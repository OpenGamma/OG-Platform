/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.time.DateUtils;

/**
 *  Class containing methods to generate the premium leg cashflow schedule for a CDS (following the market conventions for CDS)
 */
public class GenerateCreditDefaultSwapPremiumLegSchedule {

  // -------------------------------------------------------------------------------------------

  // Method to generate the premium leg cashflow schedule
  public void getCreditDefaultSwapPremiumLegSchedule(CreditDefaultSwapDefinition cds) {
    
    System.out.println("Working ...");

    /*
    ZonedDateTime startDate = cds.getStartDate();
    ZonedDateTime maturityDate = cds.getMaturityDate();

    //boolean adjustMaturityDate = cds.getAdjustMaturityDate();

    Calendar calendar = cds.getCalendar();

    if (calendar.equals(null)) {
      // No calender, therefore don't adjust any dates for non-business days
    }

    //ZonedDateTime adjustedEffectiveDate = adjustEffectiveDate(startDate, calendar);
    //ZonedDateTime immAdjustedMaturityDate = immAdjustMaturityDate(maturityDate, calendar);



    int numberOfCashflows = 1;

    while (maturityDate.isAfter(effectiveDate)) {
      maturityDate = maturityDate.minusMonths(3);
      numberOfCashflows++;

      System.out.println("Cashflow on " + maturityDate + ", " + numberOfCashflows + " cashflows");
    }

    //int year = DateUtils.getDaysBetween(effectiveDate, maturityDate);
    //System.out.println(year);

    System.out.println("Adjusted Maturity Date = " + maturityDate);


    System.out.println("Start Date = " + startDate);
    System.out.println("Adjusted Effective Date = " + adjustedEffectiveDate);
    System.out.println("Unadjusted Maturity Date = " + maturityDate);
    System.out.println("IMM Adjusted Maturity Date = " + immAdjustedMaturityDate);
     */   
  }

  // -------------------------------------------------------------------------------------------

  // Method to set the effective date to T + 1 and adjust it to a following business day if necessary
  ZonedDateTime adjustEffectiveDate(ZonedDateTime startDate, Calendar calendar) {

    // Set the effective date to be T + 1
    ZonedDateTime effectiveDate = startDate.plusDays(1);

    // Adjust the effective date so it does not fall on a non-business day
    while (!calendar.isWorkingDay(effectiveDate.toLocalDate())) {
      effectiveDate = effectiveDate.plusDays(1);
    }

    return effectiveDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to adjust the specified maturity date to the next IMM date
  ZonedDateTime immAdjustMaturityDate(ZonedDateTime maturityDate, Calendar calendar) {

    ZonedDateTime immAdjustedMaturityDate = maturityDate;

    //final int day = maturityDate.getDayOfMonth();
    //final int month = maturityDate.getMonthOfYear().getValue();
    final int year = maturityDate.getYear();

    final ZonedDateTime immDatePreviousDecember = DateUtils.getUTCDate(year - 1, 12, 20);
    final ZonedDateTime immDateMarch = DateUtils.getUTCDate(year, 3, 20);
    final ZonedDateTime immDateJune = DateUtils.getUTCDate(year, 6, 20);
    final ZonedDateTime immDateSeptember = DateUtils.getUTCDate(year, 9, 20);
    final ZonedDateTime immDateDecember = DateUtils.getUTCDate(year, 12, 20);
    final ZonedDateTime immDateNextMarch = DateUtils.getUTCDate(year + 1, 3, 20);

    /*
    System.out.println(immDatePreviousDecember);
    System.out.println(immDateMarch);
    System.out.println(immDateJune);
    System.out.println(immDateSeptember);
    System.out.println(immDateDecember);
    System.out.println(immDateNextMarch);
     */

    // -------------------------------------------------------------------

    // First of all check that the maturity date isn't one of the IMM dates for 'year'
    // These tests might be a bit redundant

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

    if (maturityDate.isAfter(immDatePreviousDecember) && maturityDate.isBefore(immDateMarch)) {
      immAdjustedMaturityDate = immDateMarch;
    }

    if (maturityDate.isAfter(immDateMarch) && maturityDate.isBefore(immDateJune)) {
      immAdjustedMaturityDate = immDateJune;
    }

    if (maturityDate.isAfter(immDateJune) && maturityDate.isBefore(immDateSeptember)) {
      immAdjustedMaturityDate = immDateSeptember;
    }

    if (maturityDate.isAfter(immDateSeptember) && maturityDate.isBefore(immDateDecember)) {
      immAdjustedMaturityDate = immDateDecember;
    }

    if (maturityDate.isAfter(immDateDecember) && maturityDate.isBefore(immDateNextMarch)) {
      immAdjustedMaturityDate = immDateNextMarch;
    }

    // -------------------------------------------------------------------

    //System.out.println("Day = " + day + ", month = " + month + ", year = " + year);

    return immAdjustedMaturityDate;
  }

  // -------------------------------------------------------------------------------------------

  // Method to adjust the IMM adjusted maturity date for non-business days (if required by the user)
  ZonedDateTime businessDayAdjustMaturityDate(ZonedDateTime maturityDate, Calendar calendar, boolean adjustMaturityDate) {

    ZonedDateTime adjustedMaturityDate = maturityDate;

    // If required adjust the maturity date so it does not fall on a non-business day
    if (adjustMaturityDate) {
      while (!calendar.isWorkingDay(adjustedMaturityDate.toLocalDate())) {
        adjustedMaturityDate = adjustedMaturityDate.plusDays(1);
      }
    }

    return adjustedMaturityDate;
  }

  // -------------------------------------------------------------------------------------------
}
