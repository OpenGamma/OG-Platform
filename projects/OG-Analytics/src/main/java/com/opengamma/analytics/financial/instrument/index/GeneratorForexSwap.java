/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexSwapDefinition;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of Forex swaps (currencies, conventions, ...).
 */
public class GeneratorForexSwap extends GeneratorInstrument {

  /**
   * The first currency. Not null.
   */
  private final Currency _currency1;
  /**
   * The second currency. Not null.
   */
  private final Currency _currency2;
  /**
   * The joint calendar of both currencies. Not null.
   */
  private final Calendar _calendar;
  /**
   * The index spot lag in days between trade and spot date (usually 2).
   */
  private final int _spotLag;
  /**
   * The business day convention.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used.
   */
  private final boolean _endOfMonth;

  /**
   * Constructor
   * @param name The generator name.
   * @param currency1 The first currency. Not null.
   * @param currency2 The second currency. Not null.
   * @param calendar The joint calendar of both currencies. Not null.
   * @param spotLag The index spot lag in days between trade and spot date (usually 2).
   * @param businessDayConvention The business day convention.
   * @param endOfMonth The flag indicating if the end-of-month rule is used.
   */
  public GeneratorForexSwap(String name, Currency currency1, Currency currency2, Calendar calendar, int spotLag, BusinessDayConvention businessDayConvention, boolean endOfMonth) {
    super(name);
    Validate.notNull(currency1, "Currency 1");
    Validate.notNull(currency2, "Currency 2");
    Validate.notNull(calendar, "Calendar");
    Validate.notNull(businessDayConvention, "Business day convention");
    this._currency1 = currency1;
    this._currency2 = currency2;
    this._calendar = calendar;
    this._spotLag = spotLag;
    this._businessDayConvention = businessDayConvention;
    this._endOfMonth = endOfMonth;
  }

  /**
   * Gets the _currency1 field.
   * @return the _currency1
   */
  public Currency getCurrency1() {
    return _currency1;
  }

  /**
   * Gets the _currency2 field.
   * @return the _currency2
   */
  public Currency getCurrency2() {
    return _currency2;
  }

  /**
   * Gets the _calendar field.
   * @return the _calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the _spotLag field.
   * @return the _spotLag
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the _businessDayConvention field.
   * @return the _businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the _endOfMonth field.
   * @return the _endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  @Override
  /**
   * The Forex swap starts at spot and end at spot+tenor.
   */
  public ForexSwapDefinition generateInstrument(final ZonedDateTime date, final Period tenor, final double forwardPoints, final double notional, final Object... objects) {
    ArgumentChecker.isTrue(objects.length == 1, "Forex rate required");
    ArgumentChecker.isTrue((objects[0] instanceof Double) || (objects[0] instanceof FXMatrix), "forex rate should be a double");
    Double fx;
    if (objects[0] instanceof Double) {
      fx = (Double) objects[0];
    } else {
      fx = ((FXMatrix) objects[0]).getFxRate(_currency1, _currency2);
    }
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, tenor, _businessDayConvention, _calendar, _endOfMonth);
    return new ForexSwapDefinition(_currency1, _currency2, startDate, endDate, notional, fx, forwardPoints);
  }

  @Override
  /**
   * The Forex swap starts at spot+startTenor and end at spot+endTenor.
   */
  public ForexSwapDefinition generateInstrument(final ZonedDateTime date, final Period startTenor, final Period endTenor, final double forwardPoints, final double notional, final Object... objects) {
    ArgumentChecker.isTrue(objects.length == 1, "Forex rate required");
    ArgumentChecker.isTrue((objects[0] instanceof Double) || (objects[0] instanceof FXMatrix), "forex rate should be a double");
    Double fx;
    if (objects[0] instanceof Double) {
      fx = (Double) objects[0];
    } else {
      fx = ((FXMatrix) objects[0]).getFxRate(_currency1, _currency2);
    }
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, startTenor, _businessDayConvention, _calendar, _endOfMonth);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(spot, endTenor, _businessDayConvention, _calendar, _endOfMonth);
    return new ForexSwapDefinition(_currency1, _currency2, startDate, endDate, notional, fx, forwardPoints);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency1.hashCode();
    result = prime * result + _currency2.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _spotLag;
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
    GeneratorForexSwap other = (GeneratorForexSwap) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency1, other._currency1)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency2, other._currency2)) {
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
