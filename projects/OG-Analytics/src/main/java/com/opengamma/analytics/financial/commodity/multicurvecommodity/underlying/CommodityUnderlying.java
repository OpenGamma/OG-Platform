/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying;

import org.apache.commons.lang.Validate;

import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Class describing a commodity underlying, used in commodity instruments. It is useful to call the curve 
 */
public class CommodityUnderlying {

  /**
   * Identifier of the underlying commodity. For cash settle (or physical settle with cash settle optionality) commodity contract this identifier should be used to 
   */
  private final ExternalId _identifier;

  /**
   * Name of the commodity underlying.
   */
  private final String _name;

  /**
   * The currency in which the index is computed.
   */
  private final Currency _currency;

  /**
   * Constructor of the price index.
   * @param identifier The  underlying identifier. Not null.
   * @param name The index name. Not null.
   * @param ccy The currency in which the underlying is computed. Not null.
   */
  public CommodityUnderlying(final ExternalId identifier, final String name, final Currency ccy) {
    Validate.notNull(identifier, "Identifier");
    Validate.notNull(name, "Name");
    Validate.notNull(ccy, "Currency");
    _identifier = identifier;
    _name = name;
    _currency = ccy;
  }

  /**
   * Gets the identifier of the commodity underlying.
   * @return The name.
   */
  public ExternalId getIdentifier() {
    return _identifier;
  }

  /**
   * Gets the Name of the commodity underlying.
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
    return "CommodityUnderlying [_identifier=" + _identifier + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_currency == null) ? 0 : _currency.hashCode());
    result = prime * result + ((_identifier == null) ? 0 : _identifier.hashCode());
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
    final CommodityUnderlying other = (CommodityUnderlying) obj;
    if (_currency == null) {
      if (other._currency != null) {
        return false;
      }
    } else if (!_currency.equals(other._currency)) {
      return false;
    }
    if (_identifier == null) {
      if (other._identifier != null) {
        return false;
      }
    } else if (!_identifier.equals(other._identifier)) {
      return false;
    }
    return true;
  }

}
