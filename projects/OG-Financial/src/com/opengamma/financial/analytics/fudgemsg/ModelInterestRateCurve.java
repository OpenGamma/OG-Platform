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

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;

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
  public static final class YieldCurveBuilder extends AbstractFudgeBuilder<YieldCurve> {
    private static final String CURVE_FIELD_NAME = "curve";

    @SuppressWarnings("unchecked")
    @Override
    public YieldCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Curve<Double, Double> curve = deserializer.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new YieldCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final YieldCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), Curve.class);
    }
  }

  /**
   * Fudge builder for {@code DiscountCurve}
   */
  @FudgeBuilderFor(DiscountCurve.class)
  public static final class DiscountCurveBuilder extends AbstractFudgeBuilder<DiscountCurve> {
    private static final String CURVE_FIELD_NAME = "curve";

    @SuppressWarnings("unchecked")
    @Override
    public DiscountCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final Curve<Double, Double> curve = deserializer.fieldValueToObject(Curve.class, message.getByName(CURVE_FIELD_NAME));
      return new DiscountCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final DiscountCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), Curve.class);
    }
  }

}
