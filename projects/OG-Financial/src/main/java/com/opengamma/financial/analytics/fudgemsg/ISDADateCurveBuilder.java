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

import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;

/**
 * Fudge builder for ISDA curve
 *
 * @see ISDADateCurve
 */
@FudgeBuilderFor(ISDADateCurve.class)
public class ISDADateCurveBuilder extends AbstractFudgeBuilder<ISDADateCurve> {

  private static final String NAME_FIELD_NAME = "name";
  private static final String CURVE_FIELD_NAME = "curve";
  private static final String OFFSET_FIELD_NAME = "offset";
  private static final String DATES_FIELD_NAME = "dates";

  @Override
  public ISDADateCurve buildObject(FudgeDeserializer deserializer, FudgeMsg message) {

    final DoublesCurve curve = (DoublesCurve) deserializer.fieldValueToObject(message.getByName(CURVE_FIELD_NAME));

    final String name;
    if (message.hasField(NAME_FIELD_NAME)) {
      name = message.getString(NAME_FIELD_NAME);
    } else {
      name = curve.getName();
    }

    final double offset = deserializer.fieldValueToObject(double.class, message.getByName(OFFSET_FIELD_NAME)).doubleValue();
    final List<ZonedDateTime> dates = (List<ZonedDateTime>) deserializer.fieldValueToObject(message.getByName(DATES_FIELD_NAME));

    return new ISDADateCurve(name, dates.toArray(new ZonedDateTime[dates.size()]), ArrayUtils.toPrimitive(curve.getXData()), ArrayUtils.toPrimitive(curve.getYData()), offset);
  }

  @Override
  protected void buildMessage(FudgeSerializer serializer, MutableFudgeMsg message, ISDADateCurve object) {
    serializer.addToMessageWithClassHeaders(message, NAME_FIELD_NAME, null, object.getName(), String.class);
    serializer.addToMessageWithClassHeaders(message, CURVE_FIELD_NAME, null, object.getCurve());
    serializer.addToMessageWithClassHeaders(message, OFFSET_FIELD_NAME, null, Double.valueOf(object.getOffset()), double.class);
    serializer.addToMessageWithClassHeaders(message, DATES_FIELD_NAME, null, Arrays.asList(object.getCurveTenors()));
  }

}
