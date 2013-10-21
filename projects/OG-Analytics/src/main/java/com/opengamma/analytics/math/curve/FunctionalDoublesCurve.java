/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * A curve that is defined by a function (i.e. <i>y = f(x)</i>, where <i>f(x)</i> is supplied)
 */
public class FunctionalDoublesCurve extends DoublesCurve {

  private static final ScalarFirstOrderDifferentiator DIFF = new ScalarFirstOrderDifferentiator();

  /**
   * 
   * @param function The function that defines the curve, not null
   * @return A functional curve with an automatically-generated name
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function) {
    return new FunctionalDoublesCurve(function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   * @return A functional curve with an automatically-generated name
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative) {
    return new FunctionalDoublesCurve(function, derivative);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name Name of the curve 
   * @return A functional curve
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final String name) {
    return new FunctionalDoublesCurve(function, name);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   * @param name Name of the curve 
   * @return A functional curve
   */
  public static FunctionalDoublesCurve from(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final String name) {
    return new FunctionalDoublesCurve(function, derivative, name);
  }

  @PropertyDefinition(validate = "notNull", get = "manual")
  private final Function1D<Double, Double> _function;

  @PropertyDefinition(validate = "notNull", get = "private")
  private final Function1D<Double, Double> _derivative;

  /**
   * TODO This is awaiting changes to Joda Beans to support final fields in Bean implementations
   * not ImmutableBean which is too restrictive as it forces implementations to be final
   *
   * @throws UnsupportedOperationException Always
   */
  private FunctionalDoublesCurve() {
    throw new UnsupportedOperationException("this constructor only exists for the benefit of Joda Beans");
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   */
  public FunctionalDoublesCurve(final Function1D<Double, Double> function) {
    super();
    Validate.notNull(function, "function");
    _function = function;
    _derivative = DIFF.differentiate(_function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param derivative The first derivative for the function, not null
   */
  private FunctionalDoublesCurve(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative) {
    super();
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(derivative, "derivative");
    _function = function;
    _derivative = derivative;
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   * @param name The name of the curve
   */
  public FunctionalDoublesCurve(final Function1D<Double, Double> function, final String name) {
    super(name);
    Validate.notNull(function, "function");
    _function = function;
    _derivative = DIFF.differentiate(_function);
  }

  /**
   * 
   * @param function The function that defines the curve, not null
   *   * @param derivative The first derivative for the function, not null
   * @param name The name of the curve
   */
  private FunctionalDoublesCurve(final Function1D<Double, Double> function, final Function1D<Double, Double> derivative, final String name) {
    super(name);
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(derivative, "derivative");
    _function = function;
    _derivative = derivative;
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException("Cannot get x data - this curve is defined by a function (x -> y)");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get y data - this curve is defined by a function (x -> y)");
  }

  @Override
  public Double getYValue(final Double x) {
    Validate.notNull(x, "x");
    return _function.evaluate(x);
  }

  @Override
  public double getDyDx(final double x) {
    return _derivative.evaluate(x);
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    throw new UnsupportedOperationException("Parameter sensitivity not supported yet for FunctionalDoublesCurve");
  }

  /**
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public int size() {
    throw new UnsupportedOperationException("Cannot get size - this curve is defined by a function (x -> y)");
  }

  /**
   * 
   * @param x An array of <i>x</i> values
   * @param interpolator An interpolator
   * @return An interpolated curve with values <i>(x, f(x))</i>
   */
  public InterpolatedDoublesCurve toInterpolatedDoublesCurve(final double[] x, final Interpolator1D interpolator) {
    Validate.notNull(x, "x");
    Validate.notNull(interpolator);
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = _function.evaluate(x[i]);
    }
    return InterpolatedDoublesCurve.from(x, y, interpolator);
  }

  /**
   * 
   * @return The function
   */
  public Function1D<Double, Double> getFunction() {
    return _function;
  }

  /**
   * 
   * @return The function
   */
  public Function1D<Double, Double> getFirstDerivativeFunction() {
    return _derivative;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _function.hashCode();
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
    final FunctionalDoublesCurve other = (FunctionalDoublesCurve) obj;
    return ObjectUtils.equals(_function, other._function);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FunctionalDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static FunctionalDoublesCurve.Meta meta() {
    return FunctionalDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FunctionalDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public FunctionalDoublesCurve.Meta metaBean() {
    return FunctionalDoublesCurve.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code function} property.
   * @return the property, not null
   */
  public final Property<Function1D<Double, Double>> function() {
    return metaBean().function().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the derivative.
   * @return the value of the property, not null
   */
  private Function1D<Double, Double> getDerivative() {
    return _derivative;
  }

  /**
   * Gets the the {@code derivative} property.
   * @return the property, not null
   */
  public final Property<Function1D<Double, Double>> derivative() {
    return metaBean().derivative().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FunctionalDoublesCurve clone() {
    BeanBuilder<? extends FunctionalDoublesCurve> builder = metaBean().builder();
    for (MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = ((Bean) value).clone();
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("FunctionalDoublesCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("function").append('=').append(getFunction()).append(',').append(' ');
    buf.append("derivative").append('=').append(getDerivative()).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FunctionalDoublesCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code function} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Function1D<Double, Double>> _function = DirectMetaProperty.ofReadOnly(
        this, "function", FunctionalDoublesCurve.class, (Class) Function1D.class);
    /**
     * The meta-property for the {@code derivative} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Function1D<Double, Double>> _derivative = DirectMetaProperty.ofReadOnly(
        this, "derivative", FunctionalDoublesCurve.class, (Class) Function1D.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "function",
        "derivative");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1380938712:  // function
          return _function;
        case -1353885305:  // derivative
          return _derivative;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FunctionalDoublesCurve> builder() {
      return new DirectBeanBuilder<FunctionalDoublesCurve>(new FunctionalDoublesCurve());
    }

    @Override
    public Class<? extends FunctionalDoublesCurve> beanType() {
      return FunctionalDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code function} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Function1D<Double, Double>> function() {
      return _function;
    }

    /**
     * The meta-property for the {@code derivative} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Function1D<Double, Double>> derivative() {
      return _derivative;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1380938712:  // function
          return ((FunctionalDoublesCurve) bean).getFunction();
        case -1353885305:  // derivative
          return ((FunctionalDoublesCurve) bean).getDerivative();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1380938712:  // function
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: function");
        case -1353885305:  // derivative
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: derivative");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FunctionalDoublesCurve) bean)._function, "function");
      JodaBeanUtils.notNull(((FunctionalDoublesCurve) bean)._derivative, "derivative");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
