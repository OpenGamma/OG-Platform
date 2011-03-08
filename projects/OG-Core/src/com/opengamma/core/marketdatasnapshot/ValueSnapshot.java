/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.PrimitiveFieldTypes;

import com.opengamma.id.Identifier;
import com.opengamma.util.PublicSPI;

/**
 * A snapshot of a market data point taken at a particular time and potentially altered by hand
 */
@PublicSPI
public class ValueSnapshot {

  /**
   * The security from which the {@link getMarketValue} was taken
   */
  private final Identifier _security;

  /**
   * The value sampled from the market
   */
  private final double _marketValue;

  /**
   * The value entered by the user, or null
   */
  private Double _overrideValue;

  public ValueSnapshot(double marketValue, Double overrideValue, Identifier security) {
    super();
    _marketValue = marketValue;
    _overrideValue = overrideValue;
    _security = security;
  }

  /**
   * @return The security from which the {@link getMarketValue} was taken
   */
  public Identifier getSecurity() {
    return _security;
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

  public MutableFudgeFieldContainer toFudgeMsg(final FudgeSerializationContext context) {
    
    final MutableFudgeFieldContainer msg = context.objectToFudgeMsg(getSecurity());
    msg.add("marketValue", null, PrimitiveFieldTypes.DOUBLE_TYPE, getMarketValue());
    if (getOverrideValue() != null) {
      msg.add("overrideValue", null, PrimitiveFieldTypes.DOUBLE_TYPE, getOverrideValue().doubleValue());
    }
    return msg;
  }

  public static ValueSnapshot fromFudgeMsg(final FudgeDeserializationContext context, final FudgeFieldContainer msg) {
    return new ValueSnapshot(msg.getDouble("marketValue"),
        msg.hasField("overrideValue") ? Double.valueOf(msg.getDouble("overrideValue")) : null,
        context.fudgeMsgToObject(Identifier.class, msg));
  }
}
