/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;

/**
 * Class describing a price index, like the one used in inflation instruments.
 */
public class IndexPrice {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The currency in which the index is computed.
   */
  private final Currency _currency;

  /**
   * Constructor of the price index.
   * @param name The index name. Not null.
   * @param ccy The currency in which the index is computed. Not null.
   */
  public IndexPrice(final String name, final Currency ccy) {
    Validate.notNull(name, "Name");
    Validate.notNull(ccy, "Currency");
    _name = name;
    _currency = ccy;
  }

  /**
   * Gets the name of the price index.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the currency in which the index is computed.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _name.hashCode();

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
    final IndexPrice other = (IndexPrice) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
