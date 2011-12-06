/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.Convention;
import com.opengamma.util.money.Currency;

/**
 * Class describing an Ibor-like index.
 */
public class IborIndex extends IndexDeposit {

  /**
   * Tenor of the index.
   */
  private final Period _tenor;
  /**
   * The conventions linked to the index.
   */
  private final Convention _convention;
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
  public IborIndex(Currency currency, Period tenor, int spotLag, Calendar calendar, DayCount dayCount, BusinessDayConvention businessDayConvention, boolean endOfMonth) {
    this(currency, tenor, spotLag, calendar, dayCount, businessDayConvention, endOfMonth, "Ibor");
  }

  /**
   * Constructor from the index details.
   * @param currency The index currency.
   * @param tenor The index tenor.
   * @param spotLag The index spot lag (usually 2 or 0).
   * @param calendar The calendar associated to the index.
   * @param dayCount The day count convention associated to the index.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param name The index name.
   */
  public IborIndex(Currency currency, Period tenor, int spotLag, Calendar calendar, DayCount dayCount, BusinessDayConvention businessDayConvention, boolean endOfMonth, final String name) {
    super(name, currency, calendar);
    Validate.notNull(tenor, "tenor");
    _tenor = tenor;
    Validate.notNull(calendar, "calendar");
    Validate.notNull(dayCount, "day count");
    Validate.notNull(businessDayConvention, "business day convention");
    _convention = new Convention(spotLag, dayCount, businessDayConvention, calendar, "Ibor conventions");
    _endOfMonth = endOfMonth;
  }

  /**
   * Gets the tenor field.
   * @return the tenor
   */
  public Period getTenor() {
    return _tenor;
  }

  /**
   * Gets the spotLag field.
   * @return the spotLag
   */
  public int getSettlementDays() {
    return _convention.getSettlementDays();
  }

  /**
   * Gets the calendar field.
   * @return the calendar
   */
  @Override
  public Calendar getCalendar() {
    return _convention.getWorkingDayCalendar();
  }

  /**
   * Gets the dayCount field.
   * @return the dayCount
   */
  public DayCount getDayCount() {
    return _convention.getDayCount();
  }

  /**
   * Gets the businessDayConvention field.
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _convention.getBusinessDayConvention();
  }

  /**
   * Gets the endOfMonth field.
   * @return the endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the _convention field.
   * @return The index conventions
   */
  public Convention getConvention() {
    return _convention;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _convention.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _tenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    IborIndex other = (IborIndex) obj;
    if (!ObjectUtils.equals(_convention, other._convention)) {
      return false;
    }
    if (!ObjectUtils.equals(_tenor, other._tenor)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    return true;
  }

}
