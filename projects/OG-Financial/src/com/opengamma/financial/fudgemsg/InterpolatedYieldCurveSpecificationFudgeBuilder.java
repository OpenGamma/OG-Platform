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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.id.Identifier;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting InterpolatedYieldCurveSpecification instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecification.class)
public class InterpolatedYieldCurveSpecificationFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecification> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, InterpolatedYieldCurveSpecification object) {
    MutableFudgeMsg message = context.newMessage();
    context.objectToFudgeMsg(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    context.objectToFudgeMsg(message, "region", null, object.getRegion());
    context.objectToFudgeMsg(message, "interpolator", null, object.getInterpolator());
    for (FixedIncomeStripWithIdentifier resolvedStrip : object.getStrips()) {
      context.objectToFudgeMsg(message, "resolvedStrips", null, resolvedStrip);
    }
    return message; 
  }

  @Override
  public InterpolatedYieldCurveSpecification buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    LocalDate curveDate = context.fieldValueToObject(LocalDate.class, message.getByName("curveDate"));
    String name = message.getString("name");
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    Identifier region = context.fieldValueToObject(Identifier.class, message.getByName("region"));
    Interpolator1D<?> interpolator = context.fieldValueToObject(Interpolator1D.class, message.getByName("interpolator"));
    List<FudgeField> resolvedStripFields = message.getAllByName("resolvedStrips");
    List<FixedIncomeStripWithIdentifier> resolvedStrips = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(context.fieldValueToObject(FixedIncomeStripWithIdentifier.class, resolvedStripField));
    }
    return new InterpolatedYieldCurveSpecification(curveDate, name, currency, interpolator, resolvedStrips, region);
  }

}
