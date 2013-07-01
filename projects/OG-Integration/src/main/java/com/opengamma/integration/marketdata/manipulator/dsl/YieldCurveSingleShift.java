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

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 * {@link StructureManipulator} that shifts all points on a curve up or down by the same amount.
 * Uses {@link YieldAndDiscountCurve#withParallelShift(double)} to perform the transformation.
 */
public class YieldCurveSingleShift implements StructureManipulator<YieldAndDiscountCurve> {

  /** Field name for Fudge message */
  private static final String SHIFT = "shift";
  /** Field name for Fudge message */
  private static final String TIME = "time";

  /** The shift to apply */
  private final double _shift;
  /** The time */
  private final double _t;

  /* package */ YieldCurveSingleShift(double t, double shift) {
    _t = t;
    _shift = shift;
  }

  @Override
  public YieldAndDiscountCurve execute(YieldAndDiscountCurve structure) {
    return structure.withSingleShift(_t, _shift);
  }

  @Override
  public Class<YieldAndDiscountCurve> getExpectedType() {
    return YieldAndDiscountCurve.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, TIME, null, _t);
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static YieldCurveSingleShift fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double t = deserializer.fieldValueToObject(Double.class, msg.getByName(TIME));
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new YieldCurveSingleShift(t, shift);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    YieldCurveSingleShift that = (YieldCurveSingleShift) o;

    if (Double.compare(that._shift, _shift) != 0) {
      return false;
    }
    if (Double.compare(that._t, _t) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(_shift);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_t);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
