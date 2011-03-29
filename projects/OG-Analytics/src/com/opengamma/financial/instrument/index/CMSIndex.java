/**
 * Copyright (C) 2011 - present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import javax.time.calendar.Period;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.money.Currency;

/**
 * Class describing a CMS index.
 */
public class CMSIndex {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The swap generator associated to the CMS index.
   */
  private final SwapGenerator _swapGenerator;
  /**
   * The tenor of the CMS index.
   */
  private final Period _tenor;

  /**
   * Constructor from all the CMS index details.
   * @param fixedLegPeriod The fixed leg payment period.
   * @param fixedLegDayCount The day count convention associated to the fixed leg.
   * @param iborIndex The Ibor index of the floating leg.
   * @param tenor The CMS tenor.
   */
  public CMSIndex(Period fixedLegPeriod, DayCount fixedLegDayCount, IborIndex iborIndex, Period tenor) {
    Validate.notNull(tenor, "tenor");
    _swapGenerator = new SwapGenerator(fixedLegPeriod, fixedLegDayCount, iborIndex);
    _tenor = tenor;
    _name = tenor.toString() + _swapGenerator.getName();
  }

  /**
   * Constructor from a swap generator and the CMS tenor.
   * @param swapGenerator The underlying swap generator.
   * @param tenor The CMS tenor.
   */
  public CMSIndex(SwapGenerator swapGenerator, Period tenor) {
    Validate.notNull(swapGenerator, "swap generator");
    Validate.notNull(tenor, "tenor");
    _swapGenerator = swapGenerator;
    _tenor = tenor;
    _name = tenor.toString() + _swapGenerator.getName();
  }

  /**
   * Gets the  index name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the _fixedLegPeriod field.
   * @return the _fixedLegPeriod
   */
  public Period getFixedLegPeriod() {
    return _swapGenerator.getFixedLegPeriod();
  }

  /**
   * Gets the _fixedLegDayCount field.
   * @return the _fixedLegDayCount
   */
  public DayCount getFixedLegDayCount() {
    return _swapGenerator.getFixedLegDayCount();
  }

  /**
   * Gets the _iborIndex field.
   * @return the _iborIndex
   */
  public IborIndex getIborIndex() {
    return _swapGenerator.getIborIndex();
  }

  /**
   * Gets the _tenor field.
   * @return the _tenor
   */
  public Period getTenor() {
    return _tenor;
  }

  /**
   * Gets the index currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _swapGenerator.getIborIndex().getCurrency();
  }

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _name.hashCode();
    result = prime * result + _swapGenerator.hashCode();
    result = prime * result + _tenor.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CMSIndex other = (CMSIndex) obj;
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_swapGenerator, other._swapGenerator)) {
      return false;
    }
    if (!ObjectUtils.equals(_tenor, other._tenor)) {
      return false;
    }
    return true;
  }

}
