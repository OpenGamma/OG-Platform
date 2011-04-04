/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.util.PublicSPI;

/**
 * A snapshot of a market data point taken at a particular time and potentially altered by hand
 */
@PublicSPI
public class ValueSnapshot {
  /**
   * The value sampled from the market
   */
  private final double _marketValue;

  /**
   * The value entered by the user, or null
   */
  private Double _overrideValue;

  public ValueSnapshot(double marketValue, Double overrideValue) {
    super();
    _marketValue = marketValue;
    _overrideValue = overrideValue;
  }

  /**
   * @return The value sampled from the market
   */
  public double getMarketValue() {
    return _marketValue;
  }

  /**
   * @return The value entered by the user, or null
   */
  public Double getOverrideValue() {
    return _overrideValue;
  }

  /**
   * @param overrideValue   the value entered by the user, or null to clear it
   */
  public void setOverrideValue(Double overrideValue) {
    _overrideValue = overrideValue;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializationContext context) {
    
    final MutableFudgeMsg msg = context.newMessage();
    msg.add("marketValue", null, FudgeWireType.DOUBLE, getMarketValue());
    if (getOverrideValue() != null) {
      msg.add("overrideValue", null, FudgeWireType.DOUBLE, getOverrideValue().doubleValue());
    }
    return msg;
  }

  public static ValueSnapshot fromFudgeMsg(final FudgeDeserializationContext context, final FudgeMsg msg) {
    return new ValueSnapshot(msg.getDouble("marketValue"), msg.hasField("overrideValue") ? Double.valueOf(msg
        .getDouble("overrideValue")) : null);
  }
}
