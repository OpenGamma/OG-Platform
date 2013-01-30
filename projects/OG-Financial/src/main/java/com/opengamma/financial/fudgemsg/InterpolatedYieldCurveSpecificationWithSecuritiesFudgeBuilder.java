/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.LocalDate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting InterpolatedYieldCurveSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecificationWithSecurities.class)
public class InterpolatedYieldCurveSpecificationWithSecuritiesFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecificationWithSecurities> {
  private static final String CURVE_DATE_FIELD = "curveDate";
  private static final String NAME_FIELD = "name";
  private static final String CURRENCY_FIELD = "currency";
  private static final String INTERPOLATOR_FIELD = "interpolator";
  private static final String INTERPOLATE_YIELDS_FIELD = "interpolateYields";
  private static final String RESOLVED_STRIPS_FIELD = "resolvedStrips";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedYieldCurveSpecificationWithSecurities object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, CURVE_DATE_FIELD, null, object.getCurveDate());
    message.add(NAME_FIELD, object.getName());
    serializer.addToMessage(message, CURRENCY_FIELD, null, object.getCurrency());
    serializer.addToMessage(message, INTERPOLATOR_FIELD, null, object.getInterpolator());
    message.add(INTERPOLATE_YIELDS_FIELD, object.interpolateYield());
    for (final FixedIncomeStripWithSecurity resolvedStrip : object.getStrips()) {
      serializer.addToMessage(message, RESOLVED_STRIPS_FIELD, null, resolvedStrip);
    }
    return message;
  }

  @Override
  public InterpolatedYieldCurveSpecificationWithSecurities buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    final LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName("curveDate"));
    final String name = message.getString("name");
    final Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    final Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName("interpolator"));
    final List<FudgeField> resolvedStripFields = message.getAllByName("resolvedStrips");
    final List<FixedIncomeStripWithSecurity> resolvedStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (final FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(deserializer.fieldValueToObject(FixedIncomeStripWithSecurity.class, resolvedStripField));
    }
    if (message.hasField(INTERPOLATE_YIELDS_FIELD)) {
      final boolean interpolateYields = message.getBoolean(INTERPOLATE_YIELDS_FIELD);
      return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, name, currency, interpolator, interpolateYields, resolvedStrips);
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, name, currency, interpolator, resolvedStrips);
  }

}
