/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import java.util.EnumSet;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Month;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.financial.analytics.ircurve.NextQuarterAdjuster;
import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Expiry calculator for futures and options on the VIX, the CBOE Volatility Index. <p>
 * VX Futures, traded on CFE, are specified here: http://cfe.cboe.com/Products/Spec_VIX.aspx <p>
 * VIX Index Options, traded on CBOE, are specified here:  <p>
 * <p>
 * Contract Months = The Exchange may list for trading up to nine near-term serial months and five months on the February quarterly cycle for the VIX futures contract.<p>
 * Expiry = The Wednesday that is thirty days prior to the third Friday of the calendar month immediately following the month in which the contract expires ("Final Settlement Date"). 
 * If the third Friday of the month subsequent to expiration of the applicable VIX futures contract is a CBOE holiday, expiry shall be thirty days prior to the CBOE business day preceding that Friday.
 * 
 */
public final class VixFutureAndOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  private static final int N_SERIAL_EXPIRIES = 9;
  private static final Set<Month> QUARTERLY_CYCLE_MONTHS = EnumSet.of(Month.FEBRUARY, Month.MAY, Month.AUGUST, Month.NOVEMBER); 
  private static final NextQuarterAdjuster s_nextQuarterAdjuster = new NextQuarterAdjuster(QUARTERLY_CYCLE_MONTHS);
  private static final TemporalAdjuster s_dayOfMonthAdjuster = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.FRIDAY);

  private static final VixFutureAndOptionExpiryCalculator INSTANCE = new VixFutureAndOptionExpiryCalculator();
  
  /** @return the singleton instance of the calculator, not null */
  public static VixFutureAndOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private VixFutureAndOptionExpiryCalculator() {
  }
  
  
  @Override
  /**
   * Gets monthly expiries for the first N_SERIAL_MONTHS, then switches to quarterly along the FEBRUARY cycle
   * @param nthFuture nth future
   * @param valDate The date from which to start
   * @return the expiry date of the nth option
   */
  public LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar) {
    ArgumentChecker.isTrue(n > 0, "n must be greater than 0.");
    if (n <= N_SERIAL_EXPIRIES) { // We look for monthly expiries
      return getMonthlyExpiry(n, today, holidayCalendar);
    }
    // And Quarterly expiries thereafter
    final int nthExpiryAfterSerialContracts = n - N_SERIAL_EXPIRIES;
    final LocalDate lastSerialExpiry = getMonthlyExpiry(N_SERIAL_EXPIRIES, today, holidayCalendar);
    return getQuarterlyExpiry(nthExpiryAfterSerialContracts, lastSerialExpiry, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(int n, LocalDate today) {
    return getExpiryDate(n, today, FutureOptionUtils.WEEKDAYS);
  }
  
  /**
   * Given the expiry rule, returns the expiry date of the nth month.
   * @param nthExpiry The nth expiry, greater than zero
   * @param date The date, not null
   * @param holidayCalendar calendar containing holidays
   * @return The expiry date of the nth monthly instrument
   */
  private LocalDate getMonthlyExpiry(final int nthExpiry, final LocalDate date, final Calendar holidayCalendar) {
    ArgumentChecker.notNegativeOrZero(nthExpiry, "nth expiry");
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(holidayCalendar, "holidayCalendar");
    // Compute the expiry of valuationDate's month
    LocalDate nextExpiry = getNextSerialExpiry(date, holidayCalendar);
    if (!nextExpiry.isAfter(date)) { // If it is not strictly after valuationDate...
      nextExpiry = getNextSerialExpiry(date.plusMonths(1), holidayCalendar);
    }
    if (nthExpiry == 1) {
      return nextExpiry;
    }
    return getNextSerialExpiry(nextExpiry.plusMonths(nthExpiry - 1), holidayCalendar);
  }
  
  // Return expiryDate that is 30 days before the 3rd Friday (or previous good day if holiday) of month following date
  private LocalDate getNextSerialExpiry(final LocalDate date, final Calendar holidayCalendar) {
    // Compute the expiry of valuationDate's month
    LocalDate following3rdFriday = date.plusMonths(1).with(s_dayOfMonthAdjuster); // 3rd Friday of following month
    while (!holidayCalendar.isWorkingDay(following3rdFriday)) {
      following3rdFriday = following3rdFriday.minusDays(1); // previous good day
    }
    return following3rdFriday.minusDays(30); // expiry is 30 days before
  }
  private LocalDate getQuarterlyExpiry(int nthExpiryAfterSerialContracts, LocalDate lastSerialExpiry, Calendar holidayCalendar) {
    // First find the nth quarter after the lastSerialExpiry
    LocalDate nthExpiryMonth = lastSerialExpiry;
    for (int n = nthExpiryAfterSerialContracts; n > 0; n--) {
      nthExpiryMonth = nthExpiryMonth.with(s_nextQuarterAdjuster);
    }
    // Then find the expiry date in that month
    return getNextSerialExpiry(nthExpiryMonth, holidayCalendar);
  }
  
  @Override
  public String getName() {
    return this.getClass().getName();
  }

}
