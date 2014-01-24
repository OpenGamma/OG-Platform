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
 * Manipulator that replaces a single market data point with a specified value.
 */
public class MarketDataReplace implements StructureManipulator<Double> {

  /** Field name for Fudge message. */
  private static final String VALUE = "value";

  /** The new value for the market data point. */
  private final double _value;

  /* package */ MarketDataReplace(double value) {
    if (Double.isInfinite(value) || Double.isNaN(value)) {
      throw new IllegalArgumentException("value must not be infinite or NaN. value=" + value);
    }
    _value = value;
  }

  @Override
  public Double execute(Double structure,
                        ValueSpecification valueSpecification,
                        FunctionExecutionContext executionContext) {
    return _value;
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, VALUE, null, _value);
    return msg;
  }

  public static MarketDataReplace fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double scalingFactor = deserializer.fieldValueToObject(Double.class, msg.getByName(VALUE));
    return new MarketDataReplace(scalingFactor);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MarketDataReplace replace = (MarketDataReplace) o;

    if (Double.compare(replace._value, _value) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(_value);
    return (int) (temp ^ (temp >>> 32));
  }

  @Override
  public String toString() {
    return "Replace [_value=" + _value + "]";
  }
}
