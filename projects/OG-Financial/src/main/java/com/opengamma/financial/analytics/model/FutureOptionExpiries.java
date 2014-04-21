/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.analytics.ircurve.NextExpiryAdjuster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.time.Tenor;

/**
 *  Utility Class for computing Expiries of Future Options from ordinals (i.e. nth future after valuationDate)
 *  For IR Options use: TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY), new NextExpiryAdjuster()
 *  For Equity Options use: new SaturdayAfterThirdFridayAdjuster(), new NextEquityExpiryAdjuster()
 */
//TODO there is far too much hard-coding of assumptions (e.g. when to switch from serial to quarterly in this class.
// Add information to the surface instrument provider, not in here
public final class FutureOptionExpiries implements ExchangeTradedInstrumentExpiryCalculator {

  /** Instance of {@code FutureOptionExpiries} used for Interest Rate Future Options. (Expiries on 3rd Wednesdays) */
  public static final FutureOptionExpiries IR = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.WEDNESDAY));
  /** Instance of {@code FutureOptionExpiries} used for Equity Future Options. (Expiries on Saturdays after 3rd Fridays) */
  public static final FutureOptionExpiries EQUITY = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY, 1));
  /** Instance of {@code FutureOptionExpiries} used for Equity Futures. (Expiries on 3rd Fridays) */
  public static final FutureOptionExpiries EQUITY_FUTURE = FutureOptionExpiries.of(new NextExpiryAdjuster(3, DayOfWeek.FRIDAY));

  /** The adjuster moves forward to the next IMM month, to the specified day in month*/
  private final NextExpiryAdjuster _nextExpiryAdjuster;

  /** _nextExpiryAdjuster.getDayOfMonthAdjuster() moves date to day within month. eg 3rd Wednesday */

  /**
   * Utility Class for computing Expiries of Future Options from ordinals (i.e. nth future after valuationDate)
   * @param nextExpiryAdjuster Examples: NextExpiryAdjuster, NextExpiryAdjuster
   */
  private FutureOptionExpiries(final NextExpiryAdjuster nextExpiryAdjuster) {
    ArgumentChecker.notNull(nextExpiryAdjuster, "nextExpiryAdjuster");
    _nextExpiryAdjuster = nextExpiryAdjuster;
  }

  /**
   * Create instance of {@code FutureOptionExpiries},
   * a utility Class for computing Expiries of Future Options from ordinals (i.e. nth option after valuationDate)
   * @param nextExpiryAdjuster Examples: NextExpiryAdjuster, NextExpiryAdjuster
   * @return the FutureOptionExpiries class, never null
   */
  public static FutureOptionExpiries of(final NextExpiryAdjuster nextExpiryAdjuster) {
    ArgumentChecker.notNull(nextExpiryAdjuster, "nextExpiryAdjuster");
    return new FutureOptionExpiries(nextExpiryAdjuster);
  }

  /**
   * Create instance of {@code FutureOptionExpiries},
   * a utility Class for computing Expiries of Future Options from ordinals (i.e. nth option after valuationDate). <p>
   * Specify the day and week in the month, plus an offset in number of days. <p>
   *  e.g. (3,DayOfWeek.FRIDAY,1) is the Saturday after the 3rd Friday in the month. This is different from the 3rd Saturday.
   * @param week Ordinal of week in month, beginning from 1.
   * @param day DayOfWeek
   * @param offset Integer offset, positive or negative from the result of week,day.
   * @return New instance of FutureOptionExpiries
   */
  public static FutureOptionExpiries of(final int week, final DayOfWeek day, final int offset) {
    final NextExpiryAdjuster nextExpiryAdjuster = new NextExpiryAdjuster(week, day, offset);
    return new FutureOptionExpiries(nextExpiryAdjuster);
  }

  /**
   * @deprecated
   * Compute time between now and future or future option's settlement date,
   * typically two business days before the third wednesday of the expiry month.
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  @Deprecated
  public Double getFutureOptionTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getFutureOptionExpiry(n, today);
    final LocalDate previousMonday = expiry;
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }

  /**
   * Compute time between now and future or future option's settlement date,
   * @param n nth Future after now
   * @param today Valuation Date
   * @param tenor The tenor of future option resets
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public Double getFutureOptionTtm(final int n, final LocalDate today, final Tenor tenor) {
    final LocalDate expiry = getExpiry(n, today, tenor);
    return TimeCalculator.getTimeBetween(today, expiry);
  }

  /**
   * Compute time between now and future or future option's settlement date,
   * typically two business days before the third wednesday of the expiry month.
   * @param n nth Future after now
   * @param today Valuation Date
   * @return OG-Analytic Time in years between now and the future's settlement date
   */
  public Double getFutureTtm(final int n, final LocalDate today) {
    final LocalDate expiry = getQuarterlyExpiry(n, today);
    final LocalDate previousMonday = expiry.minusDays(2); //TODO this should take a calendar and do two business days, and should use a convention for the number of days
    return TimeCalculator.getTimeBetween(today, previousMonday);
  }

  /**
   * @deprecated Hard-codes in assumptions about which expiries to look for
   * Gets monthly expiries for the first six months, then switch to quarterly
   * @param nthFuture nth future
   * @param valDate The date from which to start
   * @return the expiry date of the nth option
   */
  @Deprecated
  public LocalDate getFutureOptionExpiry(final int nthFuture, final LocalDate valDate) {
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    if (nthFuture <= 6) { // We look for expiries in the first 6 serial months after curveDate
      return getMonthlyExpiry(nthFuture, valDate);
    }
    // And for Quarterly expiries thereafter
    final int nthExpiryAfterSixMonths = nthFuture - 6;
    final LocalDate sixMonthsForward = getMonthlyExpiry(6, valDate);
    return getQuarterlyExpiry(nthExpiryAfterSixMonths, sixMonthsForward);
  }

  /**
   * @deprecated Hard-codes in assumptions about which expiries to look for
   * Get monthly expiries for the first 6 expires then look for January yearly ones
   * CME options seem to follow no clear pattern some switch to yearly after 4 monthly options and some have 8 or more
   * pick a value in the middle so hopefully we get a reasonable number of valid expires for all options
   * @param nthFuture nth future in the future
   * @param valDate date to start from
   * @return expiry the expiry date of the nth option
   */
  @Deprecated
  public LocalDate getCMEEquityFutureOptionExpiry(final int nthFuture, final LocalDate valDate) {
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    if (nthFuture <= 6) { // We look for expiries in the first 6 serial months after curveDate
      return getMonthlyExpiry(nthFuture, valDate);
    }
    // And for yearly January expiry after that
    final int nExpiryDone = nthFuture - 6;
    final LocalDate nextYear = LocalDate.of(valDate.getYear() + nExpiryDone, 1, 1);
    return getMonthlyExpiry(1, nextYear);
  }

  /**
   * @deprecated Hard-codes in assumptions about which expiries to look for.
   * Get n'th future expiry.
   * Only supports One Chicago equity futures. 2 serial months and 2 quarterly
   * http://www.onechicago.com/?page_id=22
   * @param nthFuture nth future in the future
   * @param valDate date to start from
   * @return expiry the expiry date of the nth option
   */
  @Deprecated
  public LocalDate getOneChicagoEquityFutureExpiry(final int nthFuture, final LocalDate valDate) {
    ArgumentChecker.isTrue(nthFuture > 0, "nthFuture must be greater than 0.");
    if (nthFuture > 4) {
      throw new OpenGammaRuntimeException("Can only have max 4 futures");
    }
    if (nthFuture <= 2) {
      return getMonthlyExpiry(nthFuture, valDate); // 2 serial months
    }
    // quarterly
    final int nQuarterlyLeft = nthFuture - 2;
    final LocalDate twoMonthsForward = getMonthlyExpiry(2, valDate);
    return getQuarterlyExpiry(nQuarterlyLeft, twoMonthsForward);
  }

  /**
   * Given a tenor, returns the nth expiry from the date according to the expiry rule. Only handles monthly and quarterly expiries at the moment.
   * @param nthExpiry The nth expiry, greater than zero
   * @param date The date, not null
   * @param tenor The tenor, not null
   * @return The expiry date
   * @throws IllegalArgumentException If the tenor is not monthly or quarterly
   */
  public LocalDate getExpiry(final int nthExpiry, final LocalDate date, final Tenor tenor) {
    ArgumentChecker.notNegativeOrZero(nthExpiry, "nth expiry");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(tenor, "tenor");
    if (tenor.equals(Tenor.ONE_MONTH)) {
      return getMonthlyExpiry(nthExpiry, date);
    }
    if (tenor.equals(Tenor.THREE_MONTHS)) {
      return getQuarterlyExpiry(nthExpiry, date);
    }
    throw new IllegalArgumentException("Could not handle frequency type " + tenor);
  }

  /**
   * Given the expiry rule, returns the expiry date of the nth month.
   * @param nthExpiry The nth expiry, greater than zero
   * @param date The date, not null
   * @return The expiry date of the nth monthly instrument
   */
  public LocalDate getMonthlyExpiry(final int nthExpiry, final LocalDate date) {
    ArgumentChecker.notNegativeOrZero(nthExpiry, "nth expiry");
    ArgumentChecker.notNull(date, "date");
    LocalDate expiry = date.with(_nextExpiryAdjuster.getDayOfMonthAdjuster()); // Compute the expiry of valuationDate's month
    if (!expiry.isAfter(date)) { // If it is not strictly after valuationDate...
      expiry = (date.plusMonths(1)).with(_nextExpiryAdjuster.getDayOfMonthAdjuster());
    }
    if (nthExpiry > 1) {
      expiry = (expiry.plusMonths(nthExpiry - 1)).with(_nextExpiryAdjuster.getDayOfMonthAdjuster());
    }
    return expiry;
  }

  /**
   * Given the expiry rule, returns the expiry date of the nth quarter.
   * @param nthExpiry The nth expiry, greater than zero
   * @param date The date, not null
   * @return The expiry date of the nth quarterly instrument
   */
  public LocalDate getQuarterlyExpiry(final int nthExpiry, final LocalDate date) {
    ArgumentChecker.notNegativeOrZero(nthExpiry, "nth expiry");
    ArgumentChecker.notNull(date, "date");
    LocalDate expiry = date.with(_nextExpiryAdjuster);
    for (int i = 1; i < nthExpiry; i++) {
      expiry = (expiry.plusDays(7)).with(_nextExpiryAdjuster);
    }
    return expiry;
  }

  @Override
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");

    LocalDate expiry = getExpiryMonth(n, today);
    while (!holidayCalendar.isWorkingDay(expiry)) {
      expiry = expiry.minusDays(1);
    }
    return expiry;
  }

  @Override
  public LocalDate getExpiryMonth(final int n, final LocalDate today) {
    return getMonthlyExpiry(n, today);
  }

  @Override
  public String getName() {
    return "FutureOptionExpiries with " + _nextExpiryAdjuster.toString();
  }

  /**
   * Produced n'th expiry date after today of monthly or quarterly tenor.<p>
   * Assumes a previous business day convention
   * @param n n'th expiry
   * @param today valuation date
   * @param tenor accepts ONE_MONTH or THREE_MONTHS
   * @param holidayCalendar Calendar
   * @return n'th expiry date
   */
  public LocalDate getExpiryDate(final int n, final LocalDate today, final Tenor tenor, final Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than zero; have {}", n);
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.notNull(tenor, "tenor");
    ArgumentChecker.notNull(holidayCalendar, "holiday calendar");
    LocalDate expiry;
    if (tenor.equals(Tenor.ONE_MONTH)) {
      expiry = getMonthlyExpiry(n, today);
    } else if (tenor.equals(Tenor.THREE_MONTHS)) {
      expiry = getQuarterlyExpiry(n, today);
    } else {
      throw new IllegalArgumentException("Could not handle frequency type " + tenor);
    }
    while (!holidayCalendar.isWorkingDay(expiry)) {
      expiry = expiry.minusDays(1);
    }
    return expiry;
  }
}
