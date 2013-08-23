/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedCompoundedONCompoundedDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 *  Generator (or template) for swap ON compounded.
 */
public class GeneratorSwapFixedCompoundedONCompounded extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The ON index of the floating leg.
   */
  private final IndexON _index;

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
   * The holiday calendar associated with the overnight index.
   */
  private final Calendar _calendar;

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param index The ON index of the floating leg.
   * @param fixedLegDayCount The fixed leg day count.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param calendar The calendar associated with the overnight index.
   */
  public GeneratorSwapFixedCompoundedONCompounded(final String name, final IndexON index, final DayCount fixedLegDayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final int spotLag, final Calendar calendar) {
    super(name);
    ArgumentChecker.notNull(fixedLegDayCount, "Fixed leg day count");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(index, "Index ON");
    _index = index;
    _fixedLegDayCount = fixedLegDayCount;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = spotLag;
    _calendar = calendar;
  }

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param index The ON index of the floating leg.
   * @param fixedLegDayCount The fixed leg day count.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param paymentLag The lag in days between the last ON fixing date and the coupon payment.
   * @param calendar The calendar associated with the overnight index.
   */
  public GeneratorSwapFixedCompoundedONCompounded(final String name, final IndexON index, final DayCount fixedLegDayCount, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final int spotLag, final int paymentLag, final Calendar calendar) {
    super(name);
    ArgumentChecker.notNull(fixedLegDayCount, "Fixed leg day count");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(index, "Index ON");
    _index = index;
    _fixedLegDayCount = fixedLegDayCount;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = paymentLag;
    _calendar = calendar;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_businessDayConvention == null) ? 0 : _businessDayConvention.hashCode());
    result = prime * result + ((_calendar == null) ? 0 : _calendar.hashCode());
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + ((_fixedLegDayCount == null) ? 0 : _fixedLegDayCount.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + _paymentLag;
    result = prime * result + _spotLag;
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GeneratorSwapFixedCompoundedONCompounded other = (GeneratorSwapFixedCompoundedONCompounded) obj;
    if (_businessDayConvention == null) {
      if (other._businessDayConvention != null) {
        return false;
      }
    } else if (!_businessDayConvention.equals(other._businessDayConvention)) {
      return false;
    }
    if (_calendar == null) {
      if (other._calendar != null) {
        return false;
      }
    } else if (!_calendar.equals(other._calendar)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_fixedLegDayCount == null) {
      if (other._fixedLegDayCount != null) {
        return false;
      }
    } else if (!_fixedLegDayCount.equals(other._fixedLegDayCount)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (_paymentLag != other._paymentLag) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    return true;
  }

  /**
   * Gets the calendar associated to the OIS index.
   * @return The calendar.
   */
  public Calendar getOvernightCalendar() {
    return _calendar;
  }

  /**
   * {@inheritDoc}
   * The effective date is date+_spotLag. The end of fixing period is effective date+tenor.
   */
  @Override
  public SwapFixedCompoundedONCompoundedDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _businessDayConvention, _calendar, _endOfMonth);
    return SwapFixedCompoundedONCompoundedDefinition.from(startDate, attribute.getEndPeriod(), notional, this, rate, true);
  }

}
