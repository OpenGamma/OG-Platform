/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.zoneddatetime;

import java.util.TimeZone;

import com.opengamma.util.timeseries.fudge.DateTimeConverterBuilder;
import com.opengamma.util.timeseries.localdate.LocalDateEpochDaysConverter;

/**
 * Fudge message builder (serializer/deserializer) for LocalDateEpochDaysConverter.
 */
public class LocalDateEpochDaysConverterBuilder extends DateTimeConverterBuilder<LocalDateEpochDaysConverter> {
  @Override
  public LocalDateEpochDaysConverter makeConverter(TimeZone timeZone) {
    return new LocalDateEpochDaysConverter(timeZone);
  }
}
