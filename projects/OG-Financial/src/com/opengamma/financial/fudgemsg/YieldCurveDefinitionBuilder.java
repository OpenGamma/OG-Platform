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
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting {@link YieldCurveDefinition} instances to/from Fudge messages.
 */
@FudgeBuilderFor(YieldCurveDefinition.class)
public class YieldCurveDefinitionBuilder implements FudgeBuilder<YieldCurveDefinition> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, YieldCurveDefinition object) {
    MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, "currency", null, object.getCurrency());
    if (object.getRegion() != null) {
      context.addToMessage(message, "region", null, object.getRegion());
    }
    message.add("name", object.getName());
    message.add("interpolatorName", object.getInterpolatorName());
    for (FixedIncomeStrip strip : object.getStrips()) {
      context.addToMessage(message, "strip", null, strip);
    }
    context.addToMessageWithClassHeaders(message, "uniqueId", null, object.getUniqueId(), UniqueId.class);
    return message;
  }

  @Override
  public YieldCurveDefinition buildObject(FudgeDeserializationContext context, FudgeMsg message) {
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
    FudgeField uniqueId = message.getByName("uniqueId");
    if (uniqueId != null) {
      curveDefinition.setUniqueId(context.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return curveDefinition;
  }

}
