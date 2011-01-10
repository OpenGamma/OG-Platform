/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * Fudge message builder (serializer/deserializer) for ZonedDateTimeEpochMillisConverter.
 */
@FudgeBuilderFor(ZonedDateTimeEpochMillisConverter.class)
public class ZonedDateTimeEpochMillisConverterBuilder extends DateTimeConverterBuilder<ZonedDateTimeEpochMillisConverter> {

  @Override
  public ZonedDateTimeEpochMillisConverter makeConverter(TimeZone timeZone) {
    return new ZonedDateTimeEpochMillisConverter(timeZone);
  }

}
