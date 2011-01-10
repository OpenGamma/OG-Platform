/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.TimeZone;

import com.opengamma.util.timeseries.fudge.DateTimeConverterBuilder;
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
