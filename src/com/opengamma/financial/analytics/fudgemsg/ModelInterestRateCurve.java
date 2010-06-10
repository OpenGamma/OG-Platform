/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Map;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeObjectDictionary;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;

/**
 * Holds Fudge builders for the interest rate curve model.
 */
/* package */final class ModelInterestRateCurve {

  private static final FudgeBuilder<ConstantYieldCurve> CONSTANT_YIELD_CURVE = new ConstantYieldCurveBuilder();
  private static final FudgeBuilder<InterpolatedYieldCurve> INTERPOLATED_YIELD_CURVE = new InterpolatedYieldCurveBuilder();

  /**
   * Restricted constructor.
   */
  private ModelInterestRateCurve() {
  }

  /* package */static void addBuilders(final FudgeObjectDictionary dictionary) {
    dictionary.addBuilder(ConstantYieldCurve.class, CONSTANT_YIELD_CURVE);
    dictionary.addBuilder(InterpolatedYieldCurve.class, INTERPOLATED_YIELD_CURVE);
  }

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code ConstantYieldCurve}.
   */
  private static final class ConstantYieldCurveBuilder extends FudgeBuilderBase<ConstantYieldCurve> {
    private static final String RATE_FIELD_NAME = "rate";

    private ConstantYieldCurveBuilder() {
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

  //-------------------------------------------------------------------------
  /**
   * Fudge builder for {@code InterpolatedYieldCurve}.
   */
  private static final class InterpolatedYieldCurveBuilder extends FudgeBuilderBase<InterpolatedYieldCurve> {
    private static final String INTERPOLATORS_FIELD_NAME = "interpolators";
    private static final String RATES_FIELD_NAME = "rates";

    private InterpolatedYieldCurveBuilder() {
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message,
        final InterpolatedYieldCurve object) {
      context.objectToFudgeMsg(message, RATES_FIELD_NAME, null, object.getData());
      context.objectToFudgeMsg(message, INTERPOLATORS_FIELD_NAME, null, object.getInterpolators());
    }

    @SuppressWarnings("unchecked")
    @Override
    public InterpolatedYieldCurve buildObject(final FudgeDeserializationContext context,
        final FudgeFieldContainer message) {
      return new InterpolatedYieldCurve(context.fieldValueToObject(Map.class, message.getByName(RATES_FIELD_NAME)),
          context.fieldValueToObject(Map.class, message.getByName(INTERPOLATORS_FIELD_NAME)));
    }
  }

}
