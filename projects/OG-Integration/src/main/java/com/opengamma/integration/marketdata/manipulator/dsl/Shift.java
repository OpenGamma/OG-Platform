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

import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 * Manipulator that shift a single market data value by an absolute amount.
 */
public class Shift implements StructureManipulator<Double> {

  /** Field name for Fudge message. */
  private static final String SHIFT = "shift";

  /** Absolute shift added to the market data value. */
  private final double _shift;

  /* package */ Shift(double shift) {
    if (Double.isInfinite(shift) || Double.isNaN(shift)) {
      throw new IllegalArgumentException("shift must not be infinite or NaN. value=" + shift);
    }
    _shift = shift;
  }

  @Override
  public Double execute(Double structure) {
    return structure + _shift;
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static Shift fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double scalingFactor = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new Shift(scalingFactor);
  }

  @Override
  public String toString() {
    return "Shift [_shift=" + _shift + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Shift shift = (Shift) o;

    if (Double.compare(shift._shift, _shift) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(_shift);
    return (int) (temp ^ (temp >>> 32));
  }
}
