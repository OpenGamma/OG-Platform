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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, T object) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, null, 0, object.getClass().getName()); // we need to stick the class name in so receiver knows.
    context.objectToFudgeMsg(message, null, 1, object.getConverter());
    context.objectToFudgeMsg(message, null, 2, object.getFastSeries());
    return message;
  }
  
  @SuppressWarnings("unchecked")
  @Override 
  public T buildObject(FudgeDeserializationContext context, FudgeFieldContainer message) {
    return makeSeries((DateTimeConverter<E>) context.fieldValueToObject(message.getByOrdinal(1)), (FastTimeSeries<E>) context.fieldValueToObject(message.getByOrdinal(2)));
  }

}
