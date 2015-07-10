/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Year;

/**
 * 
 */
public class ActualNL extends ActualTypeDayCount {
  private static final long serialVersionUID = 1L;

  @Override
  public double getAccruedInterest(final LocalDate previousCouponDate, final LocalDate date, final LocalDate nextCouponDate, final double coupon, final double paymentsPerYear) {
    return getDayCountFraction(previousCouponDate, date) * coupon;
  }

  @Override
  public double getDayCountFraction(final LocalDate firstDate, final LocalDate secondDate) {
    testDates(firstDate, secondDate);
    long actualDays = secondDate.toEpochDay() - firstDate.toEpochDay();
    int numberOfLeapDays = 0;
    LocalDate temp = nextLeapDay(firstDate);
    while (temp.isAfter(secondDate) == false) {
      numberOfLeapDays++;
      temp = nextLeapDay(temp);
    }
    return (actualDays - numberOfLeapDays) / 365d;
  }

  // finds the next leap day after the input date
  private static LocalDate nextLeapDay(LocalDate input) {
    // already a leap day, move forward either 4 or 8 years
    if (input.getMonthValue() == 2 && input.getDayOfMonth() == 29) {
      return ensureLeapDay(input.getYear() + 4);
    }
    // handle if before February 29 in a leap year
    if (input.isLeapYear() && input.getMonthValue() <= 2) {
      return LocalDate.of(input.getYear(), 2, 29);
    }
    // handle any other date
    return ensureLeapDay(((input.getYear() / 4) * 4) + 4);
  }

  // handle 2100, which is not a leap year
  private static LocalDate ensureLeapDay(int possibleLeapYear) {
    if (Year.isLeap(possibleLeapYear)) {
      return LocalDate.of(possibleLeapYear, 2, 29);
    } else {
      return LocalDate.of(possibleLeapYear + 4, 2, 29);
    }
  }

  @Override
  public String getName() {
    return "Actual/NL";
  }

}
