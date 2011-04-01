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

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Abstract fudge message builder to serialize and de=serialize FastLongDoubleTimeSeries concrete implementations.
 * To use, just override and implement makeSeries to build the appropriate concrete class. 
 * @param <T> the concrete type to decode
 */
public abstract class FastLongDoubleTimeSeriesBuilder<T extends FastLongDoubleTimeSeries> implements FudgeBuilder<T> {
  public abstract T makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values);
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, FastLongDoubleTimeSeries object) {
    final MutableFudgeMsg message = context.newMessage();
    context.objectToFudgeMsg(message, null, 0, object.getClass().getName());
    context.objectToFudgeMsg(message, null, 1, object.getEncoding());
    context.objectToFudgeMsg(message, null, 2, object.timesArrayFast());
    context.objectToFudgeMsg(message, null, 3, object.valuesArrayFast());
    return message;
  }

  @Override
  public T buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return makeSeries((DateTimeNumericEncoding) context.fieldValueToObject(message.getByOrdinal(1)), 
                      (long[]) message.getValue(2), 
                      (double[]) message.getValue(3));
  }

}
