/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.date.DateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for DateEpochDaysConverter
 */
@FudgeBuilderFor(DateEpochDaysConverter.class)
public class DateEpochDaysConverterBuilder extends DateTimeConverterBuilder<DateEpochDaysConverter> {

  @Override
  public DateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new DateEpochDaysConverter(timeZone);
  }

}
