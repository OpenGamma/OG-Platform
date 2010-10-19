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

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.math.interpolation.FlatExtrapolator1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.LinearExtrapolator1D;

/**
 * Holds Fudge builders for the interest rate curve model.
 */
/* package */final class ModelInterestRateCurve {

  /**
   * Restricted constructor.
   */
  private ModelInterestRateCurve() {
  }

  /**
   * Fudge builder for {@code ConstantDoublesCurve}
   */
  @FudgeBuilderFor(ConstantDoublesCurve.class)
  public static final class ConstantDoublesCurveBuilder extends FudgeBuilderBase<ConstantDoublesCurve> {
    private static final String Y_VALUE_FIELD_NAME = "y value";
    private static final String CURVE_NAME_FIELD_NAME = "curve name";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final ConstantDoublesCurve object) {
      message.add(Y_VALUE_FIELD_NAME, null, object.getYValue(0.));
      message.add(CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public ConstantDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return ConstantDoublesCurve.from(message.getFieldValue(Double.class, message.getByName(Y_VALUE_FIELD_NAME)),
          message.getFieldValue(String.class, message.getByName(CURVE_NAME_FIELD_NAME)));
    }
  }

  /**
   * Fudge builder for {@code InterpolatedDoublesCurve}
   */
  @FudgeBuilderFor(InterpolatedDoublesCurve.class)
  public static final class InterpolatedDoublesCurveBuilder extends FudgeBuilderBase<InterpolatedDoublesCurve> {
    private static final String X_DATA_FIELD_NAME = "x data";
    private static final String Y_DATA_FIELD_NAME = "y data";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";
    private static final String CURVE_NAME_FIELD_NAME = "curve name";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final InterpolatedDoublesCurve object) {
      context.objectToFudgeMsg(message, X_DATA_FIELD_NAME, null, object.getXDataAsPrimitive());
      context.objectToFudgeMsg(message, Y_DATA_FIELD_NAME, null, object.getYDataAsPrimitive());
      context.objectToFudgeMsg(message, INTERPOLATOR_FIELD_NAME, null, object.getInterpolator());
      context.objectToFudgeMsg(message, CURVE_NAME_FIELD_NAME, null, object.getName());
    }

    @Override
    public InterpolatedDoublesCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final double[] x = context.fieldValueToObject(double[].class, message.getByName(X_DATA_FIELD_NAME));
      final double[] y = context.fieldValueToObject(double[].class, message.getByName(Y_DATA_FIELD_NAME));
      final Interpolator1D interpolator = context.fieldValueToObject(Interpolator1D.class, message.getByName(INTERPOLATOR_FIELD_NAME));
      final String name = context.fieldValueToObject(String.class, message.getByName(CURVE_NAME_FIELD_NAME));
      return InterpolatedDoublesCurve.fromSorted(x, y, interpolator, name);
    }
  }

  @FudgeBuilderFor(CombinedInterpolatorExtrapolator.class)
  public static final class CombinedInterpolatorExtrapolatorBuilder extends FudgeBuilderBase<CombinedInterpolatorExtrapolator<?>> {
    private static final String LEFT_EXTRAPOLATOR_FIELD_NAME = "leftExtrapolator";
    private static final String RIGHT_EXTRAPOLATOR_FIELD_NAME = "rightExtrapolator";
    private static final String INTERPOLATOR_FIELD_NAME = "interpolator";

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final CombinedInterpolatorExtrapolator<?> object) {
      final Interpolator1D<?> interpolator = object.getInterpolator();
      message.add(INTERPOLATOR_FIELD_NAME, Interpolator1DFactory.getInterpolatorName(interpolator));
      message.add(LEFT_EXTRAPOLATOR_FIELD_NAME, getExtrapolatorName(object.getLeftExtrapolator()));
      message.add(RIGHT_EXTRAPOLATOR_FIELD_NAME, getExtrapolatorName(object.getRightExtrapolator()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public CombinedInterpolatorExtrapolator<?> buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final String interpolatorName = message.getString(INTERPOLATOR_FIELD_NAME);
      final String leftExtrapolatorName = message.getString(LEFT_EXTRAPOLATOR_FIELD_NAME);
      final String rightExtrapolatorName = message.getString(RIGHT_EXTRAPOLATOR_FIELD_NAME);
      final Interpolator1D<?> interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
      final Interpolator1D<?> leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      final Interpolator1D<?> rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
    }

    private String getExtrapolatorName(final Interpolator1D<?> extrapolator) {
      if (extrapolator instanceof FlatExtrapolator1D<?>) {
        return Interpolator1DFactory.FLAT_EXTRAPOLATOR;
      }
      if (extrapolator instanceof LinearExtrapolator1D<?>) {
        return Interpolator1DFactory.LINEAR_EXTRAPOLATOR;
      }
      return null;
    }

    @SuppressWarnings("unchecked")
    private Interpolator1D<?> getExtrapolator(final String extrapolatorName, final Interpolator1D<?> interpolator) {
      if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
        return Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE;
      }
      if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
        return new LinearExtrapolator1D(interpolator);
      }
      return null;
    }
  }

  /**
   * Fudge builder for {@code YieldCurve}
   */
  @FudgeBuilderFor(YieldCurve.class)
  public static final class YieldCurveBuilder extends FudgeBuilderBase<YieldCurve> {
    private static final String CURVE_FIELD_NAME = "curve";

    @Override
    public YieldCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final Curve curve = context.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new YieldCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final YieldCurve object) {
      context.objectToFudgeMsgWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), Curve.class);
    }
  }

  /**
   * Fudge builder for {@code DiscountCurve}
   */
  @FudgeBuilderFor(DiscountCurve.class)
  public static final class DiscountCurveBuilder extends FudgeBuilderBase<DiscountCurve> {
    private static final String CURVE_FIELD_NAME = "curve";

    @Override
    public DiscountCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final Curve curve = context.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new DiscountCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final DiscountCurve object) {
      context.objectToFudgeMsgWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), Curve.class);
    }
  }

}
