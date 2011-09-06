/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.timeseries.DateTimeConverter;

/**
 * @param <T> type of concrete subclass of DateTimeConverter to build
 */
public abstract class DateTimeConverterFudgeBuilder<T extends DateTimeConverter<?>> implements FudgeBuilder<T> {
  public abstract T makeConverter(TimeZone timeZone);
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, T converter) {
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessage(message, null, 0, converter.getClass().getName());
    serializer.addToMessage(message, null, 1, converter.getTimeZone());
    return message;
  }
  
  @Override
  public T buildObject(FudgeDeserializer deserializer, FudgeMsg message) {
    return makeConverter(message.getFieldValue(TimeZone.class, message.getByOrdinal(1)));
  }
}
