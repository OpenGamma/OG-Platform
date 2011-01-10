/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.common.Currency;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.Identifier;

/**
 * Builder for converting {@link YieldCurveDefinition} instances to/from Fudge messages.
 */
@FudgeBuilderFor(YieldCurveDefinition.class)
public class YieldCurveDefinitionBuilder implements FudgeBuilder<YieldCurveDefinition> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, YieldCurveDefinition object) {
    MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, "currency", null, object.getCurrency());
    if (object.getRegion() != null) {
      context.objectToFudgeMsg(message, "region", null, object.getRegion());
    }
    message.add("name", object.getName());
    message.add("interpolatorName", object.getInterpolatorName());
    for (FixedIncomeStrip strip : object.getStrips()) {
      context.objectToFudgeMsg(message, "strip", null, strip);
    }
    return message; 
  }

  @Override
  public YieldCurveDefinition buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    Currency currency = context.fieldValueToObject(Currency.class, message.getByName("currency"));
    Identifier region = null;
    if (message.hasField("region")) {
      region = context.fieldValueToObject(Identifier.class, message.getByName("region"));
    }
    String name = message.getString("name");
    String interpolatorName = message.getString("interpolatorName");
    List<FudgeField> allByOrdinal = message.getAllByName("strip");
    SortedSet<FixedIncomeStrip> strips = new TreeSet<FixedIncomeStrip>();
    for (FudgeField field : allByOrdinal) {
      FixedIncomeStrip strip = context.fieldValueToObject(FixedIncomeStrip.class, field);
      strips.add(strip);
    }
    YieldCurveDefinition curveDefinition = new YieldCurveDefinition(currency, region, name, interpolatorName, strips);
    return curveDefinition;
  }

}
