/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.Validate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorSwapFixedInflation extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The Price index.
   */
  private final IndexPrice _indexPrice;
  /**
   * The time series with the relevant price index values.
   */
  private final DoubleTimeSeries<ZonedDateTime> _priceIndexTimeSeries;
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
   * The flag indicating if price index is interpolated linearly (TRUE) or piecewise constant (FALSE).
   */
  private final boolean _isLinear;

  /**
   * Constructor from all the details. 
   * @param name The generator name. Not null.
   * @param indexPrice The Price index..
   * @param priceIndexTimeSeries price index time series. 
   * @param businessDayConvention The business day convention associated to fix leg.
   * @param calendar  The calendar used to compute the payment date.
   * @param endOfMonth The end-of-month flag.
   * @param monthLag The price index fixing lag in months(usually 3).
   * @param spotLag Lag between today and the spot date. 
   * @param isLinear TODO
   */
  public GeneratorSwapFixedInflation(String name, IndexPrice indexPrice, DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final BusinessDayConvention businessDayConvention, Calendar calendar,
      final boolean endOfMonth,
      int monthLag, int spotLag, boolean isLinear) {
    super(name);
    Validate.notNull(indexPrice, "index price");
    Validate.notNull(calendar, "calendar");
    Validate.notNull(businessDayConvention, "businessDayConvention");
    _indexPrice = indexPrice;
    _priceIndexTimeSeries = priceIndexTimeSeries;
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
   * Gets the priceIndexTimeSeries.
   * @return the priceIndexTimeSeries
   */
  public DoubleTimeSeries<ZonedDateTime> getPriceIndexTimeSeries() {
    return _priceIndexTimeSeries;
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
   * Gets the _isLinear field.
   * @return the _isLinear
   */
  public boolean isLinear() {
    return _isLinear;
  }

  @Override
  /**
   * The effective date is spot+startTenor. The maturity date is effective date + endTenor
   */
  public SwapFixedInflationZeroCouponDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), this.getCalendar());
    if (this._isLinear) {
      return SwapFixedInflationZeroCouponDefinition.fromGeneratorInterpolation(startDate, rate, notional, attribute.getEndPeriod(), this, true, _priceIndexTimeSeries);
    } else {
      return SwapFixedInflationZeroCouponDefinition.fromGeneratorMonthly(startDate, rate, notional, attribute.getEndPeriod(), this, true, _priceIndexTimeSeries);
    }
  }

  @Override
  public String toString() {
    return getName();
  }

}
