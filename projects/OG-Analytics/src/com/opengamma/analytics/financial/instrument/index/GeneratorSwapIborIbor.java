/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.swap.SwapIborIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.util.ArgumentChecker;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorSwapIborIbor extends GeneratorInstrument {

  /**
   * The Ibor index of the first leg.
   */
  private final IborIndex _iborIndex1;
  /**
   * The Ibor index of the second leg. The two index should be related to the same currency.
   */
  private final IborIndex _iborIndex2;
  /**
   * The business day convention associated to the swap.
   */
  private final BusinessDayConvention _businessDayConvention;
  /**
   * The flag indicating if the end-of-month rule is used for the swap.
   */
  private final boolean _endOfMonth;
  /**
   * The index spot lag in days between trade and settlement date (usually 2 or 0).
   */
  private final int _spotLag;

  // REVIEW: Do we need stubShort and stubFirst flags?

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the first Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex1 The Ibor index of the first leg.
   * @param iborIndex2 The Ibor index of the second leg.
   */
  public GeneratorSwapIborIbor(final String name, final IborIndex iborIndex1, final IborIndex iborIndex2) {
    super(name);
    ArgumentChecker.notNull(iborIndex1, "ibor index 1");
    ArgumentChecker.notNull(iborIndex2, "ibor index 2");
    ArgumentChecker.isTrue(iborIndex1.getCurrency() == iborIndex2.getCurrency(), "Currencies of both index should be identical");
    _iborIndex1 = iborIndex1;
    _iborIndex2 = iborIndex2;
    _businessDayConvention = iborIndex1.getBusinessDayConvention();
    _endOfMonth = iborIndex1.isEndOfMonth();
    _spotLag = iborIndex1.getSpotLag();
  }

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex1 The Ibor index of the first leg.
   * @param iborIndex2 The Ibor index of the second leg.
   * @param businessDayConvention The business day convention associated to the index.
   * @param endOfMonth The end-of-month flag.
   * @param spotLag The swap spot lag (usually 2 or 0).
   */
  public GeneratorSwapIborIbor(final String name, final IborIndex iborIndex1, final IborIndex iborIndex2, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, 
      final int spotLag) {
    super(name);
    ArgumentChecker.notNull(iborIndex1, "ibor index 1");
    ArgumentChecker.notNull(iborIndex2, "ibor index 2");
    ArgumentChecker.isTrue(iborIndex1.getCurrency() == iborIndex2.getCurrency(), "Currencies of both index should be identical");
    _iborIndex1 = iborIndex1;
    _iborIndex2 = iborIndex2;
    _businessDayConvention = businessDayConvention;
    _endOfMonth = endOfMonth;
    _spotLag = spotLag;
  }

  /**
   * Gets the Ibor index of the first leg.
   * @return The index.
   */
  public IborIndex getIborIndex1() {
    return _iborIndex1;
  }

  /**
   * Gets the Ibor index of the second leg.
   * @return The index.
   */
  public IborIndex getIborIndex2() {
    return _iborIndex2;
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

  @Override
  /**
   * The effective date is date+_spotLag. The maturity date is effective date+tenor.
   */
  public SwapIborIborDefinition generateInstrument(ZonedDateTime date, Period tenor, double spread, double notional, Object... objects) {
    ArgumentChecker.notNull(date, "Reference date");
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, _spotLag, _iborIndex1.getCalendar());
    return SwapIborIborDefinition.from(startDate, tenor, this, notional, spread, true);
  }

  @Override
  /**
   * The effective date is spot+startTenor. The maturity date is effective date + endTenor
   */
  public SwapIborIborDefinition generateInstrument(final ZonedDateTime date, final Period startTenor, final Period endTenor, double spread, double notional, Object... objects) {
    ArgumentChecker.notNull(date, "Reference date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, _spotLag, _iborIndex1.getCalendar());
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, startTenor, _iborIndex1);
    return SwapIborIborDefinition.from(startDate, endTenor, this, notional, spread, true);
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
    result = prime * result + (_endOfMonth ? 1231 : 1237);
    result = prime * result + _iborIndex1.hashCode();
    result = prime * result + _iborIndex2.hashCode();
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
    GeneratorSwapIborIbor other = (GeneratorSwapIborIbor) obj;
    if (!ObjectUtils.equals(_businessDayConvention, other._businessDayConvention)) {
      return false;
    }
    if (_endOfMonth != other._endOfMonth) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex2, other._iborIndex2)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndex1, other._iborIndex1)) {
      return false;
    }
    if (_spotLag != other._spotLag) {
      return false;
    }
    return true;
  }

}
