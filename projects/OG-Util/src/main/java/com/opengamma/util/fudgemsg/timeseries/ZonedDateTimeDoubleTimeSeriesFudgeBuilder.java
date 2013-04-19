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

import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message encoder/decoder (builder) for ZonedDateTimeDoubleTimeSeries
 */
@FudgeBuilderFor(ZonedDateTimeDoubleTimeSeries.class)
public class ZonedDateTimeDoubleTimeSeriesFudgeBuilder implements FudgeBuilder<ZonedDateTimeDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, ZonedDateTimeDoubleTimeSeries object) {
    return DoubleTimeSeriesFudgeBuilder.INSTANCE.buildMessage(serializer, object);
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return (ZonedDateTimeDoubleTimeSeries) DoubleTimeSeriesFudgeBuilder.INSTANCE.buildObject(deserializer, message);
  }

}
