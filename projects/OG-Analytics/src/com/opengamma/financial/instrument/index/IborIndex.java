/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Class describing an Ibor-like index.
 */
public class IborIndex {

  //TODO: add name?
  /**
   * The index currency.
   */
  private final Currency _currency;
  /**
   * Tenor of the index.
   */
  private final Tenor _tenor;
  /**
   * Number of days between the trade date and settlement date.
   */
  private final int _settlementDays;
  /**
   * Calendar associated to the index.
   */
  private final Calendar _calendar;
  /**
   * The day count convention used to compute the accrual factor associated to the period.
   */
  private final DayCount _dayCount;
  /**
   * Business day convention used when a date related to the index is not a good business day.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * Flag indicating if the end-of-month rule is used.
   */
  private final boolean _endOfMonth;

  /**
   * Constructor from the index details.
   * @param currency The index currency.
   * @param tenor The index tenor.
   * @param spotLag The index spot lag (usually 2 or 0).
   * @param calendar The calendar associated to the index.
   * @param dayCount The day count convention associated to the index.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   */
  public IborIndex(Currency currency, Tenor tenor, int spotLag, Calendar calendar, DayCount dayCount, BusinessDayConvention businessDayConvention, boolean endOfMonth) {
    Validate.notNull(currency, "currency");
    _currency = currency;
    Validate.notNull(tenor, "tenor");
    this._tenor = tenor;
    this._settlementDays = spotLag;
    Validate.notNull(calendar, "calendar");
    this._calendar = calendar;
    Validate.notNull(dayCount, "day count");
    this._dayCount = dayCount;
    Validate.notNull(businessDayConvention, "business day convention");
    this._businessDayConvention = businessDayConvention;
    this._endOfMonth = endOfMonth;
  }

  /**
   * Gets the _currency field.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the tenor field.
   * @return the tenor
   */
  public Tenor getTenor() {
    return _tenor;
  }

  /**
   * Gets the spotLag field.
   * @return the spotLag
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Gets the calendar field.
   * @return the calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the dayCount field.
   * @return the dayCount
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the businessDayConvention field.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the endOfMonth field.
   * @return the endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
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
    result = prime * result + _settlementDays;
    result = prime * result + _tenor.hashCode();
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
    IborIndex other = (IborIndex) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_settlementDays != other._settlementDays) {
      return false;
    }
    if (!ObjectUtils.equals(_tenor, other._tenor)) {
      return false;
    }
    return true;
  }

}
