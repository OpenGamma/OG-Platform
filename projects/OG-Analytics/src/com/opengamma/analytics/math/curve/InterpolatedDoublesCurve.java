/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A curve that is defined by a set of nodal points (i.e. <i>x-y</i> data) and an interpolator to return values of <i>y</i> for values 
 * of <i>x</i> that do not lie on nodal <i>x</i> values. 
 */
public class InterpolatedDoublesCurve extends DoublesCurve {

  /**
   * 
   * @param xData An array of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final double[] xData, final double[] yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Double[] xData, final Double[] yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }
  
  /**
   * 
   * @param data A map of <i>x-y</i> data points, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Map<Double, Double> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final DoublesPair[] data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Set<DoublesPair> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  /**
   * 
   * @param xData A list of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final List<DoublesPair> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, false);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final double[] xData, final double[] yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Double[] xData, final Double[] yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }
  
  /**
   * 
   * @param data A map of <i>x-y</i> data points, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Map<Double, Double> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final DoublesPair[] data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final Set<DoublesPair> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }

  /**
   * 
   * @param xData A list of <i>x</i> data points, not null, contains at least 2 data points
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, false, name);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null, contains same number of entries as <i>x</i>
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve from(final List<DoublesPair> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, false, name);
  }
  
  /**
   * 
   * @param xData An array of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final double[] xData, final double[] yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Double[] xData, final Double[] yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data points, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Map<Double, Double> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> data points, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final DoublesPair[] data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  /**
   * 
   * @param data A set of <i>x-y</i> data points, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Set<DoublesPair> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  /**
   * 
   * @param xData A list of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> pairs, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final List<DoublesPair> data, final Interpolator1D interpolator) {
    return new InterpolatedDoublesCurve(data, interpolator, true);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final double[] xData, final double[] yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Double[] xData, final Double[] yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data points, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Map<Double, Double> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> pairs, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final DoublesPair[] data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }
  
  /**
   * 
   * @param data A set of <i>x-y</i> pairs, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final Set<DoublesPair> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(xData, yData, interpolator, true, name);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> data points, assumed to be sorted ascending in <i>x</i>, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param name The name of the curve
   * @return An interpolated curve with automatically-generated name
   */
  public static InterpolatedDoublesCurve fromSorted(final List<DoublesPair> data, final Interpolator1D interpolator, final String name) {
    return new InterpolatedDoublesCurve(data, interpolator, true, name);
  }

  private Interpolator1DDataBundle _dataBundle;
  private Interpolator1D _interpolator;

  /**
   * 
   * @param xData An array of <i>x</i> data, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final double[] xData, final double[] yData, final Interpolator1D interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final Double[] xData, final Double[] yData, final Interpolator1D interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final Map<Double, Double> data, final Interpolator1D interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final DoublesPair[] data, final Interpolator1D interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final Set<DoublesPair> data, final Interpolator1D interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param xData A list of <i>x</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator, final boolean isSorted) {
    super(xData, yData, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> data points, assumed to be sorted ascending, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   */
  public InterpolatedDoublesCurve(final List<DoublesPair> data, final Interpolator1D interpolator, final boolean isSorted) {
    super(data, isSorted);
    init(interpolator);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final double[] xData, final double[] yData, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param xData An array of <i>x</i> data, not null, contains at least 2 data points
   * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final Double[] xData, final Double[] yData, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param data A map of <i>x-y</i> data, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final Map<Double, Double> data, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param data An array of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final DoublesPair[] data, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param data A set of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final Set<DoublesPair> data, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param xData A list of <i>x</i> data, not null, contains at least 2 data points
   * @param yData A list of <i>y</i> data, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator, final boolean isSorted, 
      final String name) {
    super(xData, yData, isSorted, name);
    init(interpolator);
  }

  /**
   * 
   * @param data A list of <i>x-y</i> pairs, not null, contains at least 2 data points
   * @param interpolator The interpolator, not null
   * @param isSorted Is the <i>x</i>-data sorted
   * @param name The name of the curve
   */
  public InterpolatedDoublesCurve(final List<DoublesPair> data, final Interpolator1D interpolator, final boolean isSorted, final String name) {
    super(data, isSorted, name);
    init(interpolator);
  }

  private void init(final Interpolator1D interpolator) {
    Validate.notNull(interpolator, "interpolator");
    Validate.isTrue(size() >= 2);
    _dataBundle = interpolator.getDataBundleFromSortedArrays(getXDataAsPrimitive(), getYDataAsPrimitive());
    _interpolator = interpolator;
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _interpolator.interpolate(_dataBundle, x);
  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  public Interpolator1DDataBundle getDataBundle() {
    return _dataBundle;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _dataBundle.hashCode();
    result = prime * result + _interpolator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterpolatedDoublesCurve other = (InterpolatedDoublesCurve) obj;
    return ObjectUtils.equals(_dataBundle, other._dataBundle) && ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
