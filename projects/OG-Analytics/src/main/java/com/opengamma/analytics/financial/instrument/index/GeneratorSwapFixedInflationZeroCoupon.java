/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Class with the description of swap inflation zero coupon characteristics.
 */
public class GeneratorSwapFixedInflationZeroCoupon extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The Price index.
   */
  private final IndexPrice _indexPrice;
  /**
   * The business day convention associated to fix leg.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The calendar used to compute the payment date.
   */
  private final Calendar _calendar;
  /**
   * The flag indicating if the end-of-month rule is used.
   */
  private final boolean _endOfMonth;
  /**
   * The price index fixing lag in months (usually 3).
   */
  private final int _monthLag;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;
  /**
   * The flag indicating if price index is interpolated linearly (TRUE) or piecewise constant (FALSE).
   */
  private final boolean _isLinear;

  /**
   * Constructor from all the details.
   * @param name The generator name. Not null.
   * @param indexPrice The Price index. Not null.
   * @param businessDayConvention The business day convention associated to fix leg. Not null.
   * @param calendar  The calendar used to compute the payment date. Not null.
   * @param endOfMonth The end-of-month flag.
   * @param monthLag The price index fixing lag in months(usually 3).
   * @param spotLag Lag between today and the spot date.
   * @param isLinear True if the price index is interpolated linearly.
   */
  public GeneratorSwapFixedInflationZeroCoupon(final String name, final IndexPrice indexPrice, final BusinessDayConvention businessDayConvention,
      final Calendar calendar, final boolean endOfMonth, final int monthLag, final int spotLag, final boolean isLinear) {
    super(name);
    ArgumentChecker.notNull(indexPrice, "index price");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
    ArgumentChecker.notNull(calendar, "calendar");
    _indexPrice = indexPrice;
    _businessDayConvention = businessDayConvention;
    _calendar = calendar;
    _endOfMonth = endOfMonth;
    _monthLag = monthLag;
    _spotLag = spotLag;
    _isLinear = isLinear;
  }

  /**
   * Gets the _indexPrice field.
   * @return the _indexPrice
   */
  public IndexPrice getIndexPrice() {
    return _indexPrice;
  }

  /**
   * Gets the _businessDayConvention field.
   * @return the _businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }

  /**
   * Gets the _calendar field.
   * @return the _calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Gets the _endOfMonth field.
   * @return the _endOfMonth
   */
  public boolean isEndOfMonth() {
    return _endOfMonth;
  }

  /**
   * Gets the _monthLag field.
   * @return the _monthLag
   */
  public int getMonthLag() {
    return _monthLag;
  }

  /**
   * Gets the swap generator spot lag.
   * @return The lag (in days).
   */
  public int getSpotLag() {
    return _spotLag;
  }

  /**
   * Returns the flag indicating if price index is interpolated linearly (TRUE) or piecewise constant (FALSE).
   * @return The flag.
   */
  public boolean isLinear() {
    return _isLinear;
  }

  /**
   * {@inheritDoc}
   * The effective date is spot+startTenor. The maturity date is effective date + endTenor.
   * The swap pays the fixed rate.
   */
  @Override
  public SwapFixedInflationZeroCouponDefinition generateInstrument(final ZonedDateTime date, final double rate, 
      final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), this.getCalendar());
    if (this._isLinear) {
      return SwapFixedInflationZeroCouponDefinition.fromGeneratorInterpolation(startDate, rate, notional, 
          attribute.getEndPeriod(), this, true);
    }
    return SwapFixedInflationZeroCouponDefinition.fromGeneratorMonthly(startDate, rate, notional, 
        attribute.getEndPeriod(), this, true);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _businessDayConvention.hashCode();
    result = prime * result + _calendar.hashCode();
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _indexPrice.hashCode();
    result = prime * result + (_isLinear ? 1231 : 1237);
    result = prime * result + _monthLag;
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
    final GeneratorSwapFixedInflationZeroCoupon other = (GeneratorSwapFixedInflationZeroCoupon) obj;
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
    if (_indexPrice == null) {
      if (other._indexPrice != null) {
        return false;
      }
    } else if (!_indexPrice.equals(other._indexPrice)) {
      return false;
    }
    if (_isLinear != other._isLinear) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    return true;
  }

}
