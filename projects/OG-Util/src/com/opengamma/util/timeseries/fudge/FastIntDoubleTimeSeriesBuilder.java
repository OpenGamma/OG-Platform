/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, FastIntDoubleTimeSeries object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, null, 0, object.getClass().getName());
    context.objectToFudgeMsg(message, null, 1, object.getEncoding());
    context.objectToFudgeMsg(message, null, 2, object.timesArrayFast());
    context.objectToFudgeMsg(message, null, 3, object.valuesArrayFast());
    return message;
  }

  @Override
  public T buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return makeSeries(context.fieldValueToObject(DateTimeNumericEncoding.class, message.getByOrdinal(1)), 
                      (int[]) message.getValue(2), (double[]) message.getValue(3));
  }

}
