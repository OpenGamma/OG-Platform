package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import com.opengamma.util.timeseries.sqldate.SQLDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for SQLDateEpochDaysConverter
 */
public class SQLDateEpochDaysConverterBuilder extends DateTimeConverterBuilder<SQLDateEpochDaysConverter> {

  @Override
  public SQLDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new SQLDateEpochDaysConverter(timeZone);
  }

}
