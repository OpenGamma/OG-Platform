/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Builder for converting TimeSeriesMataDataConfiguration instances to/from Fudge messages.
 */
@FudgeBuilderFor(HistoricalDataInfoConfiguration.class)
public class HistoricalDataInfoConfigurationBuilder implements FudgeBuilder<HistoricalDataInfoConfiguration> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final HistoricalDataInfoConfiguration object) {
    MutableFudgeMsg message = context.newMessage();
    for (HistoricalDataInfoRating rule : object.getRules()) {
      context.addToMessage(message, "rules", null, rule);
    }
    return message;
  }

  @Override
  public HistoricalDataInfoConfiguration buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    Collection<FudgeField> fields = message.getAllByName("rules");
    final List<HistoricalDataInfoRating> rules = new ArrayList<HistoricalDataInfoRating>(fields.size());
    for (FudgeField field : fields) {
      HistoricalDataInfoRating rule = context.fudgeMsgToObject(HistoricalDataInfoRating.class, (FudgeMsg) field.getValue());
      rules.add(rule);
    }
    return new HistoricalDataInfoConfiguration(rules);
  }

}
