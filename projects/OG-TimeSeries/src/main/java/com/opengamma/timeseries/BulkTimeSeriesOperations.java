/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.timeseries;

import java.util.Arrays;
import java.util.List;

/**
 * Bulk operations on time-series.
 */
public class BulkTimeSeriesOperations {

  /**
   * Calculates the intersection of the input time-series.
   * <p>
   * Earlier time-series takes precedence over later ones.
   * 
   * @param <E>  the series type
   * @param inputs  the input series, not null
   * @return the output series, not null
   */
  @SuppressWarnings("unchecked")
  public static <E> DoubleTimeSeries<E>[] intersection(DoubleTimeSeries<E>[] inputs) {
    DoubleTimeSeries<E>[] results = new DoubleTimeSeries[inputs.length];
    if (inputs.length < 2) {
      for (int i = 0; i < inputs.length; i++) {
        results[i] = (DoubleTimeSeries<E>) inputs[i].newInstance(inputs[i].timesArray(), inputs[i].valuesArray());
      }
      return results;
    }
    DoubleTimeSeries<E> intersection = inputs[0];
    for (int i = 1; i < inputs.length; i++) {
      intersection = intersection.intersectionFirstValue(inputs[i]);
    }
    for (int i = 0; i < inputs.length; i++) {
      results[i] = inputs[i].intersectionFirstValue(intersection);
    }
    return results;
  }

  /**
   * Calculates the intersection of the input time-series.
   * <p>
   * Earlier time-series takes precedence over later ones.
   * 
   * @param <E>  the series type
   * @param inputs  the input series, not null
   * @return the output series, not null
   */
  @SuppressWarnings("unchecked")
  public <E> List<DoubleTimeSeries<E>> intersection(List<DoubleTimeSeries<E>> inputs) {
    return Arrays.asList(intersection((DoubleTimeSeries<E>[]) inputs.toArray()));
  }

}
