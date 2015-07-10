/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapIborONDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Generator (or template) for OIS.
 */
public class GeneratorSwapIborON extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The Ibor index.
   */
  private final IborIndex _indexIbor;
  /**
   * The ON index.
   */
  private final IndexON _indexON;
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
   * The holiday calendar associated with the ibor index.
   */
  private final Calendar _iborCalendar;
  /**
   * The holiday calendar associated with the overnight index.
   */
  private final Calendar _overnightCalendar;

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param indexIbor The Ibor index. Not null.
   * @param indexON The ON index. Not null.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param iborCalendar The holiday calendar for the ibor index.
   * @param overnightCalendar The holiday calendar for the overnight index.
   */
  public GeneratorSwapIborON(final String name, final IborIndex indexIbor, final IndexON indexON, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final int spotLag, final Calendar iborCalendar, final Calendar overnightCalendar) {
    super(name);
    ArgumentChecker.notNull(indexIbor, "Index Ibor");
    ArgumentChecker.notNull(indexON, "Index ON");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(iborCalendar, "ibor calendar");
    ArgumentChecker.notNull(overnightCalendar, "overnight calendar");
    _indexIbor = indexIbor;
    _indexON = indexON;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = spotLag;
    _stubShort = true;
    _fromEnd = true;
    _iborCalendar = iborCalendar;
    _overnightCalendar = overnightCalendar;
  }

  /**
   * Constructor from all details. The stub is short and date constructed from the end.
   * @param name The generator name.
   * @param indexIbor The Ibor index. Not null.
   * @param indexON The ON index. Not null.
   * @param businessDayConvention The business day convention for the payments (used for both legs).
   * @param endOfMonth The flag indicating if the end-of-month rule is used (used for both legs).
   * @param spotLag The index spot lag in days between trade and settlement date (usually 2 or 0).
   * @param paymentLag The lag in days between the last ON fixing date and the coupon payment.
   * @param iborCalendar The holiday calendar for the ibor index.
   * @param overnightCalendar The holiday calendar for the overnight index.
   */
  public GeneratorSwapIborON(final String name, final IborIndex indexIbor, final IndexON indexON, final BusinessDayConvention businessDayConvention,
      final boolean endOfMonth, final int spotLag, final int paymentLag, final Calendar iborCalendar, final Calendar overnightCalendar) {
    super(name);
    ArgumentChecker.notNull(indexIbor, "Index Ibor");
    ArgumentChecker.notNull(indexON, "Index ON");
    ArgumentChecker.notNull(businessDayConvention, "Business day convention");
    ArgumentChecker.notNull(iborCalendar, "ibor calendar");
    ArgumentChecker.notNull(overnightCalendar, "overnight calendar");
    _indexIbor = indexIbor;
    _indexON = indexON;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _paymentLag = paymentLag;
    _stubShort = true;
    _fromEnd = true;
    _iborCalendar = iborCalendar;
    _overnightCalendar = overnightCalendar;
  }

  /**
   * Gets the Ibor index.
   * @return The index.
   */
  public IborIndex getIndexIbor() {
    return _indexIbor;
  }

  /**
   * Gets the ON index.
   * @return The index.
   */
  public IndexON getIndexON() {
    return _indexON;
  }

  /**
   * Gets the business day convention for the payments (used for both legs).
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
   * Gets the calendar associated to the Ibor index.
   * @return The calendar.
   */
  public Calendar getIborCalendar() {
    return _iborCalendar;
  }

  /**
   * Gets the calendar associated to the overnight index.
   * @return The calendar.
   */
  public Calendar getOvernightCalendar() {
    return _overnightCalendar;
  }

  @Override
  public SwapIborONDefinition generateInstrument(final ZonedDateTime date, final double spread, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _iborCalendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _indexIbor, _iborCalendar);
    return SwapIborONDefinition.from(startDate, attribute.getEndPeriod(), this, notional, -spread, true, _iborCalendar);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_businessDayConvention == null) ? 0 : _businessDayConvention.hashCode());
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + (_fromEnd ? 1231 : 1237);
    result = prime * result + _indexIbor.hashCode();
    result = prime * result + _indexON.hashCode();
    result = prime * result + _paymentLag;
    result = prime * result + _spotLag;
    result = prime * result + (_stubShort ? 1231 : 1237);
    result = prime * result + _iborCalendar.hashCode();
    result = prime * result + _overnightCalendar.hashCode();
    result = prime * result + _indexON.hashCode();
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
    final GeneratorSwapIborON other = (GeneratorSwapIborON) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (_fromEnd != other._fromEnd) {
      return false;
    }
    if (!ObjectUtils.equals(_indexIbor, other._indexIbor)) {
      return false;
    }
    if (!ObjectUtils.equals(_indexON, other._indexON)) {
      return false;
    }
    if (_paymentLag != other._paymentLag) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    if (_stubShort != other._stubShort) {
      return false;
    }
    if (!ObjectUtils.equals(_iborCalendar, other._iborCalendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_overnightCalendar, other._overnightCalendar)) {
      return false;
    }
    return true;
  }

}
