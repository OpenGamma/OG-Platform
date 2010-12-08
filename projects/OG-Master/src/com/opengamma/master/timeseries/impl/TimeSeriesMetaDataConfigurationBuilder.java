/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;


/**
 * Builder for converting TimeSeriesMataDataConfiguration instances to/from Fudge messages.
 */
@FudgeBuilderFor(TimeSeriesMetaDataConfiguration.class)
public class TimeSeriesMetaDataConfigurationBuilder implements FudgeBuilder<TimeSeriesMetaDataConfiguration> {

  @Override
  public MutableFudgeFieldContainer buildMessage(final FudgeSerializationContext context, final TimeSeriesMetaDataConfiguration object) {
    MutableFudgeFieldContainer message = context.newMessage();
    for (TimeSeriesMetaDataRating rule : object.getRules()) {
//      message.add("rule", context.objectToFudgeMsg(rule));
      context.objectToFudgeMsg(message, "rules", null, rule);
    }
    return message;
  }

  @Override
  public TimeSeriesMetaDataConfiguration buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
    Collection<FudgeField> fields = message.getAllByName("rules");
    final List<TimeSeriesMetaDataRating> rules = new ArrayList<TimeSeriesMetaDataRating>(fields.size());
    for (FudgeField field : fields) {
      TimeSeriesMetaDataRating rule = context.fudgeMsgToObject(TimeSeriesMetaDataRating.class, (FudgeFieldContainer) field.getValue());
      rules.add(rule);
    }
    return new TimeSeriesMetaDataConfiguration(rules);
  }

}
