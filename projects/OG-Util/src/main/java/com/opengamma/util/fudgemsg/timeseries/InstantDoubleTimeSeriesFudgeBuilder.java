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

import com.opengamma.timeseries.precise.instant.InstantDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for InstantDoubleTimeSeries
 */
@FudgeBuilderFor(InstantDoubleTimeSeries.class)
public class InstantDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<InstantDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, InstantDoubleTimeSeries object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public InstantDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (InstantDoubleTimeSeries) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
