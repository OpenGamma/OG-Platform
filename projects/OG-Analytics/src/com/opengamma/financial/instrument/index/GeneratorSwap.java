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
import com.opengamma.util.money.Currency;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorSwap {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The fixed leg period of payments.
   */
  private final Period _fixedLegPeriod;
  /**
   * The fixed leg day count.
   */
  private final DayCount _fixedLegDayCount;
  /**
   * The Ibor index of the floating leg.
   */
  private final IborIndex _iborIndex;
  /**
   * The business day convention associated to the index.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used.
   */
  private final boolean _endOfMonth;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;

  // REVIEW: Do we need stubShort and stubFirst flags?

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   */
  public GeneratorSwap(Period fixedLegPeriod, DayCount fixedLegDayCount, IborIndex iborIndex) {
    Validate.notNull(fixedLegPeriod, "fixed leg period");
    Validate.notNull(fixedLegDayCount, "fixed leg day count");
    Validate.notNull(iborIndex, "ibor index");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _iborIndex = iborIndex;
    _name = iborIndex.getCurrency().toString() + iborIndex.getTenor().toString() + fixedLegPeriod.toString();
    _businessDayConvention = iborIndex.getBusinessDayConvention();
    _endOfMonth = iborIndex.isEndOfMonth();
    _spotLag = iborIndex.getSpotLag();
  }

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param spotLag The swap spot lag (usually 2 or 0).
   */
  public GeneratorSwap(Period fixedLegPeriod, DayCount fixedLegDayCount, IborIndex iborIndex, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final int spotLag) {
    Validate.notNull(fixedLegPeriod, "fixed leg period");
    Validate.notNull(fixedLegDayCount, "fixed leg day count");
    Validate.notNull(iborIndex, "ibor index");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _iborIndex = iborIndex;
    _name = iborIndex.getCurrency().toString() + iborIndex.getTenor().toString() + fixedLegPeriod.toString();
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
  }

  /**
   * Gets the generator name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the _fixedLegPeriod field.
   * @return the _fixedLegPeriod
   */
  public Period getFixedLegPeriod() {
    return _fixedLegPeriod;
  }

  /**
   * Gets the _fixedLegDayCount field.
   * @return the _fixedLegDayCount
   */
  public DayCount getFixedLegDayCount() {
    return _fixedLegDayCount;
  }

  /**
   * Gets the _iborIndex field.
   * @return the _iborIndex
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the generator currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _iborIndex.getCurrency();
  }

  /**
   * Gets the generator calendar.
   * @return The calendar.
   */
  public Calendar getCalendar() {
    return _iborIndex.getCalendar();
  }

  /**
   * Gets the swap generator business day convention.
   * @return The convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the swap generator spot lag.
   * @return The lag (in days).
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the swap generator end-of-month rule.
   * @return The EOM.
   */
  public Boolean isEndOfMonth() {
    return _endOfMonth;
  }

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _fixedLegDayCount.hashCode();
    result = prime * result + _fixedLegPeriod.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _name.hashCode();
    result = prime * result + _spotLag;
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
    GeneratorSwap other = (GeneratorSwap) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedLegPeriod, other._fixedLegPeriod)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    return true;
  }

}
