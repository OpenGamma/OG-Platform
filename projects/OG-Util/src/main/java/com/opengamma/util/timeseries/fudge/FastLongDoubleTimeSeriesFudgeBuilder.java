/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * Abstract fudge message builder to serialize and de=serialize FastLongDoubleTimeSeries concrete implementations.
 * To use, just override and implement makeSeries to build the appropriate concrete class. 
 * @param <T> the concrete type to decode
 */
public abstract class FastLongDoubleTimeSeriesFudgeBuilder<T extends FastLongDoubleTimeSeries> implements FudgeBuilder<T> {
  public abstract T makeSeries(DateTimeNumericEncoding encoding, long[] times, double[] values);
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FastLongDoubleTimeSeries object) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, null, 0, object.getClass().getName());
    serializer.addToMessage(message, null, 1, object.getEncoding());
    serializer.addToMessage(message, null, 2, object.timesArrayFast());
    serializer.addToMessage(message, null, 3, object.valuesArrayFast());
    return message;
  }

  @Override
  public T buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return makeSeries((DateTimeNumericEncoding) deserializer.fieldValueToObject(message.getByOrdinal(1)), 
                      (long[]) message.getValue(2), 
                      (double[]) message.getValue(3));
  }

}
