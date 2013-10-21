/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;
import com.opengamma.util.tuple.DoublesPair;

/** 
 * Parent class for a family of curves where the data is stored as arrays.
 * It is possible to construct a curve using either unsorted (in <i>x</i>) data or sorted (ascending in <i>x</i>). 
 * Note that if the constructor is told that unsorted data are sorted then no sorting will take place, which will give unpredictable results.
 * 
 */
public abstract class ArraysDoublesCurve extends DoublesCurve implements Bean {

  @PropertyDefinition(get = "private")
  private final int _n;

  @PropertyDefinition(validate = "notNull", get = "manual")
  private final double[] _xData;

  @PropertyDefinition(validate = "notNull", get = "manual")
  private final double[] _yData;

  @PropertyDefinition(get = "private", set = "")
  private Double[] _xDataObject;

  @PropertyDefinition(get = "private", set = "")
  private Double[] _yDataObject;

  /**
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */

  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */

  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    _n = xData.length;
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData[i];
      Double y = yData[i];
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    *
    * @param data A map of <i>x-y</i> data, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      Double x = entry.getKey();
      Double y = entry.getValue();
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i++] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data An array of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      DoublesPair pair = data[i];
      ArgumentChecker.notNull(pair, "pair");
      _xData[i] = pair.first;
      _yData[i] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A set of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgumentChecker.notNull(entry, "entry");
      _xData[i] = entry.first;
      _yData[i++] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData A list of <i>x</i> data points, assumed to be sorted ascending, not null
    * @param yData A list of <i>y</i> data points, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      Double x = xData.get(i);
      Double y = yData.get(i);
      ArgumentChecker.notNull(x, "x");
      ArgumentChecker.notNull(y, "y");
      _xData[i] = x;
      _yData[i] = y;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A list of <i>x-y</i> data points, assumed to be sorted ascending, not null
    * @param isSorted Is the <i>x</i>-data sorted
    */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted) {
    super();
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.noNulls(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      _xData[i] = pair.first;
      _yData[i++] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _n = xData.length;
    _xData = Arrays.copyOf(xData, _n);
    _yData = Arrays.copyOf(yData, _n);
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData An array of <i>x</i> data, not null
    * @param yData An array of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Double[] xData, final Double[] yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    _n = xData.length;
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.length == yData.length, "x data size {} must be equal to y data size {}", xData.length, yData.length);
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(xData[i], "x");
      ArgumentChecker.notNull(yData[i], "y");
      _xData[i] = xData[i];
      _yData[i] = yData[i];
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A map of <i>x-y</i> data, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Map<Double, Double> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : data.entrySet()) {
      ArgumentChecker.notNull(entry.getKey(), "x");
      ArgumentChecker.notNull(entry.getValue(), "y");
      _xData[i] = entry.getKey();
      _yData[i++] = entry.getValue();
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data An array of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final DoublesPair[] data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.length;
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(data[i], "entry");
      _xData[i] = data[i].first;
      _yData[i] = data[i].second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A set of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final Set<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair entry : data) {
      ArgumentChecker.notNull(entry, "entry");
      _xData[i] = entry.first;
      _yData[i++] = entry.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param xData A list of <i>x</i> data, not null
    * @param yData A list of <i>y</i> data, not null, contains same number of entries as <i>x</i>
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final List<Double> xData, final List<Double> yData, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(xData, "x data");
    ArgumentChecker.notNull(yData, "y data");
    ArgumentChecker.isTrue(xData.size() == yData.size(), "x data size {} must be equal to y data size {}", xData.size(), yData.size());
    _n = xData.size();
    _xData = new double[_n];
    _yData = new double[_n];
    for (int i = 0; i < _n; i++) {
      ArgumentChecker.notNull(xData.get(i), "x");
      ArgumentChecker.notNull(yData.get(i), "y");
      _xData[i] = xData.get(i);
      _yData[i] = yData.get(i);
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  /**
    * 
    * @param data A list of <i>x-y</i> pairs, not null
    * @param isSorted Is the <i>x</i>-data sorted
    * @param name The name of the curve
    */
  public ArraysDoublesCurve(final List<DoublesPair> data, final boolean isSorted, final String name) {
    super(name);
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.noNulls(data, "data");
    _n = data.size();
    _xData = new double[_n];
    _yData = new double[_n];
    int i = 0;
    for (final DoublesPair pair : data) {
      _xData[i] = pair.first;
      _yData[i++] = pair.second;
    }
    if (!isSorted) {
      ParallelArrayBinarySort.parallelBinarySort(_xData, _yData);
    }
  }

  @Override
  public Double[] getXData() {

    if (_xDataObject != null) {

      return _xDataObject;
    }
    _xDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _xDataObject[i] = _xData[i];
    }
    return _xDataObject;
  }

  @Override
  public Double[] getYData() {
    if (_yDataObject != null) {
      return _yDataObject;
    }
    _yDataObject = new Double[_n];
    for (int i = 0; i < _n; i++) {
      _yDataObject[i] = _yData[i];
    }
    return _yDataObject;
  }

  /**
    * Returns the <i>x</i> data points as a primitive array
    * @return The <i>x</i> data
    */
  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  /**
    * Returns the <i>y</i> data points as a primitive array
    * @return The <i>y</i> data
    */
  public double[] getYDataAsPrimitive() {
    return _yData;
  }

  @Override
  public int size() {
    return _n;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
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
    final ArraysDoublesCurve other = (ArraysDoublesCurve) obj;
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ArraysDoublesCurve}.
   * @return the meta-bean, not null
   */
  public static ArraysDoublesCurve.Meta meta() {
    return ArraysDoublesCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ArraysDoublesCurve.Meta.INSTANCE);
  }

  @Override
  public ArraysDoublesCurve.Meta metaBean() {
    return ArraysDoublesCurve.Meta.INSTANCE;
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
   * Gets the n.
   * @return the value of the property
   */
  private int getN() {
    return _n;
  }

  /**
   * Gets the the {@code n} property.
   * @return the property, not null
   */
  public final Property<Integer> n() {
    return metaBean().n().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code xData} property.
   * @return the property, not null
   */
  public final Property<double[]> xData() {
    return metaBean().xData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the the {@code yData} property.
   * @return the property, not null
   */
  public final Property<double[]> yData() {
    return metaBean().yData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the xDataObject.
   * @return the value of the property
   */
  private Double[] getXDataObject() {
    return _xDataObject;
  }

  /**
   * Gets the the {@code xDataObject} property.
   * @return the property, not null
   */
  public final Property<Double[]> xDataObject() {
    return metaBean().xDataObject().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the yDataObject.
   * @return the value of the property
   */
  private Double[] getYDataObject() {
    return _yDataObject;
  }

  /**
   * Gets the the {@code yDataObject} property.
   * @return the property, not null
   */
  public final Property<Double[]> yDataObject() {
    return metaBean().yDataObject().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ArraysDoublesCurve clone() {
    BeanBuilder<? extends ArraysDoublesCurve> builder = metaBean().builder();
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
    StringBuilder buf = new StringBuilder(192);
    buf.append("ArraysDoublesCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("n").append('=').append(getN()).append(',').append(' ');
    buf.append("xData").append('=').append(getXData()).append(',').append(' ');
    buf.append("yData").append('=').append(getYData()).append(',').append(' ');
    buf.append("xDataObject").append('=').append(getXDataObject()).append(',').append(' ');
    buf.append("yDataObject").append('=').append(getYDataObject()).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ArraysDoublesCurve}.
   */
  public static class Meta extends DoublesCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code n} property.
     */
    private final MetaProperty<Integer> _n = DirectMetaProperty.ofReadOnly(
        this, "n", ArraysDoublesCurve.class, Integer.TYPE);
    /**
     * The meta-property for the {@code xData} property.
     */
    private final MetaProperty<double[]> _xData = DirectMetaProperty.ofReadOnly(
        this, "xData", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code yData} property.
     */
    private final MetaProperty<double[]> _yData = DirectMetaProperty.ofReadOnly(
        this, "yData", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code xDataObject} property.
     */
    private final MetaProperty<Double[]> _xDataObject = DirectMetaProperty.ofReadOnly(
        this, "xDataObject", ArraysDoublesCurve.class, Double[].class);
    /**
     * The meta-property for the {@code yDataObject} property.
     */
    private final MetaProperty<Double[]> _yDataObject = DirectMetaProperty.ofReadOnly(
        this, "yDataObject", ArraysDoublesCurve.class, Double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "n",
        "xData",
        "yData",
        "xDataObject",
        "yDataObject");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          return _n;
        case 112945218:  // xData
          return _xData;
        case 113868739:  // yData
          return _yData;
        case -2041692639:  // xDataObject
          return _xDataObject;
        case 456323298:  // yDataObject
          return _yDataObject;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ArraysDoublesCurve> builder() {
      throw new UnsupportedOperationException("ArraysDoublesCurve is an abstract class");
    }

    @Override
    public Class<? extends ArraysDoublesCurve> beanType() {
      return ArraysDoublesCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code n} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> n() {
      return _n;
    }

    /**
     * The meta-property for the {@code xData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> xData() {
      return _xData;
    }

    /**
     * The meta-property for the {@code yData} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> yData() {
      return _yData;
    }

    /**
     * The meta-property for the {@code xDataObject} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> xDataObject() {
      return _xDataObject;
    }

    /**
     * The meta-property for the {@code yDataObject} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> yDataObject() {
      return _yDataObject;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          return ((ArraysDoublesCurve) bean).getN();
        case 112945218:  // xData
          return ((ArraysDoublesCurve) bean).getXData();
        case 113868739:  // yData
          return ((ArraysDoublesCurve) bean).getYData();
        case -2041692639:  // xDataObject
          return ((ArraysDoublesCurve) bean).getXDataObject();
        case 456323298:  // yDataObject
          return ((ArraysDoublesCurve) bean).getYDataObject();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110:  // n
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: n");
        case 112945218:  // xData
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: xData");
        case 113868739:  // yData
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: yData");
        case -2041692639:  // xDataObject
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: xDataObject");
        case 456323298:  // yDataObject
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: yDataObject");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean)._xData, "xData");
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean)._yData, "yData");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
