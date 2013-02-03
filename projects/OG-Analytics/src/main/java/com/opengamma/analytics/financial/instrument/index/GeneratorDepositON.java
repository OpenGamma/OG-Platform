/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of overnight deposit characteristics (conventions, calendar, ...).
 */
public class GeneratorDepositON extends GeneratorInstrument {

  /**
   * The index currency. Not null.
   */
  private final Currency _currency;
  /**
   * The calendar associated to the index. Not null.
   */
  private final Calendar _calendar;
  /**
   * The day count convention associated to the generator. Not null.
   */
  private final DayCount _dayCount;

  /**
   * Deposit generator from all the financial details.
   * @param name The generator name. Not null.
   * @param currency The index currency. Not null.
   * @param calendar The calendar associated to the index. Not null.
   * @param dayCount The day count convention associated to the index.
   */
  public GeneratorDepositON(final String name, final Currency currency, final Calendar calendar, final DayCount dayCount) {
    super(name);
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    _currency = currency;
    _calendar = calendar;
    _dayCount = dayCount;
  }

  /**
   * Gets the index currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the calendar associated to the index.
   * @return The calendar.
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the day count convention associated to the index.
   * @return The day count convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  @Override
  /**
   * Generate an overnight deposit.
   * @param date The reference date.
   * @param tenor The period (only with days) up to the start of the overnight deposit.
   * @param marketQuote The deposit rate.
   * @param notional The deposit notional.
   * @param objects No.
   * @return The overnight deposit.
   */
  public CashDefinition generateInstrument(final ZonedDateTime date, final Period tenor, final double marketQuote, final double notional, final Object... objects) {
    ArgumentChecker.notNull(date, "Reference date");
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, tenor, _calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, 1, _calendar);
    final double accrualFactor = _dayCount.getDayCountFraction(startDate, endDate);
    return new CashDefinition(_currency, startDate, endDate, notional, marketQuote, accrualFactor);
  }

  @Override
  /**
   * Generate an overnight deposit.
   * @param date The reference date.
   * @param startTenor The period (only with days) up to the start of the overnight deposit.
   * @param endTenor Not used.
   * @param marketQuote The deposit rate.
   * @param notional The deposit notional.
   * @param objects No.
   * @return The overnight deposit.
   */
  public CashDefinition generateInstrument(final ZonedDateTime date, final Period startTenor, final Period endTenor, final double marketQuote, final double notional, final Object... objects) {
    return generateInstrument(date, startTenor, marketQuote, notional);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    GeneratorDepositON other = (GeneratorDepositON) obj;
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    return true;
  }

}
