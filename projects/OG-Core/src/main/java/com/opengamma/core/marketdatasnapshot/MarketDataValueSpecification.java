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
import com.opengamma.util.ArgumentChecker;

/**
 * An immutable specification of an individual piece of unstructured market data.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class MarketDataValueSpecification {
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
   * @param type  the type of market data this refers to, not null
   * @param uniqueId  the unique identifier of the data this refers to, not null
   */
  public MarketDataValueSpecification(MarketDataValueType type, UniqueId uniqueId) {
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
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

  /**
   * Creates a Fudge representation of the value specification:
   * <pre>
   *   message {
   *     string type;
   *     UniqueId uniqueId;
   *   }
   * </pre>
   * 
   * @param serializer Fudge serialization context, not null
   * @return the message representation of this value specification
   */
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("type", null, _type.name());
    serializer.addToMessage(msg, "uniqueId", null, _uniqueId);
    return msg;
  }

  public static MarketDataValueSpecification fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    MarketDataValueType type = deserializer.fieldValueToObject(MarketDataValueType.class, msg.getByName("type"));
    UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, msg.getByName("uniqueId"));
    return new MarketDataValueSpecification(type, uniqueId);
  }

}
