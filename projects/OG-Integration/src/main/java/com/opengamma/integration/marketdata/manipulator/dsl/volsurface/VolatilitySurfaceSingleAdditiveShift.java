/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import java.util.Objects;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.integration.marketdata.manipulator.dsl.VolatilitySurfaceShiftManipulator;

/**
 * @deprecated Replaced by {@link VolatilitySurfaceShiftManipulator}.
 */
@Deprecated
public class VolatilitySurfaceSingleAdditiveShift implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFT = "shift";
  private static final String X = "x";
  private static final String Y = "y";

  private final double _x;
  private final double _y;
  private final double _shift;

  public VolatilitySurfaceSingleAdditiveShift(double x, double y, double shift) {
    _x = x;
    _y = y;
    _shift = shift;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface,
                                   ValueSpecification valueSpecification,
                                   FunctionExecutionContext executionContext) {
    return surface.withSingleAdditiveShift(_x, _y, _shift);
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

  public static VolatilitySurfaceSingleAdditiveShift fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    Double x = deserializer.fieldValueToObject(Double.class, msg.getByName(X));
    Double y = deserializer.fieldValueToObject(Double.class, msg.getByName(Y));
    return new VolatilitySurfaceSingleAdditiveShift(x, y, shift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_x, _y, _shift);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final VolatilitySurfaceSingleAdditiveShift other = (VolatilitySurfaceSingleAdditiveShift) obj;
    return Objects.equals(this._x, other._x) &&
        Objects.equals(this._y, other._y) &&
        Objects.equals(this._shift, other._shift);
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceSingleAdditiveShift [" +
        "_x=" + _x +
        ", _y=" + _y +
        ", _shift=" + _shift +
        "]";
  }
}
