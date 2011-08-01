/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;

/**
 * Class describing an price index, like the one used in inflation instruments.
 */
public class PriceIndex {

  /**
   * Name of the index.
   */
  private final String _name;
  /**
   * The currency in which the index is computed.
   */
  private final Currency _currency;
  /**
   * The reference region for the price index.
   */
  private final Currency _region;

  // FIXME: to be changed to Region

  /**
   * Constructor of the price index.
   * @param name The index name. Not null.
   * @param ccy The currency in which the index is computed. Not null.
   * @param region The reference region for the price index. Not null.
   */
  public PriceIndex(final String name, final Currency ccy, final Currency region) {
    Validate.notNull(name, "Name");
    Validate.notNull(ccy, "Currency");
    Validate.notNull(region, "Region");
    this._name = name;
    _currency = ccy;
    this._region = region;
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

  /**
   * Gets the region associated to the price index.
   * @return The region.
   */
  public Currency getRegion() {
    return _region;
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
    result = prime * result + _region.hashCode();
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
    PriceIndex other = (PriceIndex) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (!ObjectUtils.equals(_region, other._region)) {
      return false;
    }
    return true;
  }

}
