/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * A curve that is defined by a set of nodal points (i.e. <i>x-y</i> data) and an interpolator to return values of <i>y</i> for values 
 * of <i>x</i> that do not lie on nodal <i>x</i> values. 
 * One extra node point with a set value is added (called anchor point). The value is often 0.0 (rate anchor) or 1.0 (discount factor anchor).
 * This is used in particular for spread curves; without anchor points, each curve in the spread could be shifted in opposite directions for the same total result.
 * To anchor is used to remove the translation indetermination.
 */
public final class DoublesCurveInterpolatedAnchor extends InterpolatedDoublesCurve implements Bean {

  /**
   * Anchor index. The index in the x value of the anchor.
   */
  @PropertyDefinition(get = "manual", set = "")
  private final int _anchorIndex;

  /**
   * Private constructor.
   * @param xData The sorted xData, including the anchor.
   * @param yData The yData, including the anchor.
   * @param anchorIndex The index in the xData at which the anchor is located.
   * @param interpolator The interpolator.
   * @param name The curve name.
   */
  private DoublesCurveInterpolatedAnchor(double[] xData, double[] yData, int anchorIndex, Interpolator1D interpolator, String name) {
    super(xData, yData, interpolator, true, name);
    _anchorIndex = anchorIndex;
  }

  /**
   * Constructor.
   * @param xData The x data without the anchor.
   * @param yData The y data.
   * @param anchor The anchor point. Should not be in xData.
   * @param interpolator The interpolator.
   * @param name The curve name.
   * @return The curve.
   */
  public static DoublesCurveInterpolatedAnchor from(double[] xData, double[] yData, double anchor, Interpolator1D interpolator, String name) {
    ArgumentChecker.notNull(xData, "X data");
    int xLength = xData.length;
    ArgumentChecker.notNull(yData, "Y data");
    ArgumentChecker.isTrue(xLength == yData.length, "Data of incorrect length.");
    double[] xExtended = new double[xLength + 1];
    double[] yExtended = new double[xLength + 1];
    System.arraycopy(xData, 0, xExtended, 0, xLength);
    xExtended[xLength] = anchor;
    System.arraycopy(yData, 0, yExtended, 0, xLength);
    ParallelArrayBinarySort.parallelBinarySort(xExtended, yExtended);
    int anchorIndex = ArrayUtils.indexOf(xExtended, anchor);
    return new DoublesCurveInterpolatedAnchor(xExtended, yExtended, anchorIndex, interpolator, name);
  }

  /**
   * Constructor.
   * @param xData The x data without the anchor.
   * @param yData The y data.
   * @param anchor The anchor point. Should not be in xData.
   * @param anchorValue The anchor point value.
   * @param interpolator The interpolator.
   * @param name The curve name.
   * @return The curve.
   */
  public static DoublesCurveInterpolatedAnchor from(double[] xData, double[] yData, double anchor, double anchorValue, Interpolator1D interpolator, String name) {
    ArgumentChecker.notNull(xData, "X data");
    int xLength = xData.length;
    ArgumentChecker.notNull(yData, "Y data");
    ArgumentChecker.isTrue(xLength == yData.length, "Data of incorrect length.");
    double[] xExtended = new double[xLength + 1];
    double[] yExtended = new double[xLength + 1];
    System.arraycopy(xData, 0, xExtended, 0, xLength);
    xExtended[xLength] = anchor;
    System.arraycopy(yData, 0, yExtended, 0, xLength);
    yExtended[xLength] = anchorValue;
    ParallelArrayBinarySort.parallelBinarySort(xExtended, yExtended);
    int anchorIndex = ArrayUtils.indexOf(xExtended, anchor);
    return new DoublesCurveInterpolatedAnchor(xExtended, yExtended, anchorIndex, interpolator, name);
  }

  /**
   * Gets the anchor index.
   * @return The index.
   */
  public int getAnchorIndex() {
    return _anchorIndex;
  }

  @Override
  public int size() {
    return super.size() - 1; // To take the anchor into account.
  }

  /**
   * The sensitivity is the sensitivity of the underlying interpolated 
   * @param x The value for which the sensitivity is computed.
   * @return The sensitivity.
   */
  @Override
  public Double[] getYValueParameterSensitivity(Double x) {
    Validate.notNull(x, "x");
    Double[] sensitivityWithAnchor = super.getYValueParameterSensitivity(x);
    Double[] sensitivity = new Double[sensitivityWithAnchor.length - 1];
    System.arraycopy(sensitivityWithAnchor, 0, sensitivity, 0, _anchorIndex);
    System.arraycopy(sensitivityWithAnchor, _anchorIndex + 1, sensitivity, _anchorIndex, sensitivityWithAnchor.length - _anchorIndex - 1);
    return sensitivity;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _anchorIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    DoublesCurveInterpolatedAnchor other = (DoublesCurveInterpolatedAnchor) obj;
    if (_anchorIndex != other._anchorIndex) {
      return false;
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DoublesCurveInterpolatedAnchor}.
   * @return the meta-bean, not null
   */
  public static DoublesCurveInterpolatedAnchor.Meta meta() {
    return DoublesCurveInterpolatedAnchor.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DoublesCurveInterpolatedAnchor.Meta.INSTANCE);
  }

  @Override
  public DoublesCurveInterpolatedAnchor.Meta metaBean() {
    return DoublesCurveInterpolatedAnchor.Meta.INSTANCE;
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
   * Gets the the {@code anchorIndex} property.
   * @return the property, not null
   */
  public Property<Integer> anchorIndex() {
    return metaBean().anchorIndex().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DoublesCurveInterpolatedAnchor clone() {
    BeanBuilder<? extends DoublesCurveInterpolatedAnchor> builder = metaBean().builder();
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
    StringBuilder buf = new StringBuilder(64);
    buf.append("DoublesCurveInterpolatedAnchor{");
    buf.append("anchorIndex").append('=').append(getAnchorIndex());
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DoublesCurveInterpolatedAnchor}.
   */
  public static final class Meta extends InterpolatedDoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code anchorIndex} property.
     */
    private final MetaProperty<Integer> _anchorIndex = DirectMetaProperty.ofReadOnly(
        this, "anchorIndex", DoublesCurveInterpolatedAnchor.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "anchorIndex");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1564745955:  // anchorIndex
          return _anchorIndex;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DoublesCurveInterpolatedAnchor> builder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends DoublesCurveInterpolatedAnchor> beanType() {
      return DoublesCurveInterpolatedAnchor.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code anchorIndex} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> anchorIndex() {
      return _anchorIndex;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1564745955:  // anchorIndex
          return ((DoublesCurveInterpolatedAnchor) bean).getAnchorIndex();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1564745955:  // anchorIndex
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: anchorIndex");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
