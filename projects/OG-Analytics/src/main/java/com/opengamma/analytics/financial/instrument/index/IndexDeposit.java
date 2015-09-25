/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an deposit-like index (in particular Ibor and OIS).
 */
public abstract class IndexDeposit implements Serializable {

  /**
   * The name of the index. Not null.
   */
  private final String _name;
  /**
   * The index currency. Not null.
   */
  private final Currency _currency;

  /**
   * Constructor.
   * @param name The index name.
   * @param currency The underlying currency.
   */
  public IndexDeposit(final String name, final Currency currency) {
    ArgumentChecker.notNull(name, "Index: name");
    ArgumentChecker.notNull(currency, "Index: currency");
    _name = name;
    _currency = currency;
  }

  /**
   * Gets the name of the index.
   * @return The name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the index currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public String toString() {
    return _name + "-" + _currency.toString();
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
    final IndexDeposit other = (IndexDeposit) obj;
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    return true;
  }

}
