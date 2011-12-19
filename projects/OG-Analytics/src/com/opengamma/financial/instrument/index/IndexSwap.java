/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
 * Class describing a swap index.
 */
public class IndexSwap {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The swap generator associated to the swap index.
   */
  private final SwapGenerator _swapGenerator;
  /**
   * The tenor of the swap index. Usually a integer number of years (1Y, 5Y, 10Y).
   */
  private final Period _tenor;

  /**
   * Constructor from all the Swap index details.
   * @param fixedLegPeriod The swap fixed leg payment period.
   * @param fixedLegDayCount The swap fixed leg day count convention.
   * @param iborIndex The Ibor index of swap the floating leg.
   * @param tenor The swap tenor.
   */
  public IndexSwap(Period fixedLegPeriod, DayCount fixedLegDayCount, IborIndex iborIndex, Period tenor) {
    Validate.notNull(tenor, "Swap tenor");
    _swapGenerator = new SwapGenerator(fixedLegPeriod, fixedLegDayCount, iborIndex);
    _tenor = tenor;
    _name = tenor.toString() + _swapGenerator.getName();
  }

  /**
   * Constructor from a swap generator and the swap tenor.
   * @param swapGenerator The underlying swap generator.
   * @param tenor The swap tenor.
   */
  public IndexSwap(SwapGenerator swapGenerator, Period tenor) {
    Validate.notNull(swapGenerator, "swap generator");
    Validate.notNull(tenor, "tenor");
    _swapGenerator = swapGenerator;
    _tenor = tenor;
    _name = tenor.toString() + _swapGenerator.getName();
  }

  /**
   * Gets the index name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the swap fixed leg payment period.
   * @return The swap fixed leg payment period.
   */
  public Period getFixedLegPeriod() {
    return _swapGenerator.getFixedLegPeriod();
  }

  /**
   * Gets the swap fixed leg day count convention.
   * @return The swap fixed leg day count convention.
   */
  public DayCount getFixedLegDayCount() {
    return _swapGenerator.getFixedLegDayCount();
  }

  /**
   * Gets the Ibor index of swap the floating leg.
   * @return The Ibor index of swap the floating leg.
   */
  public IborIndex getIborIndex() {
    return _swapGenerator.getIborIndex();
  }

  /**
   * Gets the tenor of the swap index.
   * @return The tenor.
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
    IndexSwap other = (IndexSwap) obj;
    if (!ObjectUtils.equals(_swapGenerator, other._swapGenerator)) {
      return false;
    }
    if (!ObjectUtils.equals(_tenor, other._tenor)) {
      return false;
    }
    return true;
  }

}
