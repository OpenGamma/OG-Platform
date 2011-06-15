/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.curve.NodalDoublesCurve;
import com.opengamma.math.function.Function;
import com.opengamma.math.interpolation.Interpolator1D;

/**
 * Fudge builders for com.opengamma.math.curve.* classes
 */
final class MathCurve {

  private MathCurve() {
  }

  /**
   * Fudge builder for {@code ConstantDoublesCurve}
   */
  @FudgeBuilderFor(ConstantDoublesCurve.class)
  public static final class ConstantDoublesCurveBuilder extends AbstractFudgeBuilder<ConstantDoublesCurve> {
    private static final String Y_VALUE_FIELD_NAME = "y value";
    private static final String CURVE_NAME_FIELD_NAME = "curve name";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final ConstantDoublesCurve object) {
      message.add(Y_VALUE_FIELD_NAME, null, object.getYValue(0.));
      message.add(CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public ConstantDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      return ConstantDoublesCurve.from(message.getFieldValue(Double.class, message.getByName(Y_VALUE_FIELD_NAME)), message.getFieldValue(String.class, message.getByName(CURVE_NAME_FIELD_NAME)));
    }
  }

  /**
   * Fudge builder for {@code InterpolatedDoublesCurve}
   */
  @FudgeBuilderFor(InterpolatedDoublesCurve.class)
  public static final class InterpolatedDoublesCurveBuilder extends AbstractFudgeBuilder<InterpolatedDoublesCurve> {
    private static final String X_DATA_FIELD_NAME = "x data";
    private static final String Y_DATA_FIELD_NAME = "y data";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    private static final String CURVE_NAME_FIELD_NAME = "curve name";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final InterpolatedDoublesCurve object) {
      context.addToMessage(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      context.addToMessage(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      context.addToMessage(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      context.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public InterpolatedDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final double[] x = context.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = context.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final Interpolator1D interpolator = context.fieldValueToObject(Interpolator1D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String name = context.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      return InterpolatedDoublesCurve.fromSorted(x, y, interpolator, name);
    }
  }

  /**
   * Fudge builder for {@code FunctionalDoublesCurve}
   */
  @FudgeBuilderFor(FunctionalDoublesCurve.class)
  public static final class FunctionalDoublesCurveBuilder extends AbstractFudgeBuilder<FunctionalDoublesCurve> {
    private static final String CURVE_FUNCTION_FIELD_NAME = "function";
    private static final String CURVE_NAME_FIELD_NAME = "name";

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public FunctionalDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final String name = context.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      final Object function = context.fieldValueToObject(message.getByName(CURVE_FUNCTION_FIELD_NAME));
      if (function instanceof Function) {
        return FunctionalDoublesCurve.from((Function) function, name);
      } else {
        throw new OpenGammaRuntimeException("Expected serialized function, got " + function);
      }
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final FunctionalDoublesCurve object) {
      context.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
      context.addToMessage(message, CURVE_FUNCTION_FIELD_NAME, null, substituteObject(object.getFunction()));
      return;
    }
  }


  /**
   * Fudge builder for {@code NodalDoublesCurve}
   */
  @FudgeBuilderFor(NodalDoublesCurve.class)
  public static final class NodalDoublesCurveBuilder extends AbstractFudgeBuilder<NodalDoublesCurve> {
    private static final String X_DATA_FIELD_NAME = "x data";
    private static final String Y_DATA_FIELD_NAME = "y data";
    private static final String CURVE_NAME_FIELD_NAME = "curve name";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeMsg message, final NodalDoublesCurve object) {
      context.addToMessage(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      context.addToMessage(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      context.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public NodalDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
      final double[] x = context.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = context.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final String name = context.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      NodalDoublesCurve nodalDoublesCurve = new NodalDoublesCurve(x, y, true, name);
      return nodalDoublesCurve;
    }
  }
}
