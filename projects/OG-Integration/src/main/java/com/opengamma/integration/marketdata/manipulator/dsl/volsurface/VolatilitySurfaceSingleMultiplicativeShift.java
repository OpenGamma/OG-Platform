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
public class VolatilitySurfaceSingleMultiplicativeShift implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFT = "shift";
  private static final String X = "x";
  private static final String Y = "y";

  private final double _x;
  private final double _y;
  private final double _shift;

  public VolatilitySurfaceSingleMultiplicativeShift(double x, double y, double shift) {
    _x = x;
    _y = y;
    _shift = shift;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface) {
    return surface.withSingleMultiplicativeShift(_x, _y, _shift);
  }

  @Override
  public Class<VolatilitySurface> getExpectedType() {
    return VolatilitySurface.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFT, null, _shift);
    serializer.addToMessage(msg, X, null, _x);
    serializer.addToMessage(msg, Y, null, _y);
    return msg;
  }

  public static VolatilitySurfaceSingleMultiplicativeShift fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    Double x = deserializer.fieldValueToObject(Double.class, msg.getByName(X));
    Double y = deserializer.fieldValueToObject(Double.class, msg.getByName(Y));
    return new VolatilitySurfaceSingleMultiplicativeShift(x, y, shift);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VolatilitySurfaceSingleMultiplicativeShift that = (VolatilitySurfaceSingleMultiplicativeShift) o;

    if (Double.compare(that._shift, _shift) != 0) {
      return false;
    }
    if (Double.compare(that._x, _x) != 0) {
      return false;
    }
    if (Double.compare(that._y, _y) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    temp = Double.doubleToLongBits(_x);
    result = (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_y);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_shift);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }
}
