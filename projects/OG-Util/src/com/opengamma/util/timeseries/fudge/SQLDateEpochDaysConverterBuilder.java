package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.sqldate.SQLDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for SQLDateEpochDaysConverter.
 */
@FudgeBuilderFor(SQLDateEpochDaysConverter.class)
public class SQLDateEpochDaysConverterBuilder extends DateTimeConverterBuilder<SQLDateEpochDaysConverter> {

  @Override
  public SQLDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new SQLDateEpochDaysConverter(timeZone);
  }

}
