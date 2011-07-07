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
 * A key used to identify a volatility surface.
 */
public class VolatilitySurfaceKey implements StructuredMarketDataKey, Comparable<VolatilitySurfaceKey>, Serializable {

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
   * The instrument type.
   */
  private final String _instrumentType;
  /**
   * @param currency the currency
   * @param name the name
   * @param instrumentType the instrument type
   */
  public VolatilitySurfaceKey(Currency currency, String name, String instrumentType) {
    super();
    _currency = currency;
    _name = name;
    _instrumentType = instrumentType;
  }
  /**
   * Gets the currency field.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }
  /**
   * Gets the name field.
   * @return the name
   */
  public String getName() {
    return _name;
  }
  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public String getInstrumentType() {
    return _instrumentType;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Compares this key to another, by currency then name.
   * 
   * @param other  the other key, not null
   * @return the comparison value
   */
  @Override
  public int compareTo(VolatilitySurfaceKey other) {
    int currCompare = _currency.compareTo(other.getCurrency());
    if (currCompare != 0) {
      return currCompare;
    }
    int nameCompare = _name.compareTo(other.getName());
    if (nameCompare != 0) {
      return nameCompare;
    }

    return _instrumentType.compareTo(other._instrumentType);
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
    if (object instanceof VolatilitySurfaceKey) {
      VolatilitySurfaceKey other = (VolatilitySurfaceKey) object;
      return ObjectUtils.equals(getCurrency(), other.getCurrency()) &&
              ObjectUtils.equals(getName(), other.getName())
              && ObjectUtils.equals(getInstrumentType(), other.getInstrumentType());
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
    return ObjectUtils.hashCode(getCurrency()) ^ ObjectUtils.hashCode(getName()) ^ ObjectUtils.hashCode(getInstrumentType());
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("currency", _currency.getCode());
    msg.add("name", _name);
    msg.add("instrumentType", _instrumentType);
    return msg;
  }

  public static VolatilitySurfaceKey fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    return new VolatilitySurfaceKey(Currency.of(msg.getString("currency")), msg.getString("name"), msg.getString("instrumentType"));
  }

}
