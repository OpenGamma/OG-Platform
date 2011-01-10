/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Fudge message builder (serializer/deserializer) for LocalDateDoubleTimeSeries.
 */
public class LocalDateDoubleTimeSeriesBuilder implements FudgeBuilder<LocalDateDoubleTimeSeries> {

  @Override
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, LocalDateDoubleTimeSeries object) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LocalDateDoubleTimeSeries buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    // TODO Auto-generated method stub
    return null;
  }

}
