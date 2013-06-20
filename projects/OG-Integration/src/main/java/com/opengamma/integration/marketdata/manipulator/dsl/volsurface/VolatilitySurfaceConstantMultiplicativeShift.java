/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;

/**
*
*/
public class VolatilitySurfaceConstantMultiplicativeShift implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFT = "shift";

  private final double _shift;

  public VolatilitySurfaceConstantMultiplicativeShift(double shift) {
    _shift = shift;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface) {
    return surface.withConstantMultiplicativeShift(_shift);
  }

  @Override
  public Class<VolatilitySurface> getExpectedType() {
    return VolatilitySurface.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    return msg;
  }

  public static VolatilitySurfaceConstantMultiplicativeShift fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new VolatilitySurfaceConstantMultiplicativeShift(shift);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VolatilitySurfaceConstantMultiplicativeShift that = (VolatilitySurfaceConstantMultiplicativeShift) o;

    if (Double.compare(that._shift, _shift) != 0) {
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
