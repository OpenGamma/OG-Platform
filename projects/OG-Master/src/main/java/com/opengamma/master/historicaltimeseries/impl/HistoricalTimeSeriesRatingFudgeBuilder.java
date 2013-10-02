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
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Builder for converting objects to/from Fudge messages.
 */
@FudgeBuilderFor(HistoricalTimeSeriesRating.class)
public class HistoricalTimeSeriesRatingFudgeBuilder implements FudgeBuilder<HistoricalTimeSeriesRating> {

  /** Field name. */
  public static final String RULES_FIELD_NAME = "rules";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final HistoricalTimeSeriesRating object) {
    MutableFudgeMsg message = serializer.newMessage();
    for (HistoricalTimeSeriesRatingRule rule : object.getRules()) {
      serializer.addToMessage(message, RULES_FIELD_NAME, null, rule);
    }
    return message;
  }

  @Override
  public HistoricalTimeSeriesRating buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    Collection<FudgeField> fields = message.getAllByName(RULES_FIELD_NAME);
    final List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>(fields.size());
    for (FudgeField field : fields) {
      HistoricalTimeSeriesRatingRule rule = deserializer.fudgeMsgToObject(HistoricalTimeSeriesRatingRule.class, (FudgeMsg) field.getValue());
      rules.add(rule);
    }
    return HistoricalTimeSeriesRating.of(rules);
  }

}
