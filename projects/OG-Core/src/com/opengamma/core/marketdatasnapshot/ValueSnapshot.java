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
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.PublicSPI;

/**
 * A snapshot of a market data point taken at a particular instant, potentially altered by hand.
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
    return Double.valueOf(getMarketValue()).hashCode() ^ ObjectUtils.hashCode(getOverrideValue());
  }

  //-------------------------------------------------------------------------
  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    final MutableFudgeMsg msg = context.newMessage();
    if (getMarketValue() != null) {
      msg.add("marketValue", null, FudgeWireType.DOUBLE, getMarketValue().doubleValue());
    }
    if (getOverrideValue() != null) {
      msg.add("overrideValue", null, FudgeWireType.DOUBLE, getOverrideValue().doubleValue());
    }
    return msg;
  }

  public static ValueSnapshot fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    Double marketValue = msg.getDouble("marketValue");
    Double overrideValue = msg.getDouble("overrideValue");
    return new ValueSnapshot(marketValue, overrideValue);
  }

}
