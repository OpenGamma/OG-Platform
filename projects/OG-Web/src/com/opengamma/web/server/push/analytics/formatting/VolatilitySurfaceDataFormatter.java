/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.time.calendar.LocalDate;

import org.apache.commons.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.time.Tenor;
import com.opengamma.web.server.conversion.LabelFormatter;

/* package */ class VolatilitySurfaceDataFormatter extends NoHistoryFormatter<VolatilitySurfaceData> {

  @Override
  public String formatForDisplay(VolatilitySurfaceData value, ValueSpecification valueSpec) {
    return "Volatility Surface (" + value.getXs().length + " x " + value.getYs().length + ")";
  }

  /**
   * Returns a map containing the x-axis labels, y-axis labels and volatility values. The lists of axis labels
   * are sorted and have no duplicate values (which isn't necessarily true of the underlying data). The volatility
   * data list contains a value for every combination of x and y values. If there is no corresponding value in the
   * underlying data the volatility value will be {@code null}.
   * @return {x_labels: [...], y_labels: [...], vol: [x0y0, x1y0,... , x0y1, x1y1,...]}
   */
  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> formatForExpandedDisplay(VolatilitySurfaceData value, ValueSpecification valueSpec) {
    Map<String, Object> results = Maps.newHashMap();
    // the x and y values won't necessarily be unique and won't necessarily map to a rectangular grid
    // this projects them onto a grid and inserts nulls where there's no data available
    SortedSet xVals = value.getUniqueXValues();
    SortedSet yVals = Sets.newTreeSet((Iterable) Arrays.asList(value.getYs()));
    List<String> xLabels = getAxisLabels(xVals);
    List<String> yLabels = getAxisLabels(yVals);
    // numeric values corresponding to the axis labels to help with plotting the surface
    List<Object> xAxisValues = Lists.newArrayListWithCapacity(xVals.size());
    List<Object> yAxisValues = Lists.newArrayListWithCapacity(yVals.size());
    List<Double> vol = Lists.newArrayListWithCapacity(xVals.size() * yVals.size());
    for (Object yVal : yVals) {
      for (Object xVal : xVals) {
        vol.add(value.getVolatility(xVal, yVal));
      }
      CollectionUtils.addIgnoreNull(yAxisValues, getAxisValue(yVal));
    }
    for (Object xVal : xVals) {
      CollectionUtils.addIgnoreNull(xAxisValues, getAxisValue(xVal));
    }
    // not all volatility surfaces can be sensibly plotted. if a value can't be meaningfully converted to a number
    // then it will be null and won't be added to the collection. so if the collection is empty it isn't added to the
    // results and a plot won't be generated
    if (!yAxisValues.isEmpty()) {
      results.put("y_values", yAxisValues);
    }
    if (!xAxisValues.isEmpty()) {
      results.put("x_values", xAxisValues);
    }
    results.put("x_labels", xLabels);
    results.put("y_labels", yLabels);
    results.put("vol", vol);
    return results;
  }

  private List<String> getAxisLabels(Collection values) {
    List<String> labels = Lists.newArrayListWithCapacity(values.size());
    for (Object value : values) {
      labels.add(LabelFormatter.format(value));
    }
    return labels;
  }

  /**
   * Returns a numeric value corresponding to a point on volatility surface axis to help with plotting the surface.
   * @param axisValue A point on the axis
   * @return A numeric value corresponding to the value or null if there's no meaningful numeric value
   */
  private Object getAxisValue(Object axisValue) {
    if (axisValue instanceof Number) {
      return axisValue;
    } else if (axisValue instanceof LocalDate) {
      return ((LocalDate) axisValue).toEpochDays();
    } else if (axisValue instanceof Tenor) {
      return ((Tenor) axisValue).getPeriod().toEstimatedDuration().toSeconds();
    }
    return null;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.SURFACE_DATA;
  }
}
