/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static com.opengamma.analytics.math.utilities.Epsilon.epsilon;
import static com.opengamma.analytics.math.utilities.Epsilon.epsilonP;

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

import com.opengamma.util.ArgumentChecker;

/**
 * Curve described by the Nelson-Siegle function.
 * The function is one of the most used functional form to describe a bond yield curve.
 */
@BeanDefinition
public class DoublesCurveNelsonSiegel
    extends DoublesCurve {

  /**
   * The number of parameters of the curve.
   */
  private static final int NB_PARAMETERS = 4;

  /**
   * The array with the four parameters used to describe the function.
   * The parameters are beta0, beta1, beta2 and lambda (in that order).
   */
  @PropertyDefinition(validate = "notNull", get = "private", set = "private")
  private double[] _parameters;

  /**
   * Constructor for Joda-Beans.
   */
  protected DoublesCurveNelsonSiegel() {
    super();
  }

  /**
   * Constructor from the four parameters and a name.
   * 
   * @param name  the curve name, not null
   * @param beta0  the beta0 parameter
   * @param beta1  the beta1 parameter
   * @param beta2  the beta2 parameter
   * @param lambda  the lambda parameter
   */
  public DoublesCurveNelsonSiegel(final String name, final double beta0, final double beta1, final double beta2, final double lambda) {
    super(name);
    _parameters = new double[] {beta0, beta1, beta2, lambda };
  }

  /**
   * Constructor from the four parameters as an array and a name.
   * 
   * @param name  the curve name, not null
   * @param parameters  the array with the four parameters used to describe the function.
   *  The parameters are beta0, beta1, beta2 and lambda (in that order)
   */
  public DoublesCurveNelsonSiegel(final String name, final double[] parameters) {
    super(name);
    ArgumentChecker.notNull(parameters, "parameters");
    ArgumentChecker.isTrue(parameters.length == NB_PARAMETERS, "Incorrect number of parameters");
    _parameters = parameters;
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
    throw new UnsupportedOperationException("Cannot get x data for Nelson-Siegle curve");
  }

  /**
   * Throws an exception as there is no <i>y</i> data.
   * 
   * @return throws UnsupportedOperationException
   * @throws UnsupportedOperationException always
   */
  @Override
  public Double[] getYData() {
    throw new UnsupportedOperationException("Cannot get x data for Nelson-Siegle curve");
  }

  @Override
  public int size() {
    return NB_PARAMETERS;
  }

  @Override
  public Double getYValue(final Double x) {
    final double x1 = x / _parameters[3];
    final double x2 = epsilon(-x1);
    return _parameters[0] + _parameters[1] * x2 + _parameters[2] * (x2 - Math.exp(-x1));
  }

  @Override
  public double getDyDx(final double x) {
    final double x1 = x / _parameters[3]; //TODO untested
    final double eP = epsilonP(-x1);
    return -(_parameters[1] * eP + _parameters[2] * (eP - Math.exp(-x1))) / _parameters[3];
  }

  @Override
  public Double[] getYValueParameterSensitivity(final Double x) {
    // Forward sweep
    final double x1 = x / _parameters[3];
    final double expx1 = Math.exp(-x1);
    final double x2;
    if (x1 < 1.0E-6) {
      x2 = 1.0 - 0.5 * x1;
    } else {
      x2 = (1 - expx1) / x1;
    }
    //    final double value = _parameters[0] + _parameters[1] * x2 + _parameters[2] * (x2 - expx1);
    // Backward sweep
    final double valueBar = 1.0;
    final double x2Bar = (_parameters[1] + _parameters[2]) * valueBar;
    double expx1Bar;
    double x1Bar;
    if (x1 < 1.0E-6) {
      expx1Bar = -_parameters[2] * valueBar;
      x1Bar = -0.5 * x2Bar + -expx1 * expx1Bar;
    } else {
      expx1Bar = -_parameters[2] * valueBar + -1.0 / x1 * x2Bar;
      x1Bar = -(1 - expx1) / (x1 * x1) * x2Bar + -expx1 * expx1Bar;
    }
    final Double[] parametersBar = new Double[4];
    parametersBar[0] = valueBar;
    parametersBar[1] = x2 * valueBar;
    parametersBar[2] = (x2 - expx1) * valueBar;
    parametersBar[3] = -x / (_parameters[3] * _parameters[3]) * x1Bar;
    return parametersBar;
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
    final DoublesCurveNelsonSiegel other = (DoublesCurveNelsonSiegel) obj;
    if (!Arrays.equals(_parameters, other._parameters)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_parameters);
    return result;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DoublesCurveNelsonSiegel}.
   * @return the meta-bean, not null
   */
  public static DoublesCurveNelsonSiegel.Meta meta() {
    return DoublesCurveNelsonSiegel.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DoublesCurveNelsonSiegel.Meta.INSTANCE);
  }

  @Override
  public DoublesCurveNelsonSiegel.Meta metaBean() {
    return DoublesCurveNelsonSiegel.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the array with the four parameters used to describe the function.
   * The parameters are beta0, beta1, beta2 and lambda (in that order).
   * @return the value of the property, not null
   */
  private double[] getParameters() {
    return _parameters;
  }

  /**
   * Sets the array with the four parameters used to describe the function.
   * The parameters are beta0, beta1, beta2 and lambda (in that order).
   * @param parameters  the new value of the property, not null
   */
  private void setParameters(double[] parameters) {
    JodaBeanUtils.notNull(parameters, "parameters");
    this._parameters = parameters;
  }

  /**
   * Gets the the {@code parameters} property.
   * The parameters are beta0, beta1, beta2 and lambda (in that order).
   * @return the property, not null
   */
  public final Property<double[]> parameters() {
    return metaBean().parameters().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DoublesCurveNelsonSiegel clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("DoublesCurveNelsonSiegel{");
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
    buf.append("parameters").append('=').append(JodaBeanUtils.toString(getParameters())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DoublesCurveNelsonSiegel}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code parameters} property.
     */
    private final MetaProperty<double[]> _parameters = DirectMetaProperty.ofReadWrite(
        this, "parameters", DoublesCurveNelsonSiegel.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "parameters");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 458736106:  // parameters
          return _parameters;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DoublesCurveNelsonSiegel> builder() {
      return new DirectBeanBuilder<DoublesCurveNelsonSiegel>(new DoublesCurveNelsonSiegel());
    }

    @Override
    public Class<? extends DoublesCurveNelsonSiegel> beanType() {
      return DoublesCurveNelsonSiegel.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code parameters} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> parameters() {
      return _parameters;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 458736106:  // parameters
          return ((DoublesCurveNelsonSiegel) bean).getParameters();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 458736106:  // parameters
          ((DoublesCurveNelsonSiegel) bean).setParameters((double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DoublesCurveNelsonSiegel) bean)._parameters, "parameters");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
