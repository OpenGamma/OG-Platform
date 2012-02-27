/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationWithSecurities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting InterpolatedYieldCurveSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecificationWithSecurities.class)
public class InterpolatedYieldCurveSpecificationWithSecuritiesFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecificationWithSecurities> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final InterpolatedYieldCurveSpecificationWithSecurities object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    serializer.addToMessage(message, "currency", null, object.getCurrency());
    serializer.addToMessage(message, "interpolator", null, object.getInterpolator());
    for (final FixedIncomeStripWithSecurity resolvedStrip : object.getStrips()) {
      serializer.addToMessage(message, "resolvedStrips", null, resolvedStrip);
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
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, name, currency, interpolator, resolvedStrips);
  }

}
