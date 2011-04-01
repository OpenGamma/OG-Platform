/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.timeseries.yearoffset.YearOffsetEpochMillisConverter;

/**
 * Fudge message builder (serializer/deserializer) for YearOffsetEpochMillisConverter.
 */
@FudgeBuilderFor(YearOffsetEpochMillisConverter.class)
public class YearOffsetEpochMillisConverterBuilder implements FudgeBuilder<YearOffsetEpochMillisConverter> {

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, 
                                                 YearOffsetEpochMillisConverter converter) {
    final MutableFudgeMsg message = context.newMessage();
    context.objectToFudgeMsg(message, null, 0, converter.getClass().getName());
    context.objectToFudgeMsg(message, null, 1, converter.getTimeZone310());
    context.objectToFudgeMsg(message, null, 2, converter.getZonedOffset().toOffsetDateTime());
    return message;
  }

  @Override
  public YearOffsetEpochMillisConverter buildObject(FudgeDeserializationContext context, 
                                                    FudgeMsg message) {
    TimeZone tz = message.getFieldValue(TimeZone.class, message.getByOrdinal(1));
    OffsetDateTime odt = message.getFieldValue(OffsetDateTime.class, message.getByOrdinal(2));
    return new YearOffsetEpochMillisConverter(odt.atZoneSameInstant(tz));
  }

}
