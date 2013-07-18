/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.money.Currency;

/**
 * A key used to identify a yield curve.
 * <p>
 * This class is immutable and thread-safe.
 */
public class YieldCurveKey extends StructuredMarketDataKey implements Comparable<YieldCurveKey> {

  /** Serialization version. */
  private static final long serialVersionUID = 2L;

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
  public YieldCurveKey(Currency currency, String name) {
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
  public int compareTo(YieldCurveKey other) {
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
    if (object instanceof YieldCurveKey) {
      YieldCurveKey other = (YieldCurveKey) object;
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

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitYieldCurveKey(this);
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("currency", _currency.getCode());
    msg.add("name", _name);
    return msg;
  }

  public static YieldCurveKey fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new YieldCurveKey(Currency.of(msg.getString("currency")), msg.getString("name"));
  }

}
