/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.sqldate.SQLDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for SQLDateEpochDaysConverter.
 */
@FudgeBuilderFor(SQLDateEpochDaysConverter.class)
public class SQLDateEpochDaysConverterFudgeBuilder extends DateTimeConverterFudgeBuilder<SQLDateEpochDaysConverter> {

  @Override
  public SQLDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new SQLDateEpochDaysConverter(timeZone);
  }

}
