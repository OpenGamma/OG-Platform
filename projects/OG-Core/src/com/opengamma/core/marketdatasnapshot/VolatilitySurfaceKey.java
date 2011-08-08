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

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * A key used to identify a volatility surface.
 */
public class VolatilitySurfaceKey implements StructuredMarketDataKey, Comparable<VolatilitySurfaceKey>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The target.
   */
  private final UniqueId _target;
  /**
   * The curve name.
   */
  private final String _name;
  /**
   * The instrument type.
   */
  private final String _instrumentType;
  
  /**
   * @param target the target
   * @param name the name
   * @param instrumentType the instrument type
   */
  public VolatilitySurfaceKey(UniqueIdentifiable target, String name, String instrumentType) {
    super();
    _target = target.getUniqueId();
    _name = name;
    _instrumentType = instrumentType;
  }
  /**
   * Gets the target field.
   * @return the target
   */
  public UniqueId getTarget() {
    return _target;
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
    int targCompare = _target.compareTo(other.getTarget());
    if (targCompare != 0) {
      return targCompare;
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
      return ObjectUtils.equals(getTarget(), other.getTarget()) &&
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
    return ObjectUtils.hashCode(getTarget()) ^ ObjectUtils.hashCode(getName()) ^ ObjectUtils.hashCode(getInstrumentType());
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("target", _target.toString());
    msg.add("name", _name);
    msg.add("instrumentType", _instrumentType);
    return msg;
  }

  public static VolatilitySurfaceKey fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    UniqueId targetUid;
    String target = msg.getString("target");
    if (target == null) {
      //Handle old form of snapshot
      Currency curr = Currency.of(msg.getString("currency"));
      targetUid = curr.getUniqueId();
    } else {
      targetUid = UniqueId.parse(target);
    }
    return new VolatilitySurfaceKey(targetUid, msg.getString("name"), msg.getString("instrumentType"));
  }

}
