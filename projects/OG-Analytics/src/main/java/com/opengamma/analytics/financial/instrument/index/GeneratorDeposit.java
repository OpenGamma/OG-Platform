/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.CashDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of deposit characteristics (conventions, calendar, ...).
 */
public class GeneratorDeposit extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The index currency. Not null.
   */
  private final Currency _currency;
  /**
   * The calendar associated to the index. Not null.
   */
  private final Calendar _calendar;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;
  /**
   * The day count convention associated to the index.
   */
  private final DayCount _dayCount;
  /**
   * The business day convention associated to the index.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used.
   */
  private final boolean _endOfMonth;

  /**
   * Deposit generator from all the financial details.
   * @param name The generator name. Not null.
   * @param currency The index currency. Not null.
   * @param calendar The calendar associated to the index. Not null.
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param dayCount The day count convention associated to the index.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth Flag indicating if the end-of-month rule is used.
   */
  public GeneratorDeposit(final String name, final Currency currency, final Calendar calendar, final int spotLag, final DayCount dayCount,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth) {
    super(name);
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    _currency = currency;
    _calendar = calendar;
    _spotLag = spotLag;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
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
   * Gets the index spot lag in days between trade and settlement date.
   * @return The spot lag in days.
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the day count convention associated to the index.
   * @return The day count convention.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the business day convention associated to the index.
   * @return The business day convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the flag indicating if the end-of-month rule is used.
   * @return The EOM flag.
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * {@inheritDoc}
   * The deposit start at spot+start tenor and end at spot+end tenor.
   */
  @Override
  public CashDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), this);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, attribute.getEndPeriod(), this);
    final double accrualFactor = _dayCount.getDayCountFraction(startDate, endDate, _calendar);
    return new CashDefinition(_currency, startDate, endDate, notional, rate, accrualFactor);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _spotLag;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GeneratorDeposit other = (GeneratorDeposit) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    return true;
  }

}
