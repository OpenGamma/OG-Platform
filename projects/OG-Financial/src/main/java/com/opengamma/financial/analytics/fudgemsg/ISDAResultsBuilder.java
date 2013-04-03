/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

/**
 * Fudge builder for objects used in ISDA pricing of CDS, CDX and CDS options
 *
 */
final class ISDAResultsBuilder {

  private ISDAResultsBuilder() {
  }

  /**
   * Fudge builder for {@link ISDADateCurve}
   */
  @FudgeBuilderFor(ISDADateCurve.class)
  public static final class ISDADateCurveBuilder extends AbstractFudgeBuilder<ISDADateCurve> {
    private static final String NAME_FIELD_NAME = "name";
    private static final String CURVE_FIELD_NAME = "curve";
    private static final String OFFSET_FIELD_NAME = "offset";
    private static final String DATES_FIELD_NAME = "dates";

    @Override
    public ISDADateCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final DoublesCurve curve = (DoublesCurve) deserializer.fieldValueToObject(message.getByName(CURVE_FIELD_NAME));
      final String name;
      if (message.hasField(NAME_FIELD_NAME)) {
        name = message.getString(NAME_FIELD_NAME);
      } else {
        name = curve.getName();
      }
      final double offset = message.getDouble(OFFSET_FIELD_NAME);
      final List<ZonedDateTime> dates = (List<ZonedDateTime>) deserializer.fieldValueToObject(message.getByName(DATES_FIELD_NAME));
      return new ISDADateCurve(name, dates.toArray(new ZonedDateTime[dates.size()]), ArrayUtils.toPrimitive(curve.getXData()), ArrayUtils.toPrimitive(curve.getYData()), offset);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final ISDADateCurve object) {
      message.add(NAME_FIELD_NAME, object.getName());
      serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve());
      message.add(OFFSET_FIELD_NAME, object.getOffset());
      serializer.addToMessageWithClassHeaders(message, OFFSET_FIELD_NAME, null, Double.valueOf(object.getOffset()), double.class);
      serializer.addToMessageWithClassHeaders(message, DATES_FIELD_NAME, null, Arrays.asList(object.getCurveDates()));
    }
  }

  @FudgeBuilderFor(HazardRateCurve.class)
  public static final class HazardRateCurveBuilder extends AbstractFudgeBuilder<HazardRateCurve> {
    private static final String DATES_FIELD = "curveDates";
    private static final String TIMES_FIELD = "times";
    private static final String RATES_FIELD = "rates";
    private static final String OFFSET_FIELD = "offset";
    @Override
    public HazardRateCurve buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      final List<ZonedDateTime> dates = (List<ZonedDateTime>) deserializer.fieldValueToObject(message.getByName(DATES_FIELD));
      final double[] times = deserializer.fieldValueToObject(double[].class, message.getByName(TIMES_FIELD));
      final double[] rates = deserializer.fieldValueToObject(double[].class, message.getByName(RATES_FIELD));
      final double offset = message.getDouble(OFFSET_FIELD);
      return new HazardRateCurve(dates.toArray(new ZonedDateTime[dates.size()]), times, rates, offset);
    }

    @Override
    protected void buildMessage(final FudgeSerializer serializer, final MutableFudgeMsg message, final HazardRateCurve object) {
      serializer.addToMessageWithClassHeaders(message, DATES_FIELD, null, Arrays.asList(object.getCurveTenors()));
      serializer.addToMessageWithClassHeaders(message, TIMES_FIELD, null, object.getTimes());
      serializer.addToMessageWithClassHeaders(message, RATES_FIELD, null, object.getRates());
      message.add(OFFSET_FIELD, object.getOffset());
    }
  }
}
