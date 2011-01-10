/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
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
  public MutableFudgeFieldContainer buildMessage(FudgeSerializationContext context, 
                                                 YearOffsetEpochMillisConverter converter) {
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsg(message, null, 0, converter.getClass().getName());
    context.objectToFudgeMsg(message, null, 1, converter.getTimeZone310());
    context.objectToFudgeMsg(message, null, 2, converter.getZonedOffset().toOffsetDateTime());
    return message;
  }

  @Override
  public YearOffsetEpochMillisConverter buildObject(FudgeDeserializationContext context, 
                                                    FudgeFieldContainer message) {
    TimeZone tz = message.getFieldValue(TimeZone.class, message.getByOrdinal(1));
    OffsetDateTime odt = message.getFieldValue(OffsetDateTime.class, message.getByOrdinal(2));
    return new YearOffsetEpochMillisConverter(odt.atZoneSameInstant(tz));
  }

}
