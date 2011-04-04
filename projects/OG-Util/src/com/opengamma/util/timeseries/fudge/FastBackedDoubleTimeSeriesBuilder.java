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

import com.opengamma.util.timeseries.DateTimeConverter;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.FastTimeSeries;

/**
 * @param <T> class of concrete time series implementation
 * @param <E> class of underlying type used to represent date/times
 * @author jim
 */
public abstract class FastBackedDoubleTimeSeriesBuilder<E, T extends FastBackedDoubleTimeSeries<E>> implements FudgeBuilder<T> {
  public abstract T makeSeries(DateTimeConverter<E> converter, FastTimeSeries<?> dts);
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, T object) {
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, null, 0, object.getClass().getName()); // we need to stick the class name in so receiver knows.
    context.addToMessage(message, null, 1, object.getConverter());
    context.addToMessage(message, null, 2, object.getFastSeries());
    return message;
  }
  
  @SuppressWarnings("unchecked")
  @Override 
  public T buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    return makeSeries((DateTimeConverter<E>) context.fieldValueToObject(message.getByOrdinal(1)), (FastTimeSeries<E>) context.fieldValueToObject(message.getByOrdinal(2)));
  }

}
