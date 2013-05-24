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
import com.opengamma.engine.marketdata.manipulator.StructureManipulator;

/**
 *
 */
public class ParallelShift implements StructureManipulator<YieldAndDiscountCurve> {

  private static final String SHIFT = "shift";
  private final double _shift;

  public ParallelShift(double shift) {
    _shift = shift;
  }

  @Override
  public YieldAndDiscountCurve execute(YieldAndDiscountCurve structure) {
    return structure.withParallelShift(_shift);
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static ParallelShift fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new ParallelShift(shift);
  }
}
