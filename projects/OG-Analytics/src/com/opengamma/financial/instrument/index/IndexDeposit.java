/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;

/**
 * Class describing an deposit-like index (in particular Ibor and OIS).
 */
public abstract class IndexDeposit {

  /**
   * The name of the index. Not null.
   */
  private final String _name;
  /**
   * The index currency. Not null.
   */
  private final Currency _currency;
  /**
   * The calendar associated to the index. Not null.
   */
  private final Calendar _calendar;

  /**
   * Constructor.
   * @param name The index name.
   * @param currency The underlying currency.
   * @param calendar The calendar.
   */
  public IndexDeposit(String name, Currency currency, Calendar calendar) {
    Validate.notNull(name, "Index: name");
    Validate.notNull(currency, "Index: currency");
    Validate.notNull(calendar, "Index: calendar");
    _name = name;
    _currency = currency;
    _calendar = calendar;
  }

  /**
   * Gets the name of the index.
   * @return The name.
   */
  public String getName() {
    return _name;
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

  @Override
  public String toString() {
    return _name + "-" + _currency.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calendar.hashCode();
    result = prime * result + _currency.hashCode();
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
    IndexDeposit other = (IndexDeposit) obj;
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
