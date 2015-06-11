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
import org.joda.beans.BeanDefinition;
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
 */
@BeanDefinition
public abstract class ArraysDoublesCurve extends DoublesCurve {

  /**
   * The size of the data points.
   */
  @PropertyDefinition(get = "private", set = "private")
  private int _n;
  /**
   * The <i>x</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private double[] _xData;
  /**
   * The <i>y</i> values.
   */
  @PropertyDefinition(validate = "notNull", get = "manual", set = "private")
  private double[] _yData;
  /**
   * The <i>x</i> values.
   */
  @PropertyDefinition(get = "private", set = "private")
  private Double[] _xDataObject;
  /**
   * The <i>y</i> values.
   */
  @PropertyDefinition(get = "private", set = "private")
  private Double[] _yDataObject;

  /**
   * Constructor for Joda-Beans.
   */
  protected ArraysDoublesCurve() {
  }

  /**
   * Creates an instance.
   *
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   */
  public ArraysDoublesCurve(final double[] xData, final double[] yData, final boolean isSorted) {
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
   * Creates an instance.
   *
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param data  the map of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param data  the array of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param data  the set of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param xData  the list of <i>x</i> data, not null
   * @param yData  the list of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param data  the list of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
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
   * Creates an instance.
   *
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param xData  the array of <i>x</i> data, not null
   * @param yData  the array of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param data  the map of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param data  the array of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param data  the set of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param xData  the list of <i>x</i> data, not null
   * @param yData  the list of <i>y</i> data, contains same number of entries as <i>x</i>, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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
   * Creates an instance.
   *
   * @param data  the list of pairs of <i>x-y</i> data, not null
   * @param isSorted  whether the <i>x</i>-data is sorted ascending
   * @param name  the name of the curve, not null
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

  //-------------------------------------------------------------------------
  @Override
  public synchronized Double[] getXData() {
    System.out.println("Ran sync code");
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
  public synchronized Double[] getYData() {
    System.out.println("Ran sync code");
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
   * Returns the <i>x</i> data points as a primitive array.
   *
   * @return the <i>x</i> data, not null
   */
  public double[] getXDataAsPrimitive() {
    return _xData;
  }

  /**
   * Returns the <i>y</i> data points as a primitive array.
   *
   * @return the <i>y</i> data, not null
   */
  public double[] getYDataAsPrimitive() {
    return _yData;
  }

  @Override
  public int size() {
    return _n;
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
    final ArraysDoublesCurve other = (ArraysDoublesCurve) obj;
    return ArrayUtils.isEquals(_xData, other._xData) && ArrayUtils.isEquals(_yData, other._yData);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_xData);
    result = prime * result + Arrays.hashCode(_yData);
    return result;
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

  //-----------------------------------------------------------------------
  /**
   * Gets the size of the data points.
   * @return the value of the property
   */
  private int getN() {
    return _n;
  }

  /**
   * Sets the size of the data points.
   * @param n  the new value of the property
   */
  private void setN(int n) {
    this._n = n;
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
   * Sets the <i>x</i> values.
   * @param xData  the new value of the property, not null
   */
  private void setXData(double[] xData) {
    JodaBeanUtils.notNull(xData, "xData");
    this._xData = xData;
  }

  /**
   * Gets the the {@code xData} property.
   * @return the property, not null
   */
  public final Property<double[]> xData() {
    return metaBean().xData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the <i>y</i> values.
   * @param yData  the new value of the property, not null
   */
  private void setYData(double[] yData) {
    JodaBeanUtils.notNull(yData, "yData");
    this._yData = yData;
  }

  /**
   * Gets the the {@code yData} property.
   * @return the property, not null
   */
  public final Property<double[]> yData() {
    return metaBean().yData().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the <i>x</i> values.
   * @return the value of the property
   */
  private Double[] getXDataObject() {
    return _xDataObject;
  }

  /**
   * Sets the <i>x</i> values.
   * @param xDataObject  the new value of the property
   */
  private void setXDataObject(Double[] xDataObject) {
    this._xDataObject = xDataObject;
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
   * Gets the <i>y</i> values.
   * @return the value of the property
   */
  private Double[] getYDataObject() {
    return _yDataObject;
  }

  /**
   * Sets the <i>y</i> values.
   * @param yDataObject  the new value of the property
   */
  private void setYDataObject(Double[] yDataObject) {
    this._yDataObject = yDataObject;
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

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
    buf.append("n").append('=').append(JodaBeanUtils.toString(getN())).append(',').append(' ');
    buf.append("xData").append('=').append(JodaBeanUtils.toString(getXData())).append(',').append(' ');
    buf.append("yData").append('=').append(JodaBeanUtils.toString(getYData())).append(',').append(' ');
    buf.append("xDataObject").append('=').append(JodaBeanUtils.toString(getXDataObject())).append(',').append(' ');
    buf.append("yDataObject").append('=').append(JodaBeanUtils.toString(getYDataObject())).append(',').append(' ');
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
    private final MetaProperty<Integer> _n = DirectMetaProperty.ofReadWrite(
        this, "n", ArraysDoublesCurve.class, Integer.TYPE);
    /**
     * The meta-property for the {@code xData} property.
     */
    private final MetaProperty<double[]> _xData = DirectMetaProperty.ofReadWrite(
        this, "xData", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code yData} property.
     */
    private final MetaProperty<double[]> _yData = DirectMetaProperty.ofReadWrite(
        this, "yData", ArraysDoublesCurve.class, double[].class);
    /**
     * The meta-property for the {@code xDataObject} property.
     */
    private final MetaProperty<Double[]> _xDataObject = DirectMetaProperty.ofReadWrite(
        this, "xDataObject", ArraysDoublesCurve.class, Double[].class);
    /**
     * The meta-property for the {@code yDataObject} property.
     */
    private final MetaProperty<Double[]> _yDataObject = DirectMetaProperty.ofReadWrite(
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
          ((ArraysDoublesCurve) bean).setN((Integer) newValue);
          return;
        case 112945218:  // xData
          ((ArraysDoublesCurve) bean).setXData((double[]) newValue);
          return;
        case 113868739:  // yData
          ((ArraysDoublesCurve) bean).setYData((double[]) newValue);
          return;
        case -2041692639:  // xDataObject
          ((ArraysDoublesCurve) bean).setXDataObject((Double[]) newValue);
          return;
        case 456323298:  // yDataObject
          ((ArraysDoublesCurve) bean).setYDataObject((Double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean)._xData, "xData");
      JodaBeanUtils.notNull(((ArraysDoublesCurve) bean)._yData, "yData");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}