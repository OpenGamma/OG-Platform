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

/**
*
*/
public class VolatilitySurfaceParallelShift implements StructureManipulator<VolatilitySurface> {

  private static final String SHIFT = "shift";

  private final double _shift;

  public VolatilitySurfaceParallelShift(double shift) {
    _shift = shift;
  }

  @Override
  public VolatilitySurface execute(VolatilitySurface surface,
                                   ValueSpecification valueSpecification,
                                   FunctionExecutionContext executionContext) {
    return surface.withParallelShift(_shift);
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

  public static VolatilitySurfaceParallelShift fromFudgeMsg(FudgeDeserializer deserializer, FudgeMsg msg) {
    Double shift = deserializer.fieldValueToObject(Double.class, msg.getByName(SHIFT));
    return new VolatilitySurfaceParallelShift(shift);
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
    final VolatilitySurfaceParallelShift other = (VolatilitySurfaceParallelShift) obj;
    return Objects.equals(this._shift, other._shift);
  }

  @Override
  public String toString() {
    return "VolatilitySurfaceParallelShift [" +
        "_shift=" + _shift +
        "]";
  }
}
