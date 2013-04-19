/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.timeseries;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.timeseries.date.DateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for DateDoubleTimeSeries
 */
@FudgeBuilderFor(DateDoubleTimeSeries.class)
public class DateDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<DateDoubleTimeSeries<?>> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, DateDoubleTimeSeries<?> object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public DateDoubleTimeSeries<?> buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (DateDoubleTimeSeries<?>) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
