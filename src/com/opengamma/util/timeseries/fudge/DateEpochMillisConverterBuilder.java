package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import com.opengamma.util.timeseries.date.time.DateEpochMillisConverter;


/**
 * Fudge message builder (serializer/deserializer) for DateEpochMillisConverter
 */
public class DateEpochMillisConverterBuilder extends DateTimeConverterBuilder<DateEpochMillisConverter> {
  @Override
  public DateEpochMillisConverter makeConverter(TimeZone timeZone) {
    return new DateEpochMillisConverter(timeZone);
  }
}
