/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Fudge message builder for {@code SimpleHistoricalTimeSeries}.
 */
@FudgeBuilderFor(SimpleHistoricalTimeSeries.class)
public class SimpleHistoricalTimeSeriesFudgeBuilder implements FudgeBuilder<SimpleHistoricalTimeSeries> {

  private static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  private static final String TIMESERIES_FIELD_NAME = "timeSeries";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, SimpleHistoricalTimeSeries object) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (object.getUniqueId() != null) {
      serializer.addToMessage(message, UNIQUE_ID_FIELD_NAME, null, object.getUniqueId());
    }
    if (object.getTimeSeries() != null) {
      serializer.addToMessage(message, TIMESERIES_FIELD_NAME, null, object.getTimeSeries());
    }
    return message;
  }

  @Override
  public SimpleHistoricalTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    UniqueId uniqueId = null;
    LocalDateDoubleTimeSeries timeseries = null;
    if (message.getByName(UNIQUE_ID_FIELD_NAME) != null) {
      uniqueId = deserializer.fieldValueToObject(UniqueId.class, message.getByName(UNIQUE_ID_FIELD_NAME));
    }
    if (message.hasField(TIMESERIES_FIELD_NAME)) {
      Object fieldValue = deserializer.fieldValueToObject(message.getByName(TIMESERIES_FIELD_NAME));
      if (fieldValue instanceof LocalDateDoubleTimeSeries) {
        timeseries = (LocalDateDoubleTimeSeries) fieldValue;
      }
    }
    if (uniqueId != null && timeseries != null) {
      return new SimpleHistoricalTimeSeries(uniqueId, timeseries);
    } else {
      throw new OpenGammaRuntimeException("Cannot deserialize " + message + " to SimpleHistoricalTimeSeries");
    }
  }

}
