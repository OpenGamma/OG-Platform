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
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.interpolation.Interpolator1D;

/**
 * Fudge builders for com.opengamma.analytics.math.curve.* classes
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
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ConstantDoublesCurve object) {
      message.add(Y_VALUE_FIELD_NAME, null, object.getYValue(0.));
      message.add(CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public ConstantDoublesCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
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
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final InterpolatedDoublesCurve object) {
      serializer.addToMessage(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      serializer.addToMessage(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      serializer.addToMessage(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      serializer.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public InterpolatedDoublesCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] x = deserializer.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = deserializer.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String name = deserializer.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
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
    public FunctionalDoublesCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final String name = deserializer.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      final Object function = deserializer.fieldValueToObject(message.getByName(CURVE_FUNCTION_FIELD_NAME));
      if (function instanceof Function) {
        return FunctionalDoublesCurve.from((Function) function, name);
      } 
      throw new OpenGammaRuntimeException("Expected serialized function, got " + function);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final FunctionalDoublesCurve object) {
      serializer.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
      serializer.addToMessage(message, CURVE_FUNCTION_FIELD_NAME, null, substituteObject(object.getFunction()));
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
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final NodalDoublesCurve object) {
      serializer.addToMessage(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      serializer.addToMessage(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      serializer.addToMessage(message, CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public NodalDoublesCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final double[] x = deserializer.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = deserializer.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final String name = deserializer.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      NodalDoublesCurve nodalDoublesCurve = new NodalDoublesCurve(x, y, true, name);
      return nodalDoublesCurve;
    }
  }
}
