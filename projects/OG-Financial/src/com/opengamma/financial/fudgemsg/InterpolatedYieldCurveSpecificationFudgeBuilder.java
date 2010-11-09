/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithIdentifier;
import com.opengamma.math.interpolation.Interpolator1D;

/**
 * Builder for converting Region instances to/from Fudge messages.
 */
@FudgeBuilderFor(InterpolatedYieldCurveSpecification.class)
public class InterpolatedYieldCurveSpecificationFudgeBuilder implements FudgeBuilder<InterpolatedYieldCurveSpecification> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, InterpolatedYieldCurveSpecification object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    context.objectToFudgeMsg(message, "interpolator", null, object.getInterpolator());
    for (FixedIncomeStripWithIdentifier resolvedStrip : object.getStrips()) {
      context.objectToFudgeMsg(message, "resolvedStrips", null, resolvedStrip);
    }
    return message; 
  }

  @Override
  public InterpolatedYieldCurveSpecification buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    LocalDate curveDate = context.fieldValueToObject(LocalDate.class, message.getByName("curveDate"));
    String name = message.getString("name");
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    Interpolator1D<?> interpolator = context.fieldValueToObject(Interpolator1D.class, message.getByName("interpolator"));
    List<FudgeField> resolvedStripFields = message.getAllByName("resolvedStrips");
    List<FixedIncomeStripWithIdentifier> resolvedStrips = new ArrayList<FixedIncomeStripWithIdentifier>();
    for (FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(context.fieldValueToObject(FixedIncomeStripWithIdentifier.class, resolvedStripField));
    }
    return new InterpolatedYieldCurveSpecification(curveDate, name, currency, interpolator, resolvedStrips);
  }

}
