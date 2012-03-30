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

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting InterpolatedYieldCurveSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecification.class)
public class InterpolatedYieldCurveSpecificationFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InterpolatedYieldCurveSpecification object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    serializer.addToMessage(message, "currency", null, object.getCurrency());
    serializer.addToMessage(message, "region", null, object.getRegion());
    serializer.addToMessage(message, "interpolator", null, object.getInterpolator());
    for (FixedIncomeStripWithIdentifier resolvedStrip : object.getStrips()) {
      serializer.addToMessage(message, "resolvedStrips", null, resolvedStrip);
    }
    return message; 
  }

  @Override
  public InterpolatedYieldCurveSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    LocalDate curveDate = deserializer.fieldValueToObject(LocalDate.class, message.getByName("curveDate"));
    String name = message.getString("name");
    Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    ExternalId region = deserializer.fieldValueToObject(ExternalId.class, message.getByName("region"));
    Interpolator1D interpolator = deserializer.fieldValueToObject(Interpolator1D.class, message.getByName("interpolator"));
    List<FudgeField> resolvedStripFields = message.getAllByName("resolvedStrips");
    List<FixedIncomeStripWithIdentifier> resolvedStrips = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(deserializer.fieldValueToObject(FixedIncomeStripWithIdentifier.class, resolvedStripField));
    }
    return new InterpolatedYieldCurveSpecification(curveDate, name, currency, interpolator, resolvedStrips, region);
  }

}
