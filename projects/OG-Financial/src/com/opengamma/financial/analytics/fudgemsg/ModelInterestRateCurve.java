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
import com.opengamma.math.curve.Curve;

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
   * Fudge builder for {@code YieldCurve}
   */
  @FudgeBuilderFor(YieldCurve.class)
  public static final class YieldCurveBuilder extends FudgeBuilderBase<YieldCurve> {
    private static final String CURVE_FIELD_NAME = "curve";

    @SuppressWarnings("unchecked")
    @Override
    public YieldCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final Curve<Double, Double> curve = context.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
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

    @SuppressWarnings("unchecked")
    @Override
    public DiscountCurve buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      final Curve<Double, Double> curve = context.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new DiscountCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final DiscountCurve object) {
      context.objectToFudgeMsgWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), Curve.class);
    }
  }

}
