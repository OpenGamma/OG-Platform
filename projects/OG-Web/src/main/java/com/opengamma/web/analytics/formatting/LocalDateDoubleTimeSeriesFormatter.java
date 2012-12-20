/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

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
/* package */ class LocalDateDoubleTimeSeriesFormatter extends AbstractFormatter<LocalDateDoubleTimeSeries> {

  /* package */ LocalDateDoubleTimeSeriesFormatter() {
    super(LocalDateDoubleTimeSeries.class);
    addFormatter(new Formatter<LocalDateDoubleTimeSeries>(Format.EXPANDED) {
      @Override
      Object format(LocalDateDoubleTimeSeries value, ValueSpecification valueSpec) {
        return formatExpanded(value);
      }
    });

  }

  @Override
  public String formatCell(LocalDateDoubleTimeSeries timeSeries, ValueSpecification valueSpec) {
    return "Time-series (" + timeSeries.getEarliestTime().toLocalDate() + " to " + timeSeries.getLatestTime().toLocalDate() + ")";
  }

  private Map<String, Object> formatExpanded(LocalDateDoubleTimeSeries value) {
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
  public DataType getDataType() {
    return DataType.TIME_SERIES;
  }
}
