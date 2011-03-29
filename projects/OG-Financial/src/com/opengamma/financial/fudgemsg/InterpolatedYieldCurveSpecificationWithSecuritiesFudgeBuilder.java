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
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, InterpolatedYieldCurveSpecificationWithSecurities object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "curveDate", null, object.getCurveDate());
    message.add("name", object.getName());
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    context.objectToFudgeMsg(message, "interpolator", null, object.getInterpolator());
    for (FixedIncomeStripWithSecurity resolvedStrip : object.getStrips()) {
      context.objectToFudgeMsg(message, "resolvedStrips", null, resolvedStrip);
    }
    return message; 
  }

  @Override
  public InterpolatedYieldCurveSpecificationWithSecurities buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
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
