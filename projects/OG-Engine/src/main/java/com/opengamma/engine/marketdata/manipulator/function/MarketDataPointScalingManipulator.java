/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.manipulator.function;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Use {@code MarketDataScaling} instead.
 */
@Deprecated
public class MarketDataPointScalingManipulator implements StructureManipulator<Double> {

  private final Double _scalingFactor;

  public MarketDataPointScalingManipulator(Double scalingFactor) {
    ArgumentChecker.notNull(scalingFactor, "scalingFactor");
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
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("scalingFactor", _scalingFactor);
    return msg;
  }

  public static MarketDataPointScalingManipulator fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataPointScalingManipulator(msg.getDouble("scalingFactor"));
  }
}
