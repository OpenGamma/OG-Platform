/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.UniqueIdentifier;

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
  private final UniqueIdentifier _uniqueId;

  /**
   * Creates an instance for a type of market data and a unique identifier.
   * 
   * @param type  the type of market data this refers to 
   * @param uniqueId  the unique identifier of the data this refers to
   */
  public MarketDataValueSpecification(MarketDataValueType type, UniqueIdentifier uniqueId) {
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
  public UniqueIdentifier getUniqueId() {
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
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("type", null, context.objectToFudgeMsg(_type));
    msg.add("uniqueId", null, context.objectToFudgeMsg(_uniqueId));
    return msg;
  }

  public static MarketDataValueSpecification fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    return new MarketDataValueSpecification(
        context.fieldValueToObject(MarketDataValueType.class, msg.getByName("type")), context.fieldValueToObject(
            UniqueIdentifier.class, msg.getByName("uniqueId")));
  }

}
