/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.timeseries.impl;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;


/**
 * Builder for converting TimeSeriesMetaDataRating instances to/from Fudge messages.
 */
@FudgeBuilderFor(TimeSeriesMetaDataRating.class)
public class TimeSeriesMetaDataRatingBuilder implements FudgeBuilder<TimeSeriesMetaDataRating> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, TimeSeriesMetaDataRating object) {
    MutableFudgeFieldContainer message = context.newMessage();
    message.add("fieldName", object.getFieldName());
    message.add("fieldValue", object.getFieldValue());
    message.add("rating", object.getRating());
    return message;
  }

  @Override
  public TimeSeriesMetaDataRating buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    String fieldName = message.getString("fieldName");
    if (fieldName == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataRating - field 'fieldName' is not present");
    }
    String fieldValue = message.getString("fieldValue");
    if (fieldValue == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataRating - field 'fieldValue' is not present");
    }
    Integer rating = message.getInt("rating");
    if (rating == null) {
      throw new IllegalArgumentException("Fudge message is not a TimeSeriesMetaDataRating - field 'rating' is not present");
    }
    return new TimeSeriesMetaDataRating(fieldName, fieldValue, rating);
  }

}
