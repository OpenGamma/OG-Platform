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

/**
 * A key used to identify a yield curve.
 * <p>
 * This class is immutable and thread-safe.
 */
public class CurveKey extends StructuredMarketDataKey implements Comparable<CurveKey> {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The curve name.
   */
  private final String _name;

  /**
   * Creates an instance with a name.
   *
   * @param name  the name
   */
  public CurveKey(String name) {
    super();
    _name = name;
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
  public int compareTo(CurveKey other) {
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
    if (object instanceof CurveKey) {
      CurveKey other = (CurveKey) object;
      return ObjectUtils.equals(getName(), other.getName());
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
    return ObjectUtils.hashCode(getName());
  }

  @Override
  public <T> T accept(final Visitor<T> visitor) {
    return visitor.visitCurveKey(this);
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("name", _name);
    return msg;
  }

  public static CurveKey fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new CurveKey(msg.getString("name"));
  }

}
