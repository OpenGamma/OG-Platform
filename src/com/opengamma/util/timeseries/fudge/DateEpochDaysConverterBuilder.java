package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import com.opengamma.util.timeseries.date.DateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for DateEpochDaysConverter
 */
public class DateEpochDaysConverterBuilder extends DateTimeConverterBuilder<DateEpochDaysConverter> {

  @Override
  public DateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new DateEpochDaysConverter(timeZone);
  }

}
