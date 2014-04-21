/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.Map;

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
import com.opengamma.util.ArgumentChecker;

/**
 * Defines a constant curve (i.e. a curve with <i>y = constant</i>).
 */
@BeanDefinition
public class ConstantDoublesCurve
    extends DoublesCurve {

  /**
   * The constant value of the curve.
   */
  @PropertyDefinition(get = "private", set = "private")
  private double _y;

  /**
   * Creates an instance specifying the <i>y</i> level of the curve.
   * 
   * @param y  the level of the curve
   * @return a constant curve with automatically-generated name, not null
   */
  public static ConstantDoublesCurve from(final double y) {
    return new ConstantDoublesCurve(y);
  }

  /**
   * Creates an instance specifying the <i>y</i> level of the curve and the name.
   * 
   * @param y  the level of the curve
   * @param name  the name of the curve, not null
   * @return a constant curve with the specified name, not null
   */
  public static ConstantDoublesCurve from(final double y, final String name) {
    return new ConstantDoublesCurve(y, name);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructor for Joda-Beans.
   */
  protected ConstantDoublesCurve() {
  }

  /**
   * Creates an instance specifying the <i>y</i> level of the curve.
   * 
   * @param y  the level of the curve
   */
  public ConstantDoublesCurve(final double y) {
    super();
    _y = y;
  }

  /**
   * Creates an instance specifying the <i>y</i> level of the curve and the name.
   * 
   * @param y  the level of the curve
   * @param name  the name of the curve, not null
   */
  public ConstantDoublesCurve(final double y, final String name) {
    super(name);
    _y = y;
  }

  //-------------------------------------------------------------------------
  /**
   * Throws an exception as there is no <i>x</i> data.
   * 
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data for constant curve");
  }

  /**
   * Gets the <i>y</i> data for the curve.
   * 
   * @return an array containing one element, the level, not null
   */
  @Override
  public Double[] getYData() {
    return new Double[] {_y };
  }

  /**
   * Gets the <i>y</i> data for the <i>x</i> value.
   * <p>
   * Any <i>x</i> value may be specified, including null.
   * 
   * @param x  the value, null ignored
   * @return the constant level value in a length one array, not null
   */
  @Override
  public Double getYValue(final Double x) {
    return _y;
  }

  /**
   * Gets the parameter sensitivity for the <i>x</i> value.
   * <p>
   * Any <i>x</i> value may be specified, including null.
   * 
   * @param x  the value, null ignored
   * @return the value 1.0 in a length one array, not null
   */
  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    return new Double[] {1.0d };
  }

  /**
   * Creates an interpolated curve using the specified <i>x</i> values and this constant <i>y</i> value.
   * 
   * @param x  the array of <i>x</i> values, not null
   * @param interpolator  the interpolator, not null
   * @return the interpolated curve with constant value, not null
   */
  public InterpolatedDoublesCurve toInterpolatedDoublesCurve(final double[] x, final Interpolator1D interpolator) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notNull(interpolator, "interpolator");
    final double[] y = new double[x.length];
    Arrays.fill(y, _y);
    return InterpolatedDoublesCurve.from(x, y, interpolator);
  }

  @Override
  public double getDyDx(final double x) {
    return 0;
  }

  /**
   * Gets the size of the curve, which is one.
   * 
   * @return the size of the curve, one
   */
  @Override
  public int size() {
    return 1;
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
    final ConstantDoublesCurve other = (ConstantDoublesCurve) obj;
    return Double.doubleToLongBits(_y) == Double.doubleToLongBits(other._y);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "ConstantDoublesCurve[name=" + getName() + ", y=" + _y + "]";
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConstantDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static ConstantDoublesCurve.Meta meta() {
    return ConstantDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ConstantDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public ConstantDoublesCurve.Meta metaBean() {
    return ConstantDoublesCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the constant value of the curve.
   * @return the value of the property
   */
  private double getY() {
    return _y;
  }

  /**
   * Sets the constant value of the curve.
   * @param y  the new value of the property
   */
  private void setY(double y) {
    this._y = y;
  }

  /**
   * Gets the the {@code y} property.
   * @return the property, not null
   */
  public final Property<Double> y() {
    return metaBean().y().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ConstantDoublesCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConstantDoublesCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code y} property.
     */
    private final MetaProperty<Double> _y = DirectMetaProperty.ofReadWrite(
        this, "y", ConstantDoublesCurve.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "y");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 121:  // y
          return _y;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ConstantDoublesCurve> builder() {
      return new DirectBeanBuilder<ConstantDoublesCurve>(new ConstantDoublesCurve());
    }

    @Override
    public Class<? extends ConstantDoublesCurve> beanType() {
      return ConstantDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code y} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> y() {
      return _y;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 121:  // y
          return ((ConstantDoublesCurve) bean).getY();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 121:  // y
          ((ConstantDoublesCurve) bean).setY((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
