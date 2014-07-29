/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
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

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.ArgumentChecker;

/**
 * Class defining a spread curve, i.e. a curve that is the result of a mathematical operation
 * (see {@link CurveSpreadFunction}) on two or more curves.
 * For example, a simple spread curve could be <i>C = A - B</i>. As this curve is in the same
 * hierarchy as the other curves, a spread curve can be defined on another spread curve,
 * e.g. <i>E = C * D = D * (A - B)</i>.
 */
@BeanDefinition
public class SpreadDoublesCurve
    extends DoublesCurve {

  /**
   * The spread function.
   */
  @PropertyDefinition(get = "private", set = "private")
  private CurveSpreadFunction _spreadFunction;
  /**
   * The evaluated function.
   */
  @PropertyDefinition(get = "private", set = "private")
  private Function<Double, Double> _f;
  /**
   * The curves.
   */
  @PropertyDefinition(get = "private", set = "private")
  private DoublesCurve[] _curves;

  //-------------------------------------------------------------------------
  /**
   * Takes an array of curves that are to be operated on by the spread function.
   * The name of the spread curve is automatically generated.
   *
   * @param spreadFunction  the spread function, not null
   * @param curves  the curves, not null
   * @return the spread curve, not null
   */
  public static SpreadDoublesCurve from(final CurveSpreadFunction spreadFunction, final DoublesCurve... curves) {
    return new SpreadDoublesCurve(spreadFunction, curves);
  }

  /**
   * Takes an array of curves that are to be operated on by the spread function.
   *
   * @param spreadFunction  the spread function, not null
   * @param name  the name of the curve, not null
   * @param curves  the curves, not null
   * @return the spread curve, not null
   */
  public static SpreadDoublesCurve from(final CurveSpreadFunction spreadFunction, final String name, final DoublesCurve... curves) {
    return new SpreadDoublesCurve(spreadFunction, name, curves);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructor for Joda-Beans.
   */
  protected SpreadDoublesCurve() {
  }

  /**
   * Creates a spread curve.
   *
   * @param spreadFunction  the spread function, not null
   * @param curves  the curves, not null, contains more than one curve, not null
   */
  public SpreadDoublesCurve(final CurveSpreadFunction spreadFunction, final DoublesCurve... curves) {
    super();
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves.length > 1, "curves");
    ArgumentChecker.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  /**
   * Creates a spread curve.
   *
   * @param spreadFunction  the spread function, not null
   * @param name  the name of the curve, not null
   * @param curves  the curves, not null, contains more than one curve, not null
   */
  public SpreadDoublesCurve(final CurveSpreadFunction spreadFunction, final String name, final DoublesCurve... curves) {
    super(name);
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.isTrue(curves.length > 1, "curves");
    ArgumentChecker.notNull(spreadFunction, "spread operator");
    _curves = curves;
    _spreadFunction = spreadFunction;
    _f = spreadFunction.evaluate(curves);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a set of the <b>unique</b> names of the curves that were used to construct this curve.
   * If a constituent curve is a spread curve, then all of its underlyings are included.
   *
   * @return the set of underlying names, not null
   */
  public Set<String> getUnderlyingNames() {
    final Set<String> result = new HashSet<>();
    for (final Curve<Double, Double> curve : _curves) {
      if (curve instanceof SpreadDoublesCurve) {
        result.addAll(((SpreadDoublesCurve) curve).getUnderlyingNames());
      } else {
        result.add(curve.getName());
      }
    }
    return result;
  }

  /**
   * Returns a string that represents the mathematical form of this curve.
   * For example, <i>D = (A + (B / C))</i>.
   *
   * @return the long name of this curve, not null
   */
  public String getLongName() {
    final StringBuilder buf = new StringBuilder(getName());
    buf.append("=");
    int i = 0;
    buf.append("(");
    for (final Curve<Double, Double> curve : _curves) {
      if (curve instanceof SpreadDoublesCurve) {
        buf.append(((SpreadDoublesCurve) curve).getLongName().substring(2));
      } else {
        buf.append(curve.getName());
      }
      if (i != _curves.length - 1) {
        buf.append(_spreadFunction.getOperationName());
      }
      i++;
    }
    buf.append(")");
    return buf.toString();
  }

  /**
   * Gets the underlying curves.
   *
   * @return the underlying curves, not null
   */
  public DoublesCurve[] getUnderlyingCurves() {
    return _curves;
  }

  /**
   * Throws an exception as there is no <i>x</i> data.
   *
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  @Override
  public Double[] getXData() {
    throw new UnsupportedOperationException();
  }

  /**
   * Throws an exception as there is no <i>y</i> data.
   *
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double getYValue(final Double x) {
    ArgumentChecker.notNull(x, "x");
    return _f.evaluate(x);
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    if (_curves.length == 2) {
      if (_curves[0] instanceof InterpolatedDoublesCurve && _curves[1] instanceof ConstantDoublesCurve) {
        return _curves[0].getYValueParameterSensitivity(x);
      } else if (_curves[1] instanceof InterpolatedDoublesCurve && _curves[0] instanceof ConstantDoublesCurve) {
        return _curves[1].getYValueParameterSensitivity(x);
      }
    }
    throw new UnsupportedOperationException("Parameter sensitivity not supported yet for SpreadDoublesCurve");
  }

  @Override
  public double getDyDx(final double x) {
    throw new NotImplementedException();
  }

  /**
   * Throws an exception as there is no <i>x</i> or <i>y</i> data.
   *
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  @Override
  public int size() {
    int size = 0;
    for (final DoublesCurve underlying : _curves) {
      if (underlying instanceof InterpolatedDoublesCurve || underlying instanceof NodalDoublesCurve || underlying instanceof SpreadDoublesCurve) {
        size += underlying.size();
      }
    }
    if (size != 0) {
      return size;
    }
    throw new UnsupportedOperationException("Size not supported for SpreadDoublesCurve " + getLongName());
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
    final SpreadDoublesCurve other = (SpreadDoublesCurve) obj;
    if (!Arrays.equals(_curves, other._curves)) {
      return false;
    }
    return ObjectUtils.equals(_spreadFunction, other._spreadFunction);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_curves);
    result = prime * result + _spreadFunction.hashCode();
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SpreadDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static SpreadDoublesCurve.Meta meta() {
    return SpreadDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SpreadDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public SpreadDoublesCurve.Meta metaBean() {
    return SpreadDoublesCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the spread function.
   * @return the value of the property
   */
  private CurveSpreadFunction getSpreadFunction() {
    return _spreadFunction;
  }

  /**
   * Sets the spread function.
   * @param spreadFunction  the new value of the property
   */
  private void setSpreadFunction(CurveSpreadFunction spreadFunction) {
    this._spreadFunction = spreadFunction;
  }

  /**
   * Gets the the {@code spreadFunction} property.
   * @return the property, not null
   */
  public final Property<CurveSpreadFunction> spreadFunction() {
    return metaBean().spreadFunction().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the evaluated function.
   * @return the value of the property
   */
  private Function<Double, Double> getF() {
    return _f;
  }

  /**
   * Sets the evaluated function.
   * @param f  the new value of the property
   */
  private void setF(Function<Double, Double> f) {
    this._f = f;
  }

  /**
   * Gets the the {@code f} property.
   * @return the property, not null
   */
  public final Property<Function<Double, Double>> f() {
    return metaBean().f().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the curves.
   * @return the value of the property
   */
  private DoublesCurve[] getCurves() {
    return _curves;
  }

  /**
   * Sets the curves.
   * @param curves  the new value of the property
   */
  private void setCurves(DoublesCurve[] curves) {
    this._curves = curves;
  }

  /**
   * Gets the the {@code curves} property.
   * @return the property, not null
   */
  public final Property<DoublesCurve[]> curves() {
    return metaBean().curves().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public SpreadDoublesCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("SpreadDoublesCurve{");
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
    buf.append("spreadFunction").append('=').append(JodaBeanUtils.toString(getSpreadFunction())).append(',').append(' ');
    buf.append("f").append('=').append(JodaBeanUtils.toString(getF())).append(',').append(' ');
    buf.append("curves").append('=').append(JodaBeanUtils.toString(getCurves())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SpreadDoublesCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code spreadFunction} property.
     */
    private final MetaProperty<CurveSpreadFunction> _spreadFunction = DirectMetaProperty.ofReadWrite(
        this, "spreadFunction", SpreadDoublesCurve.class, CurveSpreadFunction.class);
    /**
     * The meta-property for the {@code f} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Function<Double, Double>> _f = DirectMetaProperty.ofReadWrite(
        this, "f", SpreadDoublesCurve.class, (Class) Function.class);
    /**
     * The meta-property for the {@code curves} property.
     */
    private final MetaProperty<DoublesCurve[]> _curves = DirectMetaProperty.ofReadWrite(
        this, "curves", SpreadDoublesCurve.class, DoublesCurve[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "spreadFunction",
        "f",
        "curves");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -872176021:  // spreadFunction
          return _spreadFunction;
        case 102:  // f
          return _f;
        case -1349116572:  // curves
          return _curves;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SpreadDoublesCurve> builder() {
      return new DirectBeanBuilder<SpreadDoublesCurve>(new SpreadDoublesCurve());
    }

    @Override
    public Class<? extends SpreadDoublesCurve> beanType() {
      return SpreadDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code spreadFunction} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CurveSpreadFunction> spreadFunction() {
      return _spreadFunction;
    }

    /**
     * The meta-property for the {@code f} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Function<Double, Double>> f() {
      return _f;
    }

    /**
     * The meta-property for the {@code curves} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DoublesCurve[]> curves() {
      return _curves;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -872176021:  // spreadFunction
          return ((SpreadDoublesCurve) bean).getSpreadFunction();
        case 102:  // f
          return ((SpreadDoublesCurve) bean).getF();
        case -1349116572:  // curves
          return ((SpreadDoublesCurve) bean).getCurves();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -872176021:  // spreadFunction
          ((SpreadDoublesCurve) bean).setSpreadFunction((CurveSpreadFunction) newValue);
          return;
        case 102:  // f
          ((SpreadDoublesCurve) bean).setF((Function<Double, Double>) newValue);
          return;
        case -1349116572:  // curves
          ((SpreadDoublesCurve) bean).setCurves((DoublesCurve[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
