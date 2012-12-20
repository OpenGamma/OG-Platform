/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.TimeZone;

import com.opengamma.util.timeseries.fudge.DateTimeConverterFudgeBuilder;

/**
 * Fudge message builder (serializer/deserializer) for ZonedDateTimeEpochMillisConverter
 */
public class ZonedDateTimeEpochMillisConverterBuilder extends DateTimeConverterFudgeBuilder<ZonedDateTimeEpochMillisConverter> {
  @Override
  public ZonedDateTimeEpochMillisConverter makeConverter(TimeZone timeZone) {
    return new ZonedDateTimeEpochMillisConverter(timeZone);
  }
}
