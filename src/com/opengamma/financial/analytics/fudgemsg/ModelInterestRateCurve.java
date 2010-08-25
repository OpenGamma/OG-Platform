/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;

/**
 * Holds Fudge builders for the interest rate curve model.
 */
/* package */final class ModelInterestRateCurve {

  /**
   * Restricted constructor.
   */
  private ModelInterestRateCurve() {
  }

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code ConstantYieldCurve}.
   */
  @FudgeBuilderFor(ConstantYieldCurve.class)
  public static final class ConstantYieldCurveBuilder extends FudgeBuilderBase<ConstantYieldCurve> {
    private static final String RATE_FIELD_NAME = "rate";

    public ConstantYieldCurveBuilder() {
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message,
        final ConstantYieldCurve object) {
      message.add(RATE_FIELD_NAME, null, object.getInterestRate(0.));
    }

    @Override
    public ConstantYieldCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      return new ConstantYieldCurve(message.getFieldValue(Double.class, message.getByName(RATE_FIELD_NAME)));
    }
  }

  /**
   * Fudge builder for {@code InterpolatedDiscountCurve}.
   */
  @FudgeBuilderFor(InterpolatedDiscountCurve.class)
  public static final class InterpolatedDiscountCurveBuilder extends FudgeBuilderBase<InterpolatedDiscountCurve> {
    private static final String DATA_FIELD_NAME = "data";
    private static final String INTERPOLATORS_FIELD_NAME = "interpolator";

    public InterpolatedDiscountCurveBuilder() {
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message,
        final InterpolatedDiscountCurve object) {
      context.objectToFudgeMsg(message, DATA_FIELD_NAME, null, object.getData());
      context.objectToFudgeMsg(message, INTERPOLATORS_FIELD_NAME, null, object.getInterpolators());
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterpolatedDiscountCurve buildObject(final FudgeDeserializationContext context,
        final FudgeFieldContainer message) {
      return new InterpolatedDiscountCurve(context.fieldValueToObject(Map.class, message.getByName(DATA_FIELD_NAME)),
          context.fieldValueToObject(Map.class, message.getByName(INTERPOLATORS_FIELD_NAME)));
    }
  }

  /**
   * Fudge builder for {@code InterpolatedYieldCurve}.
   */
  @FudgeBuilderFor(InterpolatedYieldCurve.class)
  public static final class InterpolatedYieldCurveBuilder extends FudgeBuilderBase<InterpolatedYieldCurve> {
    private static final String DATA_FIELD_NAME = "data";
    private static final String INTERPOLATORS_FIELD_NAME = "interpolators";

    public InterpolatedYieldCurveBuilder() {
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message,
        final InterpolatedYieldCurve object) {
      context.objectToFudgeMsg(message, DATA_FIELD_NAME, null, object.getData());
      context.objectToFudgeMsg(message, INTERPOLATORS_FIELD_NAME, null, object.getInterpolators());
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterpolatedYieldCurve buildObject(final FudgeDeserializationContext context,
        final FudgeFieldContainer message) {
      return new InterpolatedYieldCurve(context.fieldValueToObject(Map.class, message.getByName(DATA_FIELD_NAME)),
          context.fieldValueToObject(Map.class, message.getByName(INTERPOLATORS_FIELD_NAME)));
    }
  }

}
