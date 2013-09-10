/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationYearOnYearDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class with the description of swap inflation year on year characteristics.
 */
public class GeneratorSwapFixedInflationYearOnYear extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The fixed leg period of payments.
   */
  private final Period _fixedLegPeriod;
  /**
   * The fixed leg day count.
   */
  private final DayCount _fixedLegDayCount;
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
   * The price index fixing lag in months(usually 3).
   */
  private final int _monthLag;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;
  /**
   * The flag indicating if the inflation year on year coupons are paying the notional (TRUE) or not (FALSE).
   */
  private final boolean _payNotional;
  /**
   * The flag indicating if price index is interpolated linearly (TRUE) or piecewise constant (FALSE).
   */
  private final boolean _isLinear;

  /**
   * Constructor from all the details.
   * @param name The generator name. Not null.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param indexPrice The Price index..
   * @param businessDayConvention The business day convention associated to fix leg.
   * @param calendar  The calendar used to compute the payment date.
   * @param endOfMonth The end-of-month flag.
   * @param monthLag The price index fixing lag in months(usually 3).
   * @param spotLag Lag between today and the spot date.
   * @param payNotional  The flag indicating if the inflation year on year coupons are paying the notional (TRUE) or not (FALSE).
   * @param isLinear The flag indicating if price index is interpolated linearly (TRUE) or piecewise constant (FALSE).
   */
  public GeneratorSwapFixedInflationYearOnYear(final String name, final Period fixedLegPeriod, final DayCount fixedLegDayCount, final IndexPrice indexPrice,
      final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean endOfMonth,
      final int monthLag, final int spotLag, final boolean payNotional, final boolean isLinear) {
    super(name);
    ArgumentChecker.notNull(fixedLegPeriod, "fixed leg period");
    ArgumentChecker.notNull(fixedLegDayCount, "fixed leg day count");
    ArgumentChecker.notNull(indexPrice, "index price");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(businessDayConvention, "businessDayConvention");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _indexPrice = indexPrice;
    _businessDayConvention = businessDayConvention;
    _calendar = calendar;
    _endOfMonth = endOfMonth;
    _monthLag = monthLag;
    _spotLag = spotLag;
    _payNotional = payNotional;
    _isLinear = isLinear;
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
   * Gets the _payNotional field.
   * @return the _payNotional
   */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the _isLinear field.
   * @return the _isLinear
   */
  public boolean isLinear() {
    return _isLinear;
  }

  /**
   * {@inheritDoc}
   * The effective date is spot+startTenor. The maturity date is effective date + endTenor
   */
  @Override
  public SwapFixedInflationYearOnYearDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), this.getCalendar());
    if (this._isLinear) {
      return SwapFixedInflationYearOnYearDefinition.fromGeneratorInterpolation(startDate, rate, notional, attribute.getEndPeriod(), this, true);
    }
    return SwapFixedInflationYearOnYearDefinition.fromGeneratorMonthly(startDate, rate, notional, attribute.getEndPeriod(), this, true);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_businessDayConvention == null) ? 0 : _businessDayConvention.hashCode());
    result = prime * result + ((_calendar == null) ? 0 : _calendar.hashCode());
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _fixedLegDayCount.hashCode();
    result = prime * result + _fixedLegPeriod.hashCode();
    result = prime * result + ((_indexPrice == null) ? 0 : _indexPrice.hashCode());
    result = prime * result + (_isLinear ? 1231 : 1237);
    result = prime * result + (_payNotional ? 1231 : 1237);
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
    final GeneratorSwapFixedInflationYearOnYear other = (GeneratorSwapFixedInflationYearOnYear) obj;
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
    if (!ObjectUtils.equals(_fixedLegDayCount, other._fixedLegDayCount)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixedLegPeriod, other._fixedLegPeriod)) {
      return false;
    }
    if (_indexPrice == null) {
      if (other._indexPrice != null) {
        return false;
      }
    } else if (!_indexPrice.equals(other._indexPrice)) {
      return false;
    }
    if (_payNotional != other._payNotional) {
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
