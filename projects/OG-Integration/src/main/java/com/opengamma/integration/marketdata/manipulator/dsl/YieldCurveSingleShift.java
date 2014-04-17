/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.Objects;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Uses {@link YieldAndDiscountCurve#withSingleShift} to perform the transformation.
 * TODO can this be deleted in favour of pointShift with a single point?
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
  public YieldAndDiscountCurve execute(YieldAndDiscountCurve structure,
                                       ValueSpecification valueSpecification,
                                       FunctionExecutionContext executionContext) {
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
  public int hashCode() {
    return Objects.hash(_shift, _t);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveSingleShift other = (YieldCurveSingleShift) obj;
    return Objects.equals(this._shift, other._shift) && Objects.equals(this._t, other._t);
  }

  @Override
  public String toString() {
    return "YieldCurveSingleShift [" +
        "_shift=" + _shift +
        ", _t=" + _t +
        "]";
  }
}
