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

import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ImmutableLocalDateDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableLocalDateDoubleTimeSeries.class)
public class ImmutableLocalDateDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableLocalDateDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableLocalDateDoubleTimeSeries object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public ImmutableLocalDateDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (ImmutableLocalDateDoubleTimeSeries) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
