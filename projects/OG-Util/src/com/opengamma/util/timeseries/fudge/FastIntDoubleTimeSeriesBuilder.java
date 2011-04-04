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
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;

/**
 * Abstract fudge message builder to serialize and de=serialize FastIntDoubleTimeSeries concrete implementations.
 * To use, just override and implement makeSeries to build the appropriate concrete class. 
 * @param <T> the concrete type to decode
 */
public abstract class FastIntDoubleTimeSeriesBuilder<T extends FastIntDoubleTimeSeries> implements FudgeBuilder<T> {
  public abstract T makeSeries(DateTimeNumericEncoding encoding, int[] times, double[] values);
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, FastIntDoubleTimeSeries object) {
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, null, 0, object.getClass().getName());
    context.addToMessage(message, null, 1, object.getEncoding());
    context.addToMessage(message, null, 2, object.timesArrayFast());
    context.addToMessage(message, null, 3, object.valuesArrayFast());
    return message;
  }

  @Override
  public T buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return makeSeries(context.fieldValueToObject(DateTimeNumericEncoding.class, message.getByOrdinal(1)), 
                      (int[]) message.getValue(2), (double[]) message.getValue(3));
  }

}
