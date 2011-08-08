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

import com.opengamma.id.UniqueId;

/**
 * An immutable specification of an individual piece of unstructured market data.
 */
public class MarketDataValueSpecification {
  //TODO This is a whole lot like LiveDataSpecification, but decoupled.  We may want to unify them

  /**
   * The type of the target.
   */
  private final MarketDataValueType _type;
  /**
   * The identifier of the target.
   */
  private final UniqueId _uniqueId;

  /**
   * Creates an instance for a type of market data and a unique identifier.
   * 
   * @param type  the type of market data this refers to 
   * @param uniqueId  the unique identifier of the data this refers to
   */
  public MarketDataValueSpecification(MarketDataValueType type, UniqueId uniqueId) {
    super();
    _type = type;
    _uniqueId = uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the type of the market data.
   * 
   * @return the type
   */
  public MarketDataValueType getType() {
    return _type;
  }

  /**
   * Gets the unique identifier.
   * 
   * @return the unique identifier
   */
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this specification equals another.
   * <p>
   * This checks the type and unique identifier.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof MarketDataValueSpecification) {
      MarketDataValueSpecification other = (MarketDataValueSpecification) object;
      return ObjectUtils.equals(getType(), other.getType()) &&
              ObjectUtils.equals(getUniqueId(), other.getUniqueId());
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
    return ObjectUtils.hashCode(getType()) ^ ObjectUtils.hashCode(getUniqueId());
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("type", null, serializer.objectToFudgeMsg(_type));
    msg.add("uniqueId", null, serializer.objectToFudgeMsg(_uniqueId));
    return msg;
  }

  public static MarketDataValueSpecification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataValueSpecification(
        deserializer.fieldValueToObject(MarketDataValueType.class, msg.getByName("type")), deserializer.fieldValueToObject(
            UniqueId.class, msg.getByName("uniqueId")));
  }

}
