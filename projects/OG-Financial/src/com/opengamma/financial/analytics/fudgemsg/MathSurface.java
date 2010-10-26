/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.math.interpolation.Interpolator2D;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.math.surface.InterpolatedDoublesSurface;

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
  public static final class ConstantDoublesSurfaceBuilder extends FudgeBuilderBase<ConstantDoublesSurface> {
    private static final String Z_VALUE_FIELD_NAME = "z value";
    private static final String SURFACE_NAME_FIELD_NAME = "surface name";

    @Override
    public ConstantDoublesSurface buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return ConstantDoublesSurface.from(message.getFieldValue(Double.class, message.getByName(Z_VALUE_FIELD_NAME)), message.getFieldValue(String.class, message.getByName(SURFACE_NAME_FIELD_NAME)));
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final ConstantDoublesSurface object) {
      message.add(Z_VALUE_FIELD_NAME, null, object.getZValue(0., 0.));
      message.add(SURFACE_NAME_FIELD_NAME, null, object.getName());
    }

  }

  /**
   * Fudge builder for {@code InterpolatedDoublesSurface}
   */
  @FudgeBuilderFor(InterpolatedDoublesSurface.class)
  public static final class InterpolatedDoublesSurfaceBuilder extends FudgeBuilderBase<InterpolatedDoublesSurface> {
    private static final String X_DATA_FIELD_NAME = "x data";
    private static final String Y_DATA_FIELD_NAME = "y data";
    private static final String Z_DATA_FIELD_NAME = "z data";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    private static final String SURFACE_NAME_FIELD_NAME = "surface name";

    @Override
    public InterpolatedDoublesSurface buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final double[] x = context.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = context.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final double[] z = context.fieldValueToObject(double[].class, message.getByName(Z_DATA_FIELD_NAME));
      final Interpolator2D interpolator = context.fieldValueToObject(Interpolator2D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String name = context.fieldValueToObject(String.class, message.getByName(SURFACE_NAME_FIELD_NAME));
      return InterpolatedDoublesSurface.from(x, y, z, interpolator, name);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final InterpolatedDoublesSurface object) {
      context.objectToFudgeMsg(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      context.objectToFudgeMsg(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      context.objectToFudgeMsg(message, Z_DATA_FIELD_NAME, null, object.getZDataAsPrimitive());
      context.objectToFudgeMsg(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      context.objectToFudgeMsg(message, SURFACE_NAME_FIELD_NAME, null, object.getName());
    }
  }
}
