/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Converter for {@link LocalDateDoubleTimeSeries} results.
 */
public class LocalDateDoubleTimeSeriesConverter implements ResultConverter<LocalDateDoubleTimeSeries> {

  /** The number of milliseconds per day. */
  private static final long MILLIS_PER_DAY = 86400L * 1000;

  /* package */static Object convertForDisplayImpl(ResultConverterCache context, ValueSpecification valueSpec, LocalDateDoubleTimeSeries series, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    if (series.isEmpty()) {
      return result;
    }
    Map<String, Object> summary = ImmutableMap.<String, Object>of(
        "from", series.getEarliestTime().toString(),
        "to", series.getLatestTime().toString());
    result.put("summary", summary);
    if (mode == ConversionMode.FULL) {
      Object[] tsData = new Object[series.size()];
      for (int i = 0; i < series.size(); i++) {
        LocalDate date = series.getTimeAtIndex(i);
        long epochMillis = date.toEpochDay() * MILLIS_PER_DAY;
        tsData[i] = new Object[]{epochMillis, series.getValueAtIndex(i)};
      }
      Map<String, Object> ts = ImmutableMap.<String, Object>of(
          "template_data", ImmutableMap.<String, Object>of(
              "data_field", valueSpec.getValueName(),
              "observation_time", valueSpec.getValueName()),
          "timeseries", ImmutableMap.<String, Object>of(
              "fieldLabels", new String[] {"Time", "Value"},
              "data", tsData));
      result.put("ts", ts);
    }
    return result;
  }

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LocalDateDoubleTimeSeries value, ConversionMode mode) {
    return convertForDisplayImpl(context, valueSpec, value, mode);
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, LocalDateDoubleTimeSeries value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, LocalDateDoubleTimeSeries value) {
    return value.toString();
  }

  @Override
  public String getFormatterName() {
    return "TIME_SERIES";
  }

}
