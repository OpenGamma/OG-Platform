/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.io.Serializable;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Parent class for a family of curves that have real <i>x</i> and <i>y</i> values.
 */
@BeanDefinition
public abstract class DoublesCurve extends Curve<Double, Double> implements Serializable {

  /**
   * Constructor
   */
  protected DoublesCurve() {
  }

  /**
   * Constructor with a name.
   * 
   * @param name  the curve name, not null
   */
  protected DoublesCurve(final String name) {
    super(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Computes the sensitivity of the Y value with respect to the curve parameters.
   * 
   * @param x  the value at which the parameter sensitivity is computed, not null
   * @return the sensitivity, not null
   */
  public abstract Double[] getYValueParameterSensitivity(Double x);

  /**
   * Compute the first derivative of the curve, $\frac{dy}{dx}$.
   * 
   * @param x  the value at which the derivative is taken
   * @return the first derivative 
   */
  public abstract double getDyDx(final double x);

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DoublesCurve}.
   * @return the meta-bean, not null
   */
  public static DoublesCurve.Meta meta() {
    return DoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DoublesCurve.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  @Override
  public DoublesCurve.Meta metaBean() {
    return DoublesCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("DoublesCurve{");
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
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DoublesCurve}.
   */
  public static class Meta extends Curve.Meta<Double, Double> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends DoublesCurve> builder() {
      throw new UnsupportedOperationException("DoublesCurve is an abstract class");
    }

    @Override
    public Class<? extends DoublesCurve> beanType() {
      return DoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
