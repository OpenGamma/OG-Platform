/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;

/**
 * Generator (or template) for OIS.
 */
public class GeneratorOIS {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The ON index of the floating leg.
   */
  private final IndexON _index;
  /**
   * The legs period of payments (both fixed and ON legs have the same payment frequency).
   */
  private final Period _legsPeriod;
  /**
   * The fixed leg day count.
   */
  private final DayCount _fixedLegDayCount;
  /**
   * The business day convention for the payments (used for both legs).
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used (used for both legs).
   */
  private final boolean _endOfMonth;
  /**
   * The spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;
  /**
   * The lag in days between the last ON fixing date and the coupon payment. Usually is the same as the _spotLag.
   */
  private final int _paymentLag;
  /**
   * In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   */
  private final boolean _stubShort;
  /**
   * The dates in the schedule can be computed from the end date (true) or from the start date (false).
   */
  private final boolean _fromEnd;

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param index The ON index of the floating leg.
   * @param legsPeriod The legs period of payments (both fixed and ON legs have the same payment frequency).
   * @param fixedLegDayCount The fixed leg day count.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  public GeneratorOIS(final String name, final IndexON index, final Period legsPeriod, final DayCount fixedLegDayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final int spotLag) {
    Validate.notNull(name, "Name");
    Validate.notNull(legsPeriod, "Period");
    Validate.notNull(fixedLegDayCount, "Fixed leg day count");
    Validate.notNull(businessDayConvention, "Business day convention");
    Validate.notNull(index, "Index ON");
    _name = name;
    _index = index;
    this._legsPeriod = legsPeriod;
    this._fixedLegDayCount = fixedLegDayCount;
    this._businessDayConvention = businessDayConvention;
    this._endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = spotLag;
    _stubShort = true;
    _fromEnd = true;
  }

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param index The ON index of the floating leg.
   * @param legsPeriod The legs period of payments (both fixed and ON legs have the same payment frequency).
   * @param fixedLegDayCount The fixed leg day count.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param paymentLag The lag in days between the last ON fixing date and the coupon payment.
   */
  public GeneratorOIS(final String name, final IndexON index, final Period legsPeriod, final DayCount fixedLegDayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth,
      final int spotLag, final int paymentLag) {
    Validate.notNull(name, "Name");
    Validate.notNull(legsPeriod, "Period");
    Validate.notNull(fixedLegDayCount, "Fixed leg day count");
    Validate.notNull(businessDayConvention, "Business day convention");
    Validate.notNull(index, "Index ON");
    _name = name;
    _index = index;
    this._legsPeriod = legsPeriod;
    this._fixedLegDayCount = fixedLegDayCount;
    this._businessDayConvention = businessDayConvention;
    this._endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = paymentLag;
    _stubShort = true;
    _fromEnd = true;
  }

  /**
   * Gets the generator name.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the legs period of payments (both fixed and ON legs have the same payment frequency).
   * @return The period.
   */
  public Period getLegsPeriod() {
    return _legsPeriod;
  }

  /**
   * Gets the fixed leg day count.
   * @return The day count.
   */
  public DayCount getFixedLegDayCount() {
    return _fixedLegDayCount;
  }

  /**
   * Gets The business day convention for the payments (used for both legs).
   * @return The business day convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the flag indicating if the end-of-month rule is used (used for both legs).
   * @return The flag indicating if the end-of-month rule is used.
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the ON index of the floating leg.
   * @return The ON index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the flag indicating if the remaining interval is shorter (true) or longer (false) than the requested period.
   * @return The flag.
   */
  public boolean isStubShort() {
    return _stubShort;
  }

  /**
   * Gets the flag indicating if dates in the schedule are be computed from the end date (true) or from the start date (false).
   * @return The flag.
   */
  public boolean isFromEnd() {
    return _fromEnd;
  }

  /**
   * Gets the spot lag in days between trade and settlement date (usually 2 or 0).
   * @return The spot lag.
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the lag in days between the last ON fixing date and the coupon payment.
   * @return The payment lag.
   */
  public int getPaymentLag() {
    return _paymentLag;
  }

  /**
   * Gets the calendar associated to the OIS index.
   * @return The calendar.
   */
  public Calendar getCalendar() {
    return _index.getCalendar();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _fixedLegDayCount.hashCode();
    result = prime * result + (_fromEnd ? 1231 : 1237);
    result = prime * result + _index.hashCode();
    result = prime * result + _legsPeriod.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + (_stubShort ? 1231 : 1237);
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
    final GeneratorOIS other = (GeneratorOIS) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (_fromEnd != other._fromEnd) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_legsPeriod, other._legsPeriod)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (_stubShort != other._stubShort) {
      return false;
    }
    return true;
  }

}
