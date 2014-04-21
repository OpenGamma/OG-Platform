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

public class MarketDataPointRandomizingManipulator implements StructureManipulator<Double> {

  /**
   * The lower bound of the scaling to be applied to the market data value.
   */
  private final double _lowerBound;

  /**
   * The upper bound of the scaling to be applied to the market data value.
   */
  private final double _upperBound;

  /**
   * The precalculated range provided by the 2 bounds.
   */
  private final double _rangeSize;

  /**
   * Creates a randomizing manipulator where the actual market value is shifted by a random
   * factor between the provided lower and upper bounds. Values produced will be shifted by
   * a value greater than or equal to the lower bound, and strictly less than the upper
   * bound. The reason for this is simplicity as it is in line with {@link Math#random()}.
   *
   * The following would create a manipulator such that if the original value is x, the
   * produced value, y, would satisfy 0.9x <= y < 1.1x (i.e. y is within 10% of x).
   *
   * <code>
   *   new MarketDataPointRandomizingManipulator(0.9, 1.1);
   * </code>
   *
   * @param lowerBound the lower bound of the scaling to be applied to the market data value
   * @param upperBound the upper bound of the scaling to be applied to the market data value
   */
  public MarketDataPointRandomizingManipulator(Double lowerBound, Double upperBound) {
    ArgumentChecker.notNull(lowerBound, "lowerBound");
    ArgumentChecker.notNull(upperBound, "upperBound");
    ArgumentChecker.notNegative(lowerBound, "lowerBound");
    ArgumentChecker.isTrue(lowerBound < upperBound, "Lower bound must be less than the upper bound");
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _rangeSize = upperBound - lowerBound;
  }

  @Override
  public Double execute(Double structure,
                        ValueSpecification valueSpecification,
                        FunctionExecutionContext executionContext) {
    return structure * randomFactor();
  }

  @Override
  public Class<Double> getExpectedType() {
    return Double.class;
  }

  private double randomFactor() {
    return Math.random() * _rangeSize + _lowerBound;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add("lowerBound", _lowerBound);
    msg.add("upperBound", _upperBound);
    return msg;
  }

  public static MarketDataPointRandomizingManipulator fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return new MarketDataPointRandomizingManipulator(msg.getDouble("lowerBound"), msg.getDouble("upperBound"));
  }
}
