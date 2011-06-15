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
@FudgeBuilderFor(TimeSeriesInfoConfiguration.class)
public class TimeSeriesInfoConfigurationBuilder implements FudgeBuilder<TimeSeriesInfoConfiguration> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializationContext context, final TimeSeriesInfoConfiguration object) {
    MutableFudgeMsg message = context.newMessage();
    for (TimeSeriesInfoRating rule : object.getRules()) {
      context.addToMessage(message, "rules", null, rule);
    }
    return message;
  }

  @Override
  public TimeSeriesInfoConfiguration buildObject(final FudgeDeserializationContext context, final FudgeMsg message) {
    Collection<FudgeField> fields = message.getAllByName("rules");
    final List<TimeSeriesInfoRating> rules = new ArrayList<TimeSeriesInfoRating>(fields.size());
    for (FudgeField field : fields) {
      TimeSeriesInfoRating rule = context.fudgeMsgToObject(TimeSeriesInfoRating.class, (FudgeMsg) field.getValue());
      rules.add(rule);
    }
    return new TimeSeriesInfoConfiguration(rules);
  }

}
