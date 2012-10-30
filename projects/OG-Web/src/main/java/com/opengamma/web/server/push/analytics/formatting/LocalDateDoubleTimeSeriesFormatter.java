/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeDoubleTimeSeries;

/**
 *
 */
/* package */ class LocalDateDoubleTimeSeriesFormatter extends NoHistoryFormatter<LocalDateDoubleTimeSeries> {

  @Override
  public String formatForDisplay(LocalDateDoubleTimeSeries timeSeries, ValueSpecification valueSpec) {
    return "Time-series (" + timeSeries.getEarliestTime().toLocalDate() + " to " + timeSeries.getLatestTime().toLocalDate() + ")";
  }

  @Override
  public Map<String, Object> formatForExpandedDisplay(LocalDateDoubleTimeSeries value, ValueSpecification valueSpec) {
    ZonedDateTimeDoubleTimeSeries series = value.toZonedDateTimeDoubleTimeSeries();
    List<Object[]> data = Lists.newArrayListWithCapacity(series.size());
    for (Map.Entry<ZonedDateTime, Double> entry : series) {
      long timeMillis = entry.getKey().toInstant().toEpochMillisLong();
      Double vol = entry.getValue();
      data.add(new Object[]{timeMillis, vol});
    }
    Map<String, String> templateData = ImmutableMap.of("data_field", "Historical Time Series",
                                                       "observation_time", "Historical Time Series");
    Map<String, Object> timeSeries = ImmutableMap.of("fieldLabels", new String[]{"Time", "Value"},
                                                     "data", data);
    return ImmutableMap.<String, Object>of("template_data", templateData, "timeseries", timeSeries);
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.TIME_SERIES;
  }
}
