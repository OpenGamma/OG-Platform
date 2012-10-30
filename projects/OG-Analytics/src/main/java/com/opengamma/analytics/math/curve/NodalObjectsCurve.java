/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> Type of the x data
 * @param <U> Type of the y data
 */
public class NodalObjectsCurve<T extends Comparable<T>, U> extends ObjectsCurve<T, U> {

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final T[] xData, final U[] yData) {
    return new NodalObjectsCurve<T, U>(xData, yData, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Map<T, U> data) {
    return new NodalObjectsCurve<T, U>(data, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Set<Pair<T, U>> data) {
    return new NodalObjectsCurve<T, U>(data, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final List<T> xData, final List<U> yData) {
    return new NodalObjectsCurve<T, U>(xData, yData, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final T[] xData, final U[] yData, final String name) {
    return new NodalObjectsCurve<T, U>(xData, yData, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Map<T, U> data, final String name) {
    return new NodalObjectsCurve<T, U>(data, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Set<Pair<T, U>> data, final String name) {
    return new NodalObjectsCurve<T, U>(data, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final List<T> xData, final List<U> yData, final String name) {
    return new NodalObjectsCurve<T, U>(xData, yData, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final T[] xData, final U[] yData) {
    return new NodalObjectsCurve<T, U>(xData, yData, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Map<T, U> data) {
    return new NodalObjectsCurve<T, U>(data, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Set<Pair<T, U>> data) {
    return new NodalObjectsCurve<T, U>(data, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final List<T> xData, final List<U> yData) {
    return new NodalObjectsCurve<T, U>(xData, yData, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final T[] xData, final U[] yData, final String name) {
    return new NodalObjectsCurve<T, U>(xData, yData, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Map<T, U> data, final String name) {
    return new NodalObjectsCurve<T, U>(data, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Set<Pair<T, U>> data, final String name) {
    return new NodalObjectsCurve<T, U>(data, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final List<T> xData, final List<U> yData, final String name) {
    return new NodalObjectsCurve<T, U>(xData, yData, true, name);
  }

  public NodalObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalObjectsCurve(final Map<T, U> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalObjectsCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  @Override
  public U getYValue(final T x) {
    Validate.notNull(x, "x");
    final int index = Arrays.binarySearch(getXData(), x);
    if (index < 0) {
      throw new IllegalArgumentException("Curve does not contain data for x point " + x);
    }
    return getYData()[index];
  }

}
