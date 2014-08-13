/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.cash.DepositIborDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * Class with the description of Ibor deposit.
 */
public class GeneratorDepositIbor extends GeneratorInstrument<GeneratorAttributeIR> {

  /**
   * The index. Not null.
   */
  private final IborIndex _index;
  /**
   * The holiday calendar associated with this index.
   */
  private final Calendar _calendar;

  /**
   * Constructor.
   * @param name The generator name.
   * @param index The index.
   * @param calendar The holiday calendar for the ibor leg.
   */
  public GeneratorDepositIbor(final String name, final IborIndex index, final Calendar calendar) {
    super(name);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    _index = index;
    _calendar = calendar;
  }

  /**
   * Gets the index.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  @Override
  public DepositIborDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttributeIR attribute) {
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(date, _index.getSpotLag(), _calendar);
    final ZonedDateTime endDate;
    if (attribute.getEndPeriod().equals(Period.ZERO)) {
      endDate = ScheduleCalculator.getAdjustedDate(startDate, _index, _calendar);
    } else {
      endDate = ScheduleCalculator.getAdjustedDate(startDate, attribute.getEndPeriod(), _index, _calendar);
    }
    final double accrualFactor = _index.getDayCount().getDayCountFraction(startDate, endDate, _calendar);
    return new DepositIborDefinition(_index.getCurrency(), startDate, endDate, notional, marketQuote, accrualFactor, _index);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _index.hashCode();
    result = prime * result + _calendar.hashCode();
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
    final GeneratorDepositIbor other = (GeneratorDepositIbor) obj;
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    return true;
  }

}
