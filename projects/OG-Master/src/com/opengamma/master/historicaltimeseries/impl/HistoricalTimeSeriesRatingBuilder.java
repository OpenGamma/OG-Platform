/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

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
@FudgeBuilderFor(HistoricalTimeSeriesRating.class)
public class HistoricalTimeSeriesRatingBuilder implements FudgeBuilder<HistoricalTimeSeriesRating> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final HistoricalTimeSeriesRating object) {
    MutableFudgeMsg message = context.newMessage();
    for (HistoricalTimeSeriesRatingRule rule : object.getRules()) {
      context.addToMessage(message, "rules", null, rule);
    }
    return message;
  }

  @Override
  public HistoricalTimeSeriesRating buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    Collection<FudgeField> fields = message.getAllByName("rules");
    final List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>(fields.size());
    for (FudgeField field : fields) {
      HistoricalTimeSeriesRatingRule rule = context.fudgeMsgToObject(HistoricalTimeSeriesRatingRule.class, (FudgeMsg) field.getValue());
      rules.add(rule);
    }
    return new HistoricalTimeSeriesRating(rules);
  }

}
