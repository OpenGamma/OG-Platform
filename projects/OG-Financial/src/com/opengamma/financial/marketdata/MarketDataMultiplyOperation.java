/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.OverrideOperation;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Market data override operation for multiplying a fixed amount to the underlying (e.g. x1.05 gives a 5% increase).
 */
public class MarketDataMultiplyOperation implements OverrideOperation {

  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataAddOperation.class);

  private final double _amount;

  public MarketDataMultiplyOperation(final double amount) {
    _amount = amount;
  }

  public double getAmount() {
    return _amount;
  }

  @Override
  public Object apply(final ValueRequirement valueRequirement, final Object value) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue() * getAmount();
    } else {
      s_logger.warn("Can't apply multiply operation to {} for {}", value, valueRequirement);
      return value;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MarketDataMultiplyOperation)) {
      return false;
    }
    final MarketDataMultiplyOperation other = (MarketDataMultiplyOperation) o;
    return getAmount() == other.getAmount();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() ^ (int) Double.doubleToLongBits(getAmount());
  }

  private static final String AMOUNT_KEY = "multiply";

  public FudgeMsg toFudgeMsg(final FudgeMsgFactory factory) {
    final MutableFudgeMsg msg = factory.newMessage();
    msg.add(AMOUNT_KEY, getAmount());
    return msg;
  }

  public static MarketDataMultiplyOperation fromFudgeMsg(final FudgeMsg msg) {
    return new MarketDataMultiplyOperation(msg.getDouble(AMOUNT_KEY));
  }

}
