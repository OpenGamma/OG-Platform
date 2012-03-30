/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 * Converter for {@link LocalDateDoubleTimeSeries} results.
 */
public class LocalDateDoubleTimeSeriesConverter implements ResultConverter<LocalDateDoubleTimeSeries> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LocalDateDoubleTimeSeries value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    Map<String, Object> summary = ImmutableMap.<String, Object>of(
        "from", value.getEarliestTime().toLocalDate().toString(),
        "to", value.getLatestTime().toLocalDate().toString());
    result.put("summary", summary);
    if (mode == ConversionMode.FULL) {
      ZonedDateTimeDoubleTimeSeries zonedTimeSeries = value.toZonedDateTimeDoubleTimeSeries();
      Object[] tsData = new Object[zonedTimeSeries.size()];
      for (int i = 0; i < zonedTimeSeries.size(); i++) {
        ZonedDateTime time = zonedTimeSeries.getTimeAt(i);
        double tsValue = zonedTimeSeries.getValueAt(i);
        tsData[i] = new Object[] {time.toInstant().toEpochMillisLong(), tsValue};
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
