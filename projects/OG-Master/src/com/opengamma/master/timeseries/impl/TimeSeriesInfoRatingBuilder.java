/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

/**
 * Builder for converting TimeSeriesInfoRating instances to/from Fudge messages.
 */
@FudgeBuilderFor(TimeSeriesInfoRating.class)
public class TimeSeriesInfoRatingBuilder implements FudgeBuilder<TimeSeriesInfoRating> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, TimeSeriesInfoRating object) {
    MutableFudgeMsg message = context.newMessage();
    message.add("fieldName", object.getFieldName());
    message.add("fieldValue", object.getFieldValue());
    message.add("rating", object.getRating());
    return message;
  }

  @Override
  public TimeSeriesInfoRating buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    String fieldName = message.getString("fieldName");
    if (fieldName == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesInfoRating - field 'fieldName' is not present");
    }
    String fieldValue = message.getString("fieldValue");
    if (fieldValue == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesInfoRating - field 'fieldValue' is not present");
    }
    Integer rating = message.getInt("rating");
    if (rating == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesInfoRating - field 'rating' is not present");
    }
    return new TimeSeriesInfoRating(fieldName, fieldValue, rating);
  }

}
