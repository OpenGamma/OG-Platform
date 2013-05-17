/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Specialization of {@code NodalObjectsCurve} with Tenor and Double.
 */
public class NodalTenorDoubleCurve extends NodalObjectsCurve<Tenor, Double> {

  public static NodalTenorDoubleCurve from(final Tenor[] xData, final Double[] yData) {
    return new NodalTenorDoubleCurve(xData, yData, false);
  }

  public static NodalTenorDoubleCurve from(final Tenor[] xData, final Double[] yData, final String name) {
    return new NodalTenorDoubleCurve(xData, yData, false, name);
  }

  public static NodalTenorDoubleCurve fromSorted(final Tenor[] xData, final Double[] yData) {
    return new NodalTenorDoubleCurve(xData, yData, true);
  }

  public static NodalTenorDoubleCurve fromSorted(final Tenor[] xData, final Double[] yData, final String name) {
    return new NodalTenorDoubleCurve(xData, yData, true, name);
  }

  public NodalTenorDoubleCurve(List<Tenor> xData, List<Double> yData, boolean isSorted, String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalTenorDoubleCurve(List<Tenor> xData, List<Double> yData, boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalTenorDoubleCurve(Map<Tenor, Double> data, boolean isSorted, String name) {
    super(data, isSorted, name);
  }

  public NodalTenorDoubleCurve(Map<Tenor, Double> data, boolean isSorted) {
    super(data, isSorted);
  }

  public NodalTenorDoubleCurve(Set<Pair<Tenor, Double>> data, boolean isSorted, String name) {
    super(data, isSorted, name);
  }

  public NodalTenorDoubleCurve(Set<Pair<Tenor, Double>> data, boolean isSorted) {
    super(data, isSorted);
  }

  public NodalTenorDoubleCurve(Tenor[] xData, Double[] yData, boolean isSorted, String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalTenorDoubleCurve(Tenor[] xData, Double[] yData, boolean isSorted) {
    super(xData, yData, isSorted);
  }

}
