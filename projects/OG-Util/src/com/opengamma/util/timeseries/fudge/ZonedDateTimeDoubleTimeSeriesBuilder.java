/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for ZonedDateTimeDoubleTimeSeries.
 */
public class ZonedDateTimeDoubleTimeSeriesBuilder implements FudgeBuilder<ZonedDateTimeDoubleTimeSeries> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ZonedDateTimeDoubleTimeSeries object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ZonedDateTimeDoubleTimeSeries buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    // TODO Auto-generated method stub
    return null;
  }

}
