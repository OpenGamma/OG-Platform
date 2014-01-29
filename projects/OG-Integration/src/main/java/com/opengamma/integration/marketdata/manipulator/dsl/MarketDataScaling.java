/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Manipulator that scales a single market data value.
 * TODO should be consistent with curve and spot rate scaling - add 1 to scaling factor
 */
public class MarketDataScaling implements StructureManipulator<Double> {

  /** Field name for Fudge message. */
  private static final String SCALING_FACTOR = "scalingFactor";

  /** Scaling factor applied to the market data value. */
  private final double _scalingFactor;

  /* package */ MarketDataScaling(double scalingFactor) {
    if (Double.isInfinite(scalingFactor) || Double.isNaN(scalingFactor)) {
      throw new IllegalArgumentException("scalingFactor must not be infinite or NaN. value=" + scalingFactor);
    }
    _scalingFactor = scalingFactor;
  }

  @Override
  public Double execute(Double structure,
                        ValueSpecification valueSpecification,
                        FunctionExecutionContext executionContext) {
    return structure * _scalingFactor;
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SCALING_FACTOR, null, _scalingFactor);
    return msg;
  }

  public static MarketDataScaling fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double scalingFactor = deserializer.fieldValueToObject(Double.class, msg.getByName(SCALING_FACTOR));
    return new MarketDataScaling(scalingFactor);
  }

  @Override
  public String toString() {
    return "Scaling [_scalingFactor=" + _scalingFactor + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketDataScaling scaling = (MarketDataScaling) o;

    if (Double.compare(scaling._scalingFactor, _scalingFactor) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(_scalingFactor);
    return (int) (temp ^ (temp >>> 32));
  }
}
