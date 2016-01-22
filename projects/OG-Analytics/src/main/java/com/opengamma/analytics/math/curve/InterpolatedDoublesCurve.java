/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * A curve that is defined by a set of nodal points (i.e. <i>x-y</i> data) and an interpolator
 * to return values of <i>y</i> for values of <i>x</i> that do not lie on nodal <i>x</i> values. 
 */
@BeanDefinition
public class InterpolatedDoublesCurve extends ArraysDoublesCurve implements Serializable {

  /**
   * The data bundle.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private Interpolator1DDataBundle _dataBundle;
  /**
   * The interpolator.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private Interpolator1D _interpolator;

  //-------------------------------------------------------------------------
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

  //-------------------------------------------------------------------------
  /**
   * Constructor for Joda-Beans.
   */
  protected InterpolatedDoublesCurve() {
  }

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
  public InterpolatedDoublesCurve(final List<Double> xData, final List<Double> yData, final Interpolator1D interpolator, final boolean isSorted, final String name) {
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
    ArgumentChecker.notNull(interpolator, "interpolator");
    //  Validate.isTrue(size() >= 2);
    _dataBundle = interpolator.getDataBundleFromSortedArrays(getXDataAsPrimitive(), getYDataAsPrimitive());
    _interpolator = interpolator;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getYValue(final Double x) {
    ArgumentChecker.notNull(x, "x");
    return _interpolator.interpolate(_dataBundle, x);
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    ArgumentChecker.notNull(x, "x");
    return ArrayUtils.toObject(_interpolator.getNodeSensitivitiesForValue(_dataBundle, x));
  }

  public Interpolator1D getInterpolator() {
    return _interpolator;
  }

  public Interpolator1DDataBundle getDataBundle() {
    return _dataBundle;
  }

  @Override
  public double getDyDx(final double x) {
    return _interpolator.firstDerivative(_dataBundle, x);
  }

  //-------------------------------------------------------------------------
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _dataBundle.hashCode();
    result = prime * result + _interpolator.hashCode();
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code InterpolatedDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static InterpolatedDoublesCurve.Meta meta() {
    return InterpolatedDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(InterpolatedDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public InterpolatedDoublesCurve.Meta metaBean() {
    return InterpolatedDoublesCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the data bundle.
   * @param dataBundle  the new value of the property, not null
   */
  private void setDataBundle(Interpolator1DDataBundle dataBundle) {
    JodaBeanUtils.notNull(dataBundle, "dataBundle");
    this._dataBundle = dataBundle;
  }

  /**
   * Gets the the {@code dataBundle} property.
   * @return the property, not null
   */
  public final Property<Interpolator1DDataBundle> dataBundle() {
    return metaBean().dataBundle().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the interpolator.
   * @param interpolator  the new value of the property, not null
   */
  private void setInterpolator(Interpolator1D interpolator) {
    JodaBeanUtils.notNull(interpolator, "interpolator");
    this._interpolator = interpolator;
  }

  /**
   * Gets the the {@code interpolator} property.
   * @return the property, not null
   */
  public final Property<Interpolator1D> interpolator() {
    return metaBean().interpolator().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public InterpolatedDoublesCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("InterpolatedDoublesCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("dataBundle").append('=').append(JodaBeanUtils.toString(getDataBundle())).append(',').append(' ');
    buf.append("interpolator").append('=').append(JodaBeanUtils.toString(getInterpolator())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code InterpolatedDoublesCurve}.
   */
  public static class Meta extends ArraysDoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code dataBundle} property.
     */
    private final MetaProperty<Interpolator1DDataBundle> _dataBundle = DirectMetaProperty.ofReadWrite(
        this, "dataBundle", InterpolatedDoublesCurve.class, Interpolator1DDataBundle.class);
    /**
     * The meta-property for the {@code interpolator} property.
     */
    private final MetaProperty<Interpolator1D> _interpolator = DirectMetaProperty.ofReadWrite(
        this, "interpolator", InterpolatedDoublesCurve.class, Interpolator1D.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "dataBundle",
        "interpolator");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 791094476:  // dataBundle
          return _dataBundle;
        case 2096253127:  // interpolator
          return _interpolator;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends InterpolatedDoublesCurve> builder() {
      return new DirectBeanBuilder<InterpolatedDoublesCurve>(new InterpolatedDoublesCurve());
    }

    @Override
    public Class<? extends InterpolatedDoublesCurve> beanType() {
      return InterpolatedDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code dataBundle} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Interpolator1DDataBundle> dataBundle() {
      return _dataBundle;
    }

    /**
     * The meta-property for the {@code interpolator} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Interpolator1D> interpolator() {
      return _interpolator;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 791094476:  // dataBundle
          return ((InterpolatedDoublesCurve) bean).getDataBundle();
        case 2096253127:  // interpolator
          return ((InterpolatedDoublesCurve) bean).getInterpolator();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 791094476:  // dataBundle
          ((InterpolatedDoublesCurve) bean).setDataBundle((Interpolator1DDataBundle) newValue);
          return;
        case 2096253127:  // interpolator
          ((InterpolatedDoublesCurve) bean).setInterpolator((Interpolator1D) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((InterpolatedDoublesCurve) bean)._dataBundle, "dataBundle");
      JodaBeanUtils.notNull(((InterpolatedDoublesCurve) bean)._interpolator, "interpolator");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
