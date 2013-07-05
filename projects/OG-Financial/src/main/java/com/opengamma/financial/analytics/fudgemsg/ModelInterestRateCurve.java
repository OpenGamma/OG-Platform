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
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

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
   * Fudge builder for {@link YieldCurve}
   */
  @FudgeBuilderFor(YieldCurve.class)
  public static final class YieldCurveBuilder extends AbstractFudgeBuilder<YieldCurve> {
    /** The curve field */
    private static final String CURVE_FIELD_NAME = "curve";
    /** The name field */
    private static final String NAME_FIELD_NAME = "name";

    @Override
    public YieldCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve curve = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(CURVE_FIELD_NAME));
      final String name;
      if (message.hasField(NAME_FIELD_NAME)) {
        name = message.getString(NAME_FIELD_NAME);
      } else {
        name = curve.getName();
      }
      return new YieldCurve(name, curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final YieldCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), DoublesCurve.class);
      serializer.addToMessage(message, NAME_FIELD_NAME, null, object.getName());
    }
  }

  /**
   * Fudge builder for {@link DiscountCurve}
   */
  @FudgeBuilderFor(DiscountCurve.class)
  public static final class DiscountCurveBuilder extends AbstractFudgeBuilder<DiscountCurve> {
    /** The curve field */
    private static final String CURVE_FIELD_NAME = "curve";
    /** The name field */
    private static final String NAME_FIELD_NAME = "name";

    @Override
    public DiscountCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve curve = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(CURVE_FIELD_NAME));
      final String name;
      if (message.hasField(NAME_FIELD_NAME)) {
        name = message.getString(NAME_FIELD_NAME);
      } else {
        name = curve.getName();
      }
      return new DiscountCurve(name, curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final DiscountCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), DoublesCurve.class);
      serializer.addToMessage(message, NAME_FIELD_NAME, null, object.getName());
    }
  }

  /**
   * Fudge builder for {@link PriceIndexCurve}
   */
  @FudgeBuilderFor(PriceIndexCurve.class)
  public static final class PriceIndexCurveBuilder extends AbstractFudgeBuilder<PriceIndexCurve> {
    /** The curve field */
    private static final String CURVE_FIELD = "curve";

    @Override
    public PriceIndexCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve curve = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(CURVE_FIELD));
      return new PriceIndexCurve(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PriceIndexCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD, null, object.getCurve(), DoublesCurve.class);
    }

  }
}
