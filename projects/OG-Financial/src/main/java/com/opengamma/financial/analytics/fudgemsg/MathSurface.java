/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * 
 */
final class MathSurface {

  private MathSurface() {
  }

  /**
   * Fudge builder for {@code ConstantDoublesSurface}
   */
  @FudgeBuilderFor(ConstantDoublesSurface.class)
  public static final class ConstantDoublesSurfaceBuilder extends AbstractFudgeBuilder<ConstantDoublesSurface> {
    private static final String Z_VALUE_FIELD_NAME = "z value";
    private static final String SURFACE_NAME_FIELD_NAME = "surface name";

    @Override
    public ConstantDoublesSurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      return ConstantDoublesSurface.from(message.getFieldValue(Double.class, message.getByName(Z_VALUE_FIELD_NAME)), message.getFieldValue(String.class, message.getByName(SURFACE_NAME_FIELD_NAME)));
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ConstantDoublesSurface object) {
      message.add(Z_VALUE_FIELD_NAME, null, object.getZValue(0., 0.));
      message.add(SURFACE_NAME_FIELD_NAME, null, object.getName());
    }

  }

  /**
   * Fudge builder for {@code InterpolatedDoublesSurface}
   */
  @FudgeBuilderFor(InterpolatedDoublesSurface.class)
  public static final class InterpolatedDoublesSurfaceBuilder extends AbstractFudgeBuilder<InterpolatedDoublesSurface> {
    private static final String X_DATA_FIELD_NAME = "x data";
    private static final String Y_DATA_FIELD_NAME = "y data";
    private static final String Z_DATA_FIELD_NAME = "z data";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    private static final String SURFACE_NAME_FIELD_NAME = "surface name";

    @Override
    public InterpolatedDoublesSurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] x = deserializer.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = deserializer.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final double[] z = deserializer.fieldValueToObject(double[].class, message.getByName(Z_DATA_FIELD_NAME));
      final Interpolator2D interpolator = deserializer.fieldValueToObject(Interpolator2D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String name = deserializer.fieldValueToObject(String.class, message.getByName(SURFACE_NAME_FIELD_NAME));
      return InterpolatedDoublesSurface.from(x, y, z, interpolator, name);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final InterpolatedDoublesSurface object) {
      serializer.addToMessage(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      serializer.addToMessage(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      serializer.addToMessage(message, Z_DATA_FIELD_NAME, null, object.getZDataAsPrimitive());
      serializer.addToMessage(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      serializer.addToMessage(message, SURFACE_NAME_FIELD_NAME, null, object.getName());
    }
  }

  @FudgeBuilderFor(FunctionalDoublesSurface.class)
  public static final class FunctionalDoublesSurfaceBuilder extends AbstractFudgeBuilder<FunctionalDoublesSurface> {
    private static final String SURFACE_FUNCTION_FIELD_NAME = "function";
    private static final String SURFACE_NAME_FIELD_NAME = "name";

    @SuppressWarnings("unchecked")
    @Override
    public FunctionalDoublesSurface buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = deserializer.fieldValueToObject(String.class, message.getByName(SURFACE_NAME_FIELD_NAME));
      final Object function = deserializer.fieldValueToObject(message.getByName(SURFACE_FUNCTION_FIELD_NAME));
      if (function instanceof Function) {
        return FunctionalDoublesSurface.from((Function) function, name);
      }
      throw new OpenGammaRuntimeException("Expected serialized Function, got " + function);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final FunctionalDoublesSurface object) {
      serializer.addToMessage(message, SURFACE_NAME_FIELD_NAME, null, object.getName());
      serializer.addToMessage(message, SURFACE_FUNCTION_FIELD_NAME, null, substituteObject(object.getFunction()));
      return;
    }

  }
}
