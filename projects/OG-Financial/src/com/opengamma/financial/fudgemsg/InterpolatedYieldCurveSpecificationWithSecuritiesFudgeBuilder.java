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
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, InterpolatedYieldCurveSpecificationWithSecurities object) {
    MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    context.addToMessage(message, "currency", null, object.getCurrency());
    context.addToMessage(message, "interpolator", null, object.getInterpolator());
    for (FixedIncomeStripWithSecurity resolvedStrip : object.getStrips()) {
      context.addToMessage(message, "resolvedStrips", null, resolvedStrip);
    }
    return message; 
  }

  @Override
  public InterpolatedYieldCurveSpecificationWithSecurities buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    LocalDate curveDate = context.fieldValueToObject(LocalDate.class, message.getByName("curveDate"));
    String name = message.getString("name");
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    Interpolator1D<?> interpolator = context.fieldValueToObject(Interpolator1D.class, message.getByName("interpolator"));
    List<FudgeField> resolvedStripFields = message.getAllByName("resolvedStrips");
    List<FixedIncomeStripWithSecurity> resolvedStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (FudgeField resolvedStripField : resolvedStripFields) {
      resolvedStrips.add(context.fieldValueToObject(FixedIncomeStripWithSecurity.class, resolvedStripField));
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, name, currency, interpolator, resolvedStrips);
  }

}
