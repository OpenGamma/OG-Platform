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

import com.opengamma.timeseries.precise.instant.ImmutableInstantDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ImmutableInstantDoubleTimeSeries
 */
@FudgeBuilderFor(ImmutableInstantDoubleTimeSeries.class)
public class ImmutableInstantDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ImmutableInstantDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ImmutableInstantDoubleTimeSeries object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public ImmutableInstantDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (ImmutableInstantDoubleTimeSeries) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
