/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.money.Currency;

/**
 * Class with the description of swap characteristics.
 */
public class GeneratorFRA extends Generator {

  /**
   * The Ibor index underlying the FRA.
   */
  private final IborIndex _iborIndex;

  /**
   * Constructor from the details. The business day conventions, end-of-month and spot lag are from the Ibor index.
   * @param name The generator name. Not null.
   * @param iborIndex The Ibor index of the floating leg.
   */
  public GeneratorFRA(String name, IborIndex iborIndex) {
    super(name);
    Validate.notNull(iborIndex, "ibor index");
    _iborIndex = iborIndex;
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
    return _iborIndex.getCalendar();
  }

  @Override
  public InstrumentDefinition<?> generateInstrument(ZonedDateTime date, Period tenor, double rate, double notional, Object... objects) {
    Period startPeriod = tenor.minus(_iborIndex.getTenor());
    return ForwardRateAgreementDefinition.fromTrade(date, startPeriod, notional, _iborIndex, rate);
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_iborIndex == null) ? 0 : _iborIndex.hashCode());
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
    GeneratorFRA other = (GeneratorFRA) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
      return false;
    }
    return true;
  }

}
