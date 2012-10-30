/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.PublicSPI;

/**
 * A snapshot of a market data point taken at a particular instant, potentially altered by hand.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
public class ValueSnapshot implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The value sampled from the market.
   */
  private final Double _marketValue;
  /**
   * The value entered by the user, null if not overridden.
   */
  private Double _overrideValue;

  /**
   * Creates an instance with the real value and optional override.
   * 
   * @param marketValue  the real market value
   * @param overrideValue  the override, null if no override
   */
  public ValueSnapshot(Double marketValue, Double overrideValue) {
    super();
    _marketValue = marketValue;
    _overrideValue = overrideValue;
  }

  /**
   * Creates an instance with the real value and no override.
   * 
   * @param marketValue  the real market value
   */
  public ValueSnapshot(Double marketValue) {
    this(marketValue, null);
  }

  
  //-------------------------------------------------------------------------
  /**
   * Gets the real market value.
   * 
   * @return the value sampled from the market
   */
  public Double getMarketValue() {
    return _marketValue;
  }

  /**
   * Gets the override value.
   * 
   * @return the override value, null if not overridden
   */
  public Double getOverrideValue() {
    return _overrideValue;
  }

  /**
   * Sets the override value.
   * 
   * @param overrideValue  the override value to set, null to clear the override
   */
  public void setOverrideValue(Double overrideValue) {
    _overrideValue = overrideValue;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this snapshot equals another.
   * <p>
   * This checks the real and override values.
   * 
   * @param object  the object to compare to, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof ValueSnapshot) {
      ValueSnapshot other = (ValueSnapshot) object;
      return ObjectUtils.equals(getMarketValue(), other.getMarketValue()) &&
              ObjectUtils.equals(getOverrideValue(), other.getOverrideValue());
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
    return ObjectUtils.hashCode(getMarketValue()) ^ ObjectUtils.hashCode(getOverrideValue());
  }

  // TODO: externalize the Fudge representation to a builder

  /**
   * Creates a Fudge representation of the snapshot value:
   * <pre>
   *   message {
   *     optional double marketValue;
   *     optional double overrideValue;
   *   }
   * </pre>
   * 
   * @param serializer the Fudge serialization context, not null
   * @return the message representation
   */
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    if (getMarketValue() != null) {
      msg.add("marketValue", null, FudgeWireType.DOUBLE, getMarketValue().doubleValue());
    }
    if (getOverrideValue() != null) {
      msg.add("overrideValue", null, FudgeWireType.DOUBLE, getOverrideValue().doubleValue());
    }
    return msg;
  }

  /**
   * Creates a snapshot value object from a Fudge message representation. See {@link #toFudgeMsg}
   * for the message format.
   * 
   * @param deserializer the Fudge deserialization context, not null
   * @param msg message containing the value representation, not null
   * @return a snapshot object
   */
  public static ValueSnapshot fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double marketValue = msg.getDouble("marketValue");
    Double overrideValue = msg.getDouble("overrideValue");
    return new ValueSnapshot(marketValue, overrideValue);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
  

}
