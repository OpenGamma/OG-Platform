/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.money.Currency;

/**
 * A key used to identify a volatility cube.
 */
public class VolatilityCubeKey implements Comparable<VolatilityCubeKey>, Serializable {
  
  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The currency.
   */
  private final Currency _currency;
  /**
   * The curve name.
   */
  private final String _name;

  /**
   * Creates an instance with a currency and name.
   * 
   * @param currency  the currency
   * @param name  the name
   */
  public VolatilityCubeKey(Currency currency, String name) {
    super();
    _currency = currency;
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the currency.
   * 
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares this key to another, by currency then name.
   * 
   * @param other  the other key, not null
   * @return the comparison value
   */
  @Override
  public int compareTo(VolatilityCubeKey other) {
    int currCompare = _currency.compareTo(other.getCurrency());
    if (currCompare != 0) {
      return currCompare;
    }
    return _name.compareTo(other.getName());
  }

  /**
   * Checks if this key equals another.
   * <p>
   * This checks the currency and name.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof VolatilityCubeKey) {
      VolatilityCubeKey other = (VolatilityCubeKey) object;
      return ObjectUtils.equals(getCurrency(), other.getCurrency()) &&
              ObjectUtils.equals(getName(), other.getName());
    }
    return false;
  }

  /**
   * Returns a suitable hash code.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(getCurrency()) ^ ObjectUtils.hashCode(getName());
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("currency", _currency.getCode());
    msg.add("name", _name);
    return msg;
  }

  public static VolatilityCubeKey fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    return new VolatilityCubeKey(Currency.of(msg.getString("currency")), msg.getString("name"));
  }

}
