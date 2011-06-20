/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

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
 * Builder for converting objects to/from Fudge messages.
 */
@FudgeBuilderFor(HistoricalTimeSeriesInfoConfiguration.class)
public class HistoricalTimeSeriesInfoConfigurationBuilder implements FudgeBuilder<HistoricalTimeSeriesInfoConfiguration> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final HistoricalTimeSeriesInfoConfiguration object) {
    MutableFudgeMsg message = context.newMessage();
    for (HistoricalTimeSeriesInfoRating rule : object.getRules()) {
      context.addToMessage(message, "rules", null, rule);
    }
    return message;
  }

  @Override
  public HistoricalTimeSeriesInfoConfiguration buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    Collection<FudgeField> fields = message.getAllByName("rules");
    final List<HistoricalTimeSeriesInfoRating> rules = new ArrayList<HistoricalTimeSeriesInfoRating>(fields.size());
    for (FudgeField field : fields) {
      HistoricalTimeSeriesInfoRating rule = context.fudgeMsgToObject(HistoricalTimeSeriesInfoRating.class, (FudgeMsg) field.getValue());
      rules.add(rule);
    }
    return new HistoricalTimeSeriesInfoConfiguration(rules);
  }

}
