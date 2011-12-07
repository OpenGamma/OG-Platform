/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of swap characteristics.
 */
public class SwapGenerator {

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

  // Implementation comment: business day convention, calendar and EOM from IborIndex.

  /**
   * Constructor from all the details.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   */
  public SwapGenerator(Period fixedLegPeriod, DayCount fixedLegDayCount, IborIndex iborIndex) {
    Validate.notNull(fixedLegPeriod, "fixed leg period");
    Validate.notNull(fixedLegDayCount, "fixed leg day count");
    Validate.notNull(iborIndex, "ibor index");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _iborIndex = iborIndex;
    _name = iborIndex.getCurrency().toString() + iborIndex.getTenor().toString() + fixedLegPeriod.toString();
  }

  /**
   * Gets the _name field.
   * @return the _name
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

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _fixedLegDayCount.hashCode();
    result = prime * result + _fixedLegPeriod.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _name.hashCode();
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
    SwapGenerator other = (SwapGenerator) obj;
    if (!ObjectUtils.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedLegPeriod, other._fixedLegPeriod)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }

}
