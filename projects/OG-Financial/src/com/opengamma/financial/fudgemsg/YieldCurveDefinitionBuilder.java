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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.financial.analytics.ircurve.FixedIncomeStrip;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Builder for converting {@link YieldCurveDefinition} instances to/from Fudge messages.
 */
@FudgeBuilderFor(YieldCurveDefinition.class)
public class YieldCurveDefinitionBuilder implements FudgeBuilder<YieldCurveDefinition> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, YieldCurveDefinition object) {
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, "currency", null, object.getCurrency());
    if (object.getRegionId() != null) {
      serializer.addToMessage(message, "region", null, object.getRegionId());
    }
    message.add("name", object.getName());
    message.add("interpolatorName", object.getInterpolatorName());
    for (FixedIncomeStrip strip : object.getStrips()) {
      serializer.addToMessage(message, "strip", null, strip);
    }
    serializer.addToMessageWithClassHeaders(message, "uniqueId", null, object.getUniqueId(), UniqueId.class);
    return message;
  }

  @Override
  public YieldCurveDefinition buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    Currency currency = deserializer.fieldValueToObject(Currency.class, message.getByName("currency"));
    ExternalId region = null;
    if (message.hasField("region")) {
      region = deserializer.fieldValueToObject(ExternalId.class, message.getByName("region"));
    }
    String name = message.getString("name");
    String interpolatorName = message.getString("interpolatorName");
    List<FudgeField> allByOrdinal = message.getAllByName("strip");
    SortedSet<FixedIncomeStrip> strips = new TreeSet<FixedIncomeStrip>();
    for (FudgeField field : allByOrdinal) {
      FixedIncomeStrip strip = deserializer.fieldValueToObject(FixedIncomeStrip.class, field);
      strips.add(strip);
    }
    YieldCurveDefinition curveDefinition = new YieldCurveDefinition(currency, region, name, interpolatorName, strips);
    FudgeField uniqueId = message.getByName("uniqueId");
    if (uniqueId != null) {
      curveDefinition.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueId));
    }
    return curveDefinition;
  }

}
