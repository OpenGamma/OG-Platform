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

import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ImmutableZonedDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableZonedDateTimeDoubleTimeSeries.class)
public class ImmutableZonedDateTimeDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableZonedDateTimeDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableZonedDateTimeDoubleTimeSeries object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public ImmutableZonedDateTimeDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (ImmutableZonedDateTimeDoubleTimeSeries) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
