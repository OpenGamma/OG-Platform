/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an Ibor-like index.
 */
public class IborIndex extends IndexDeposit {

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
   * Tenor of the index.
   */
  private final Period _tenor;

  /**
   * {@link IborIndex} is used as a key within the curve system, thus {@link #hashCode()} needs to be fast.
   */
  private final int _hashCode;

  /**
   * Constructor from the index details. The name is set to "Ibor".
   * @param currency The index currency.
   * @param tenor The index tenor.
   * @param spotLag The index spot lag (usually 2 or 0).
   * @param dayCount The day count convention associated to the index.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @deprecated Use the constructor that takes an index name.
   */
  @Deprecated
  public IborIndex(final Currency currency, final Period tenor, final int spotLag, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final boolean endOfMonth) {
    this(currency, tenor, spotLag, dayCount, businessDayConvention, endOfMonth, "Ibor");
  }

  /**
   * Constructor from the index details.
   * @param currency The index currency.
   * @param tenor The index tenor.
   * @param spotLag The index spot lag (usually 2 or 0).
   * @param dayCount The day count convention associated to the index.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param name The index name.
   */
  public IborIndex(final Currency currency, final Period tenor, final int spotLag, final DayCount dayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final String name) {
    super(name, currency);
    ArgumentChecker.notNull(tenor, "tenor");
    _tenor = tenor;
    ArgumentChecker.notNull(dayCount, "day count");
    ArgumentChecker.notNull(businessDayConvention, "business day convention");
    _spotLag = spotLag;
    _dayCount = dayCount;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _hashCode = generateHashCode();
  }

  /**
   * Gets the tenor field.
   * @return the tenor
   */
  public Period getTenor() {
    return _tenor;
  }

  /**
   * Gets the spot lag (in days).
   * @return The spot lag.
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Gets the day count.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Gets the business day convention.
   * @return The business day convention.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the end-of-month rule flag.
   * @return The flag.
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    return _hashCode;
  }

  private int generateHashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _dayCount.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _spotLag;
    result = prime * result + _tenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    final IborIndex other = (IborIndex) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
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
    if (!ObjectUtils.equals(_tenor, other._tenor)) {
      return false;
    }
    return true;
  }

}
