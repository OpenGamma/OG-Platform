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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
 * {@link StructureManipulator} that shifts all points on a curve up or down by the same amount.
 * Uses {@link YieldAndDiscountCurve#withParallelShift(double)} to perform the transformation.
 */
public class YieldCurveParallelShift implements StructureManipulator<YieldAndDiscountCurve> {

  private static final Logger s_logger = LoggerFactory.getLogger(YieldCurveParallelShift.class);

  /** Field name for Fudge message */
  private static final String SHIFT = "shift";
  /** The shift to apply  */
  private final double _shift;

  /* package */ YieldCurveParallelShift(double shift) {
    _shift = shift;
  }

  @Override
  public YieldAndDiscountCurve execute(YieldAndDiscountCurve structure) {
    s_logger.debug("Shifting curve {} by {}", structure.getName(), _shift);
    return structure.withParallelShift(_shift);
  }

  @Override
  public Class<YieldAndDiscountCurve> getExpectedType() {
    return YieldAndDiscountCurve.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static YieldCurveParallelShift fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new YieldCurveParallelShift(shift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_shift);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final YieldCurveParallelShift other = (YieldCurveParallelShift) obj;
    return Objects.equals(this._shift, other._shift);
  }

  @Override
  public String toString() {
    return "YieldCurveParallelShift [" +
        "_shift=" + _shift +
        "]";
  }
}
