package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter;


/**
 * Fudge message builder (serializer/deserializer) for LocalDateEpochMillisConverter
 */
public class LocalDateEpochMillisConverterBuilder extends DateTimeConverterBuilder<LocalDateEpochDaysConverter> {
  @Override
  public LocalDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new LocalDateEpochDaysConverter(timeZone);
  }
}
