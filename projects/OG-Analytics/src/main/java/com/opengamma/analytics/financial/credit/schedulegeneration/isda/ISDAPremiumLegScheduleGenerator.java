/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.schedulegeneration.IMMDates;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods to generate the premium leg cash-flow schedule for a CDS (following the market conventions for CDS)
 */
public class ISDAPremiumLegScheduleGenerator {
  // TODO : Add a check if the calendar is 'null' to signify no adjustment of business dates? Overloaded method
  // TODO : Fix the code for the back stubs (is there a better way of handling this than just duplicating code?) - front stubs seem to work okay
  // TODO : Add the code for the first coupons
  // TODO : Should convertDatesToDoubles be put somewhere else? To use it, need to create a GenerateCreditDefaultSwapPremiumLegSchedule object which is a bit wasteful
  // TODO : Look at TemporalAdjuster class for IMM date handling
  // TODO : FrontLong stubs - e.g. startDate = 20/12/2007, effDate = 21/12/2007; first coupon at 20/6/2008. ISDA model first coupon at 20/3/2008 (seems to use start date not eff date)
  // TODO : Remove one of the overloaded convertdatesToDoubles methods
  // TODO : Rewrite and integrate constructISDACompliantCashflowSchedule into the code better
  // TODO : Need to hook in the flags for adjusting various dates again
  // TODO : Add WORKDAY equivalent function

  public ZonedDateTime[] constructISDACompliantCreditDefaultSwapPremiumLegSchedule(final CreditDefaultSwapDefinition cds) {
    ArgumentChecker.notNull(cds, "cds");
    final ZonedDateTime startDate = cds.getStartDate();
    final ZonedDateTime endDate = cds.getMaturityDate();
    final boolean protectStart = cds.getProtectionStart();
    final StubType stubType = cds.getStubType();
    if (protectStart && endDate.equals(startDate)) {
      //note no adjustment of either date
      return new ZonedDateTime[] {startDate, startDate.plusDays(1)};
    }
    // Is the stub at the front end of the payment schedule
    if (stubType == StubType.FRONTSHORT || stubType == StubType.FRONTLONG) {
      final List<ZonedDateTime> reversedCashflowSchedule = new ArrayList<>();
      ZonedDateTime date = endDate;
      while (date.isAfter(startDate)) {
        reversedCashflowSchedule.add(date);
        date = date.minus(cds.getCouponFrequency().getPeriod());
      }
      // TODO : Check the FRONTSHORT/FRONTLONG logic here
      if (reversedCashflowSchedule.size() == 1 || date.isEqual(startDate) || stubType == StubType.FRONTSHORT) {
        reversedCashflowSchedule.add(startDate);
      } else {
        reversedCashflowSchedule.set(reversedCashflowSchedule.size() - 1, startDate);
      }
      //TODO this logic assumes list was populated with decreasing dates
      final int nDatesInSchedule = reversedCashflowSchedule.size();
      final ZonedDateTime[] cashflowSchedule = new ZonedDateTime[nDatesInSchedule];
      //TODO not handling protectStart
      // Remember if protectStart = TRUE then there is an extra day of accrued that is not captured here
      cashflowSchedule[nDatesInSchedule - 1] = reversedCashflowSchedule.get(0);
      final Calendar calendar = cds.getCalendar();
      final BusinessDayConvention bdc = cds.getBusinessDayAdjustmentConvention();
      for (int i = nDatesInSchedule - 2; i > 0; i--) {
        cashflowSchedule[nDatesInSchedule - i - 1] = bdc.adjustDate(calendar, reversedCashflowSchedule.get(i));
      }
      cashflowSchedule[0] = reversedCashflowSchedule.get(nDatesInSchedule - 1);
      return cashflowSchedule;
    }
    // TODO : Add the code for the back stub
    // Is the stub at the back end of the payment schedule
    throw new NotImplementedException();
  }

  //Public method to extract just the adjusted maturity date for a CDS according to the contract specifications (don't have to go through the whole schedule gen process)
  public ZonedDateTime getAdjustedMaturityDate(final CreditDefaultSwapDefinition cds) {
    ArgumentChecker.notNull(cds, "CDS");
    // Set the adjusted maturity date to be equal to the (user input) maturity date
    ZonedDateTime adjustedMaturityDate = cds.getMaturityDate();
    // If specified by the user adjust the maturity date to fall on the next IMM date
    if (cds.getIMMAdjustMaturityDate()) {
      adjustedMaturityDate = immAdjustDate(cds.getMaturityDate());
    }
    return adjustedMaturityDate;
  }

  // Method to adjust the specified maturity date to the next IMM date
  private ZonedDateTime immAdjustDate(final ZonedDateTime date) {
    // Start at the current maturityDate
    ZonedDateTime immAdjustedDate = date;
    // Get the year of the contract maturity
    final int year = date.getYear();
    // Construct an IMM object with the IMM dates for 'year'
    final IMMDates immDates = new IMMDates(year);
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
    return immAdjustedDate;
  }

}
