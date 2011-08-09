/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Builder for converting object instances to/from Fudge messages.
 */
@FudgeBuilderFor(HistoricalTimeSeriesRatingRule.class)
public class HistoricalTimeSeriesRatingRuleBuilder implements FudgeBuilder<HistoricalTimeSeriesRatingRule> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, HistoricalTimeSeriesRatingRule object) {
    MutableFudgeMsg message = serializer.newMessage();
    message.add("fieldName", object.getFieldName());
    message.add("fieldValue", object.getFieldValue());
    message.add("rating", object.getRating());
    return message;
  }

  @Override
  public HistoricalTimeSeriesRatingRule buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    String fieldName = message.getString("fieldName");
    if (fieldName == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'fieldName' is not present");
    }
    String fieldValue = message.getString("fieldValue");
    if (fieldValue == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'fieldValue' is not present");
    }
    Integer rating = message.getInt("rating");
    if (rating == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'rating' is not present");
    }
    return new HistoricalTimeSeriesRatingRule(fieldName, fieldValue, rating);
  }

}
