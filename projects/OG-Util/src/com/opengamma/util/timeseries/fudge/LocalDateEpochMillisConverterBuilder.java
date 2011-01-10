/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.fudge;

import java.util.TimeZone;

import org.fudgemsg.mapping.FudgeBuilderFor;

import com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for LocalDateEpochMillisConverter.
 */
@FudgeBuilderFor(LocalDateEpochDaysConverter.class)
public class LocalDateEpochMillisConverterBuilder extends DateTimeConverterBuilder<LocalDateEpochDaysConverter> {
  @Override
  public LocalDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new LocalDateEpochDaysConverter(timeZone);
  }
}
