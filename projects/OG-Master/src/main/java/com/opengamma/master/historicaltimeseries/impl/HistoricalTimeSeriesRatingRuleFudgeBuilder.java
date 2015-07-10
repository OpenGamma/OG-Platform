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
public class HistoricalTimeSeriesRatingRuleFudgeBuilder implements FudgeBuilder<HistoricalTimeSeriesRatingRule> {

  /** Field name. */
  public static final String FIELD_NAME_FIELD_NAME = "fieldName";
  /** Field name. */
  public static final String FIELD_VALUE_FIELD_NAME = "fieldValue";
  /** Field name. */
  public static final String RATING_FIELD_NAME = "rating";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, HistoricalTimeSeriesRatingRule object) {
    MutableFudgeMsg message = serializer.newMessage();
    message.add(FIELD_NAME_FIELD_NAME, object.getFieldName());
    message.add(FIELD_VALUE_FIELD_NAME, object.getFieldValue());
    message.add(RATING_FIELD_NAME, object.getRating());
    return message;
  }

  @Override
  public HistoricalTimeSeriesRatingRule buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    String fieldName = message.getString(FIELD_NAME_FIELD_NAME);
    if (fieldName == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'fieldName' is not present");
    }
    String fieldValue = message.getString(FIELD_VALUE_FIELD_NAME);
    if (fieldValue == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'fieldValue' is not present");
    }
    Integer rating = message.getInt(RATING_FIELD_NAME);
    if (rating == null) {
      throw new IllegalArgumentException("Fudge message is not a HistoricalTimeSeriesRatingRule - field 'rating' is not present");
    }
    return HistoricalTimeSeriesRatingRule.of(fieldName, fieldValue, rating);
  }

}
