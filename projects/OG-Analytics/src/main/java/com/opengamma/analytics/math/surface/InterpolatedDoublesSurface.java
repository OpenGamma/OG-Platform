/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.surface;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator2D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * A surface that is defined by a set of nodal points (i.e. <i>x-y-z</i> data) and an interpolator to return values of <i>z</i> for values
 * of <i>(x, y)</i> that do not lie on nodal <i>(x, y)</i> values.
 */
public class InterpolatedDoublesSurface extends DoublesSurface {

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator);
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyzData, interpolator);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   * @param interpolator The interpolator, not null
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator) {
    return new InterpolatedDoublesSurface(xyzData, interpolator);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator,
      final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator,
      final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  /**
   * @param xData A list of <i>x</i> data points, not null
   * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator,
      final String name) {
    return new InterpolatedDoublesSurface(xData, yData, zData, interpolator, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null
   * @param zData A list of <i>z</i> data points, not null, contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyData, zData, interpolator, name);
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyzData, interpolator, name);
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   * @return An interpolated surface with automatically-generated name
   */
  public static InterpolatedDoublesSurface from(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator, final String name) {
    return new InterpolatedDoublesSurface(xyzData, interpolator, name);
  }

  /**
   * Creates a surface based on a regular grid.
   * @param xVector The x data of the grid. Not null.
   * @param yVector The x data of the grid. Not null.
   * @param zData The z data. Not null. Contains a number of entries which is the product of the number of entries of xVector and yVector.
   * The z data should be in the order of xVector repeated yVector.length times, i.e. x[0]-y[0], x[1]-y[0], x[2]-y[0], ..., x[0]-y[1], x[1], y[1], etc.
   * @param interpolator  The interpolator, not null
   * @return An interpolated surface with automatically-generated name.
   */
  public static InterpolatedDoublesSurface fromGrid(final double[] xVector, final double[] yVector, final double[] zData, final Interpolator2D interpolator) {
    final int nX = xVector.length;
    final int nY = yVector.length;
    ArgumentChecker.isTrue(zData.length == nX * nY, "Sizes not compatible");
    final double[] xData = new double[nX * nY];
    final double[] yData = new double[nX * nY];
    for (int loopy = 0; loopy < nY; loopy++) {
      System.arraycopy(xVector, 0, xData, loopy * nX, nX);
    }
    for (int loopx = 0; loopx < nX; loopx++) {
      for (int loopy = 0; loopy < nY; loopy++) {
        yData[loopx + loopy * nX] = yVector[loopy];
      }
    }
    return from(xData, yData, zData, interpolator);
  }

  private final GridInterpolator2D _interpolator;
  private Map<Double, Interpolator1DDataBundle> _data;

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xData An array of <i>x</i> data points, not null, no null elements
   * @param yData An array of <i>y</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, not null elements. Contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xData A list of <i>x</i> data points, not null, no null elements
   * @param yData A list of <i>y</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator) {
    super(xData, yData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null, no null elements
   * @param zData An array of <i>z</i> data points, not null. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null, no null elements
   * @param zData An array of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null, no null elements
   * @param zData A list of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator) {
    super(xyData, zData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null, no null elements
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator) {
    super(xyzData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null, no null elements
   * @param interpolator The interpolator, not null
   */
  public InterpolatedDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator) {
    super(xyzData);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xData An array of <i>x</i> data points, not null
   * @param yData An array of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final double[] xData, final double[] yData, final double[] zData, final Interpolator2D interpolator, final String name) {
    super(xData, yData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xData An array of <i>x</i> data points, not null, no null elements
   * @param yData An array of <i>y</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param zData An array of <i>z</i> data points, not null, not null elements. Contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final Double[] xData, final Double[] yData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    super(xData, yData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xData A list of <i>x</i> data points, not null, no null elements
   * @param yData A list of <i>y</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param zData A list of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final List<Double> xData, final List<Double> yData, final List<Double> zData, final Interpolator2D interpolator,
      final String name) {
    super(xData, yData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null, no null elements
   * @param zData An array of <i>z</i> data points, not null. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final double[] zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData An array of <i>x-y</i> data points, not null, no null elements
   * @param zData An array of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final DoublesPair[] xyData, final Double[] zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyData A list of <i>x-y</i> data points, not null, no null elements
   * @param zData A list of <i>z</i> data points, not null, no null elements. Contains same number of entries as <i>x-y</i>
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final List<DoublesPair> xyData, final List<Double> zData, final Interpolator2D interpolator, final String name) {
    super(xyData, zData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyzData A map of <i>x-y</i> data points to <i>z</i> data points, not null, no null elements
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final Map<DoublesPair, Double> xyzData, final Interpolator2D interpolator, final String name) {
    super(xyzData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  /**
   * @param xyzData A list of <i>x-y-z</i> data points, not null, no null elements
   * @param interpolator The interpolator, not null
   * @param name The name of the surface
   */
  public InterpolatedDoublesSurface(final List<Triple<Double, Double, Double>> xyzData, final Interpolator2D interpolator, final String name) {
    super(xyzData, name);
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.isTrue(interpolator instanceof GridInterpolator2D, "Interpolator must be a GridInterpolator2D"); //TODO remove me
    _interpolator = (GridInterpolator2D) interpolator;
    init();
  }

  // TODO this logic should be in the interpolator
  private void init() {
    final double[] x = getXDataAsPrimitive();
    final double[] y = getYDataAsPrimitive();
    final double[] z = getZDataAsPrimitive();
    
    _data = _interpolator.getDataBundleFromUnsorted(x, y, z, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Double x, final Double y) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(y, "y");
    return _interpolator.interpolate(_data, DoublesPair.of(x.doubleValue(), y.doubleValue()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Double getZValue(final Pair<Double, Double> xy) {
    ArgumentChecker.notNull(xy, "xy");
    return _interpolator.interpolate(_data, DoublesPair.of(xy));
  }

  /**
   * @return The interpolator
   */
  public Interpolator2D getInterpolator() {
    return _interpolator;
  }

  public Map<Double, ? extends Interpolator1DDataBundle> getInterpolatorData() {
    return _data;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    final InterpolatedDoublesSurface other = (InterpolatedDoublesSurface) obj;
    return ObjectUtils.equals(_interpolator, other._interpolator);
  }

}
