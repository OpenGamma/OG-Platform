/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Lists;
import com.opengamma.analytics.financial.model.interestrate.curve.DayPeriodPreCalculatedDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountAddZeroSpreadCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

/**
 * Holds Fudge builders for the interest rate curve model.
 */
/* package */ final class ModelInterestRateCurve {

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
   * Fudge builder for {@link DayPeriodPreCalculatedDiscountCurve}
   */
  @FudgeBuilderFor(DayPeriodPreCalculatedDiscountCurve.class)
  public static final class DayPeriodPreCalculatedDiscountCurveBuilder extends AbstractFudgeBuilder<DayPeriodPreCalculatedDiscountCurve> {
    /** The curve field */
    private static final String CURVE_FIELD_NAME = "curve";
    /** The name field */
    private static final String NAME_FIELD_NAME = "name";

    @Override
    public DayPeriodPreCalculatedDiscountCurve buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      final DoublesCurve curve = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(CURVE_FIELD_NAME));
      final String name;
      if (message.hasField(NAME_FIELD_NAME)) {
        name = message.getString(NAME_FIELD_NAME);
      } else {
        name = curve.getName();
      }
      return new DayPeriodPreCalculatedDiscountCurve(name, curve);
    }
    
    @Override
    protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, DayPeriodPreCalculatedDiscountCurve object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve(), DoublesCurve.class);
      serializer.addToMessage(message, NAME_FIELD_NAME, null, object.getName());
    }
  }

  /**
   * Fudge builder for {@link PriceIndexCurveSimple}
   */
  @FudgeBuilderFor(PriceIndexCurveSimple.class)
  public static final class PriceIndexCurveBuilder extends AbstractFudgeBuilder<PriceIndexCurveSimple> {
    /** The curve field */
    private static final String CURVE_FIELD = "curve";

    @Override
    public PriceIndexCurveSimple buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve curve = deserializer.fieldValueToObject(DoublesCurve.class, message.getByName(CURVE_FIELD));
      return new PriceIndexCurveSimple(curve);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final PriceIndexCurveSimple object) {
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD, null, object.getCurve(), DoublesCurve.class);
    }

  }

  /**
   * Fudge builder for {@link YieldAndDiscountAddZeroSpreadCurve}. This will work as long as there are Fudge builders
   * available for the delegate curve types.
   */
  @FudgeBuilderFor(YieldAndDiscountAddZeroSpreadCurve.class)
  public static final class YieldAndDiscountAddZeroSpreadCurveFudgeBuilder extends AbstractFudgeBuilder<YieldAndDiscountAddZeroSpreadCurve> {

    /** The curve field */
    private static final String CURVES_FIELD = "curves";
    /** The name field */
    private static final String NAME_FIELD = "name";
    /** The subtract field */
    private static final String SUBTRACT_FIELD = "subtract";

    @Override
    protected void buildMessage(FudgeSerializer serializer,
                                MutableFudgeMsg message,
                                YieldAndDiscountAddZeroSpreadCurve curve) {
      serializer.addToMessage(message, NAME_FIELD, null, curve.getName());
      serializer.addToMessage(message, SUBTRACT_FIELD, null, curve.getSign() < 0);
      MutableFudgeMsg curvesMessage = serializer.newMessage();
      for (YieldAndDiscountCurve yieldAndDiscountCurve : curve.getCurves()) {
        serializer.addToMessage(curvesMessage, null, null, yieldAndDiscountCurve);
      }
      serializer.addToMessage(message, CURVES_FIELD, null, curvesMessage);
    }

    @Override
    public YieldAndDiscountAddZeroSpreadCurve buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
      String name = deserializer.fieldValueToObject(String.class, message.getByName(NAME_FIELD));
      FudgeMsg curvesMessage = message.getMessage(CURVES_FIELD);
      List<YieldAndDiscountCurve> curves = Lists.newArrayList();
      for (FudgeField field : curvesMessage) {
        curves.add(deserializer.fieldValueToObject(YieldAndDiscountCurve.class, field));
      }
      boolean subtract = deserializer.fieldValueToObject(Boolean.class, message.getByName(SUBTRACT_FIELD));
      YieldAndDiscountCurve[] curveArray = curves.toArray(new YieldAndDiscountCurve[curves.size()]);
      return new YieldAndDiscountAddZeroSpreadCurve(name, subtract, curveArray);
    }
  }
}
