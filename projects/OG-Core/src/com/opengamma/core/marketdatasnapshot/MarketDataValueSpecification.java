package com.opengamma.core.marketdatasnapshot;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.id.UniqueIdentifier;

/**
 * An immutable specification of an individual piece of (unstructured) market data
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
   * @param type the type of market data this refers to 
   * @param uniqueId the UID of the data this refers to
   */
  public MarketDataValueSpecification(MarketDataValueType type, UniqueIdentifier uniqueId) {
    super();
    _type = type;
    _uniqueId = uniqueId;
  }

  /**
   * Gets the type field.
   * @return the type
   */
  public MarketDataValueType getType() {
    return _type;
  }

  /**
   * Gets the uniqueId field.
   * @return the uniqueId
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {

    final MutableFudgeMsg msg = context.newMessage();
    msg.add("type", null, context.objectToFudgeMsg(_type));
    msg.add("uniqueId", null, context.objectToFudgeMsg(_uniqueId));
    return msg;
  }

  public static MarketDataValueSpecification fromFudgeMsg(final FudgeDeserializationContext context,
      final FudgeMsg msg) {
    return new MarketDataValueSpecification(
        context.fieldValueToObject(MarketDataValueType.class, msg.getByName("type")), context.fieldValueToObject(
            UniqueIdentifier.class, msg.getByName("uniqueId")));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_type == null) ? 0 : _type.hashCode());
    result = prime * result + ((_uniqueId == null) ? 0 : _uniqueId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MarketDataValueSpecification other = (MarketDataValueSpecification) obj;
    if (_type != other._type)
      return false;
    if (_uniqueId == null) {
      if (other._uniqueId != null)
        return false;
    } else if (!_uniqueId.equals(other._uniqueId))
      return false;
    return true;
  }

  
}
