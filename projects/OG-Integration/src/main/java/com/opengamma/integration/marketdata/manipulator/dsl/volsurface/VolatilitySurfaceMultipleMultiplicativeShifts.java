/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl.volsurface;

import java.util.Arrays;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.util.ArgumentChecker;

/**
*
*/
public class VolatilitySurfaceMultipleMultiplicativeShifts implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFTS = "shifts";
  private static final String X = "x";
  private static final String Y = "y";

  private final double[] _x;
  private final double[] _y;
  private final double[] _shifts;

  public VolatilitySurfaceMultipleMultiplicativeShifts(double[] x, double[] y, double[] shifts) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    ArgumentChecker.notNull(shifts, "shifts");
    _x = x;
    _y = y;
    _shifts = shifts;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface) {
    return surface.withMultipleMultiplicativeShifts(_x, _y, _shifts);
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

  public static VolatilitySurfaceMultipleMultiplicativeShifts fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    double[] shift = deserializer.fieldValueToObject(double[].class, msg.getByName(SHIFTS));
    double[] x = deserializer.fieldValueToObject(double[].class, msg.getByName(X));
    double[] y = deserializer.fieldValueToObject(double[].class, msg.getByName(Y));
    return new VolatilitySurfaceMultipleMultiplicativeShifts(x, y, shift);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VolatilitySurfaceMultipleMultiplicativeShifts that = (VolatilitySurfaceMultipleMultiplicativeShifts) o;

    if (!Arrays.equals(_shifts, that._shifts)) {
      return false;
    }
    if (!Arrays.equals(_x, that._x)) {
      return false;
    }
    if (!Arrays.equals(_y, that._y)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(_x);
    result = 31 * result + Arrays.hashCode(_y);
    result = 31 * result + Arrays.hashCode(_shifts);
    return result;
  }
}
