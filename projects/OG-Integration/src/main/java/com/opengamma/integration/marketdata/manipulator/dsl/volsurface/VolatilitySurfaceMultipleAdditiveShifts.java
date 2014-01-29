/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import java.util.Arrays;
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
import com.opengamma.util.ArgumentChecker;

/**
 * @deprecated Replaced by {@link VolatilitySurfaceShiftManipulator}.
 */
@Deprecated
public class VolatilitySurfaceMultipleAdditiveShifts implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFTS = "shifts";
  private static final String X = "x";
  private static final String Y = "y";

  private final double[] _x;
  private final double[] _y;
  private final double[] _shifts;

  public VolatilitySurfaceMultipleAdditiveShifts(double[] x, double[] y, double[] shifts) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    ArgumentChecker.notNull(shifts, "shifts");
    _x = x;
    _y = y;
    _shifts = shifts;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface,
                                   ValueSpecification valueSpecification,
                                   FunctionExecutionContext executionContext) {
    return surface.withMultipleAdditiveShifts(_x, _y, _shifts);
  }

  @Override
  public Class<VolatilitySurface> getExpectedType() {
    return VolatilitySurface.class;
  }

  public MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer) {
    MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, SHIFTS, null, _shifts);
    serializer.addToMessage(msg, X, null, _x);
    serializer.addToMessage(msg, Y, null, _y);
    return msg;
  }

  public static VolatilitySurfaceMultipleAdditiveShifts fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    double[] shift = deserializer.fieldValueToObject(double[].class, msg.getByName(SHIFTS));
    double[] x = deserializer.fieldValueToObject(double[].class, msg.getByName(X));
    double[] y = deserializer.fieldValueToObject(double[].class, msg.getByName(Y));
    return new VolatilitySurfaceMultipleAdditiveShifts(x, y, shift);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_x, _y, _shifts);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final VolatilitySurfaceMultipleAdditiveShifts other = (VolatilitySurfaceMultipleAdditiveShifts) obj;
    return Arrays.equals(this._x, other._x) &&
        Arrays.equals(this._y, other._y) &&
        Arrays.equals(this._shifts, other._shifts);
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceMultipleAdditiveShifts [" +
        "_x=" + Arrays.toString(_x) +
        ", _y=" + Arrays.toString(_y) +
        ", _shifts=" + Arrays.toString(_shifts) +
        "]";
  }
}
