/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorSwapFixedIbor extends GeneratorInstrument<GeneratorAttributeIR> {

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
  /**
   * The holiday calendar associated with the floating ibor index.
   */
  private final Calendar _calendar;
  /**
   * In case the the periods do not fit exactly between start and end date, is the remaining interval shorter (true) or longer (false) than the requested period.
   */
  private final boolean _stubShort;
  /**
   * The dates in the schedule can be computed from the end date (true) or from the start date (false).
   */
  private final boolean _fromEnd;

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * The default for stubShort is true and for fromEnd is false.
   * @param name The generator name. Not null.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   * @param calendar The holiday calendar for the ibor leg.
   */
  public GeneratorSwapFixedIbor(final String name, final Period fixedLegPeriod, final DayCount fixedLegDayCount, final IborIndex iborIndex, final Calendar calendar) {
    super(name);
    ArgumentChecker.notNull(fixedLegPeriod, "fixed leg period");
    ArgumentChecker.notNull(fixedLegDayCount, "fixed leg day count");
    ArgumentChecker.notNull(iborIndex, "ibor index");
    ArgumentChecker.notNull(calendar, "calendar");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _iborIndex = iborIndex;
    _businessDayConvention = iborIndex.getBusinessDayConvention();
    _endOfMonth = iborIndex.isEndOfMonth();
    _spotLag = iborIndex.getSpotLag();
    _calendar = calendar;
    _stubShort = true;
    _fromEnd = false;
  }

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * The default for stubShort is true and for fromEnd is false.
   * @param name The generator name. Not null.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param spotLag The swap spot lag (usually 2 or 0).
   * @param calendar The holiday calendar for the ibor leg.
   */
  public GeneratorSwapFixedIbor(final String name, final Period fixedLegPeriod, final DayCount fixedLegDayCount, final IborIndex iborIndex,
      final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final int spotLag, final Calendar calendar) {
    super(name);
    ArgumentChecker.notNull(fixedLegPeriod, "fixed leg period");
    ArgumentChecker.notNull(fixedLegDayCount, "fixed leg day count");
    ArgumentChecker.notNull(iborIndex, "ibor index");
    ArgumentChecker.notNull(calendar, "calendar");
    _fixedLegPeriod = fixedLegPeriod;
    _fixedLegDayCount = fixedLegDayCount;
    _iborIndex = iborIndex;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
    _calendar = calendar;
    _stubShort = true;
    _fromEnd = false;
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
    return _calendar;
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

  /**
   * Returns the stubShort flag.
   * @return The flag
   */
  public boolean isStubShort() {
    return _stubShort;
  }

  /**
   * Returns the fromEnd flag.
   * @return The flag
   */
  public boolean isFromEnd() {
    return _fromEnd;
  }

  /**
   * {@inheritDoc}
   * The effective date is spot+startTenor. The maturity date is effective date + endTenor
   */
  @Override
  public SwapFixedIborDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional, final GeneratorAttributeIR attribute) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _iborIndex, _calendar);
    return SwapFixedIborDefinition.from(startDate, attribute.getEndPeriod(), this, notional, rate, true);
  }

  /**
   * Generate fixed-for-ibor swap definition
   * @param date The reference date
   * @param rate The fixed rate
   * @param notional The notional
   * @param attribute The instrument attributes, as given by a GeneratorAttribute.
   * @param isFixedPayer True if fixed rate payer
   * @return Fixed-for-ibor swap definition 
   */
  public SwapFixedIborDefinition generateInstrument(final ZonedDateTime date, final double rate, final double notional,
      final GeneratorAttributeIR attribute, final boolean isFixedPayer) {
    ArgumentChecker.notNull(date, "Reference date");
    ArgumentChecker.notNull(attribute, "Attributes");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _calendar);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, attribute.getStartPeriod(), _iborIndex,
        _calendar);
    return SwapFixedIborDefinition.from(startDate, attribute.getEndPeriod(), this, notional, rate, isFixedPayer);
  }

  @Override
  public String toString() {
    return getName();
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
    result = prime * result + getName().hashCode();
    result = prime * result + _spotLag;
    result = prime * result + _calendar.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final GeneratorSwapFixedIbor other = (GeneratorSwapFixedIbor) obj;
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
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}
