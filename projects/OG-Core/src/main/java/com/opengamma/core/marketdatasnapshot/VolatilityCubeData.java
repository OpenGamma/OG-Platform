/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.FirstThenSecondPairComparator;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Data structure to hold a particular volatility cube's data points.
 * Note no interpolation or fitting is done in this code.
 * 
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 * @param <Z> Type of the z-data
 */
@BeanDefinition
public class VolatilityCubeData<X, Y, Z> implements Bean, Serializable {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  /**
   * Default name for the x axis.
   */
  public static final String DEFAULT_X_LABEL = "x";

  /**
   * Default name for the y axis.
   */
  public static final String DEFAULT_Y_LABEL = "y";

  /**
   * Default name for the z axis.
   */
  public static final String DEFAULT_Z_LABEL = "z";

  /**
   * Comparator for the pairs.
   */
  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;

  /**
   * The definition name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _definitionName;

  /**
   * The specification name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _specificationName;

  /**
   * A (x, y, z) to volatility map.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Map<Triple<X, Y, Z>, Double> _values;

  /**
   * The x axis label.
   */
  @PropertyDefinition(validate = "notNull")
  private String _xLabel;

  /**
   * The y axis label.
   */
  @PropertyDefinition(validate = "notNull")
  private String _yLabel;

  /**
   * The z axis label
   */
  @PropertyDefinition(validate = "notNull")
  private String _zLabel;

  /**
   * The x values
   */
  @PropertyDefinition(validate = "notNull")
  private X[] _xs;

  /**
   * The y values
   */
  @PropertyDefinition(validate = "notNull")
  private Y[] _ys;

  /**
   * The z values
   */
  @PropertyDefinition(validate = "notNull")
  private Z[] _zs;

  /**
   * The volatilities
   */
  @PropertyDefinition(validate = "notNull")
  private Double[] _vs;

  /**
   * A set of unique x values
   */
  private transient SortedSet<X> _uniqueXs;

  /**
   * A set of unique y values
   */
  private transient SortedSet<Y> _uniqueYs;

  /**
   * A set of strips in the xy plane
   */
  private transient Map<X, Map<Y, List<ObjectsPair<Z, Double>>>> _strips;

  /**
   * For the builder.
   */
  /* package */VolatilityCubeData() {
  }

  /**
   * Constructor that uses the default axis labels.
   * @param definitionName The definition name, not null
   * @param specificationName The specification name, not null
   * @param values The (x, y, z, volatility) points, not null
   */
  public VolatilityCubeData(final String definitionName,
      final String specificationName,
      final Map<Triple<X, Y, Z>, Double> values) {
    this(definitionName, specificationName, DEFAULT_X_LABEL, DEFAULT_Y_LABEL, DEFAULT_Z_LABEL, values);
  }

  /**
   * @param definitionName The definition name, not null
   * @param specificationName The specification name, not null
   * @param xLabel The x axis label, not null
   * @param yLabel The y axis label, not null
   * @param zLabel The z axis label, not null
   * @param values The (x, y, z volatility) points, not null
   */
  public VolatilityCubeData(final String definitionName,
      final String specificationName,
      final String xLabel,
      final String yLabel,
      final String zLabel,
      final Map<Triple<X, Y, Z>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    setDefinitionName(definitionName);
    setSpecificationName(specificationName);
    setXLabel(xLabel);
    setYLabel(yLabel);
    setZLabel(zLabel);
    init(values);
  }

  /**
   * Initializes data structures and divides the cube into x-y strips.
   * @param values The values, not null
   */
  @SuppressWarnings("unchecked")
  private void init(final Map<Triple<X, Y, Z>, Double> values) {
    _values = values;
    _strips = new HashMap<>();
    _uniqueXs = new TreeSet<>();
    _uniqueYs = new TreeSet<>();
    final List<X> xs = new ArrayList<>();
    final List<Y> ys = new ArrayList<>();
    final List<Z> zs = new ArrayList<>();
    final List<Double> vs = new ArrayList<>();
    for (final Map.Entry<Triple<X, Y, Z>, Double> entries : values.entrySet()) {
      final X x = entries.getKey().getFirst();
      final Y y = entries.getKey().getSecond();
      final Z z = entries.getKey().getThird();
      if (!_strips.containsKey(x)) {
        final Map<Y, List<ObjectsPair<Z, Double>>> map = new HashMap<>();
        _strips.put(x, map);
        _uniqueXs.add(x);
      }
      if (!_strips.get(x).containsKey(y)) {
        final List<ObjectsPair<Z, Double>> list = new ArrayList<>();
        _strips.get(x).put(y, list);
        _uniqueYs.add(y);
      }
      xs.add(x);
      ys.add(y);
      zs.add(z);
      vs.add(entries.getValue());
      _strips.get(x).get(y).add(ObjectsPair.of(z, entries.getValue()));
    }
    _xs = (X[]) xs.toArray();
    _ys = (Y[]) ys.toArray();
    _zs = (Z[]) zs.toArray();
    _vs = vs.toArray(new Double[vs.size()]);
  }

  /**
   * Gets the number of points in this cube.
   * @return The number of points
   */
  public int size() {
    return _values.size();
  }

  /**
   * Gets the volatility for a particular x, y, z point.
   * @param x The x value
   * @param y The y value
   * @param z The z value
   * @return The volatility
   */
  public Double getVolatility(final X x, final Y y, final Z z) {
    return _values.get(Triple.of(x, y, z));
  }

  /**
   * Gets a sorted set of unique x values.
   * @return The unique x values
   */
  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }

  /**
   * Gets a sorted set of unique y values.
   * @return The unique y values
   */
  public SortedSet<Y> getUniqueYValues() {
    return _uniqueYs;
  }

  /**
   * Gets a slice through the surface in the x, y plane.
   * @param x The x value, not null
   * @param y The y value
   * @return A slice of the cube
   */
  public List<ObjectsPair<Z, Double>> getZValuesForXandY(final X x, final Y y) {
    ArgumentChecker.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x&y value " + x + ":" + y);
    }
    final Map<Y, List<ObjectsPair<Z, Double>>> result = _strips.get(x);
    if (result != null) {
      final List<ObjectsPair<Z, Double>> rr = result.get(y);
      Collections.sort(rr, COMPARATOR);
      return rr;
    }
    return Collections.emptyList();
  }

  /**
   * Gets the cube data as a map.
   * @return The cube data
   */
  public Map<Triple<X, Y, Z>, Double> asMap() {
    return _values;
  }

  /**
   * Sets a (x, y, z) to volatility map.
   * @param values  the new value of the property, not null
   */
  public void setValues(final Map<Triple<X, Y, Z>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    init(values);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilityCubeData}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static VolatilityCubeData.Meta meta() {
    return VolatilityCubeData.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code VolatilityCubeData}.
   * @param <R>  the first generic type
   * @param <S>  the second generic type
   * @param <T>  the second generic type
   * @param cls1  the first generic type
   * @param cls2  the second generic type
   * @param cls3  the third generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R, S, T> VolatilityCubeData.Meta<R, S, T> metaVolatilityCubeData(Class<R> cls1, Class<S> cls2, Class<T> cls3) {
    return VolatilityCubeData.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilityCubeData.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public VolatilityCubeData.Meta<X, Y, Z> metaBean() {
    return VolatilityCubeData.Meta.INSTANCE;
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
   * Gets the definition name.
   * @return the value of the property, not null
   */
  public String getDefinitionName() {
    return _definitionName;
  }

  /**
   * Sets the definition name.
   * @param definitionName  the new value of the property, not null
   */
  public void setDefinitionName(String definitionName) {
    JodaBeanUtils.notNull(definitionName, "definitionName");
    this._definitionName = definitionName;
  }

  /**
   * Gets the the {@code definitionName} property.
   * @return the property, not null
   */
  public final Property<String> definitionName() {
    return metaBean().definitionName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the specification name.
   * @return the value of the property, not null
   */
  public String getSpecificationName() {
    return _specificationName;
  }

  /**
   * Sets the specification name.
   * @param specificationName  the new value of the property, not null
   */
  public void setSpecificationName(String specificationName) {
    JodaBeanUtils.notNull(specificationName, "specificationName");
    this._specificationName = specificationName;
  }

  /**
   * Gets the the {@code specificationName} property.
   * @return the property, not null
   */
  public final Property<String> specificationName() {
    return metaBean().specificationName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a (x, y, z) to volatility map.
   * @return the value of the property, not null
   */
  public Map<Triple<X, Y, Z>, Double> getValues() {
    return _values;
  }

  /**
   * Gets the the {@code values} property.
   * @return the property, not null
   */
  public final Property<Map<Triple<X, Y, Z>, Double>> values() {
    return metaBean().values().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the x axis label.
   * @return the value of the property, not null
   */
  public String getXLabel() {
    return _xLabel;
  }

  /**
   * Sets the x axis label.
   * @param xLabel  the new value of the property, not null
   */
  public void setXLabel(String xLabel) {
    JodaBeanUtils.notNull(xLabel, "xLabel");
    this._xLabel = xLabel;
  }

  /**
   * Gets the the {@code xLabel} property.
   * @return the property, not null
   */
  public final Property<String> xLabel() {
    return metaBean().xLabel().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the y axis label.
   * @return the value of the property, not null
   */
  public String getYLabel() {
    return _yLabel;
  }

  /**
   * Sets the y axis label.
   * @param yLabel  the new value of the property, not null
   */
  public void setYLabel(String yLabel) {
    JodaBeanUtils.notNull(yLabel, "yLabel");
    this._yLabel = yLabel;
  }

  /**
   * Gets the the {@code yLabel} property.
   * @return the property, not null
   */
  public final Property<String> yLabel() {
    return metaBean().yLabel().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the z axis label
   * @return the value of the property, not null
   */
  public String getZLabel() {
    return _zLabel;
  }

  /**
   * Sets the z axis label
   * @param zLabel  the new value of the property, not null
   */
  public void setZLabel(String zLabel) {
    JodaBeanUtils.notNull(zLabel, "zLabel");
    this._zLabel = zLabel;
  }

  /**
   * Gets the the {@code zLabel} property.
   * @return the property, not null
   */
  public final Property<String> zLabel() {
    return metaBean().zLabel().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the x values
   * @return the value of the property, not null
   */
  public X[] getXs() {
    return _xs;
  }

  /**
   * Sets the x values
   * @param xs  the new value of the property, not null
   */
  public void setXs(X[] xs) {
    JodaBeanUtils.notNull(xs, "xs");
    this._xs = xs;
  }

  /**
   * Gets the the {@code xs} property.
   * @return the property, not null
   */
  public final Property<X[]> xs() {
    return metaBean().xs().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the y values
   * @return the value of the property, not null
   */
  public Y[] getYs() {
    return _ys;
  }

  /**
   * Sets the y values
   * @param ys  the new value of the property, not null
   */
  public void setYs(Y[] ys) {
    JodaBeanUtils.notNull(ys, "ys");
    this._ys = ys;
  }

  /**
   * Gets the the {@code ys} property.
   * @return the property, not null
   */
  public final Property<Y[]> ys() {
    return metaBean().ys().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the z values
   * @return the value of the property, not null
   */
  public Z[] getZs() {
    return _zs;
  }

  /**
   * Sets the z values
   * @param zs  the new value of the property, not null
   */
  public void setZs(Z[] zs) {
    JodaBeanUtils.notNull(zs, "zs");
    this._zs = zs;
  }

  /**
   * Gets the the {@code zs} property.
   * @return the property, not null
   */
  public final Property<Z[]> zs() {
    return metaBean().zs().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the volatilities
   * @return the value of the property, not null
   */
  public Double[] getVs() {
    return _vs;
  }

  /**
   * Sets the volatilities
   * @param vs  the new value of the property, not null
   */
  public void setVs(Double[] vs) {
    JodaBeanUtils.notNull(vs, "vs");
    this._vs = vs;
  }

  /**
   * Gets the the {@code vs} property.
   * @return the property, not null
   */
  public final Property<Double[]> vs() {
    return metaBean().vs().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public VolatilityCubeData<X, Y, Z> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VolatilityCubeData<?, ?, ?> other = (VolatilityCubeData<?, ?, ?>) obj;
      return JodaBeanUtils.equal(getDefinitionName(), other.getDefinitionName()) &&
          JodaBeanUtils.equal(getSpecificationName(), other.getSpecificationName()) &&
          JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getXLabel(), other.getXLabel()) &&
          JodaBeanUtils.equal(getYLabel(), other.getYLabel()) &&
          JodaBeanUtils.equal(getZLabel(), other.getZLabel()) &&
          JodaBeanUtils.equal(getXs(), other.getXs()) &&
          JodaBeanUtils.equal(getYs(), other.getYs()) &&
          JodaBeanUtils.equal(getZs(), other.getZs()) &&
          JodaBeanUtils.equal(getVs(), other.getVs());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getDefinitionName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSpecificationName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValues());
    hash += hash * 31 + JodaBeanUtils.hashCode(getXLabel());
    hash += hash * 31 + JodaBeanUtils.hashCode(getYLabel());
    hash += hash * 31 + JodaBeanUtils.hashCode(getZLabel());
    hash += hash * 31 + JodaBeanUtils.hashCode(getXs());
    hash += hash * 31 + JodaBeanUtils.hashCode(getYs());
    hash += hash * 31 + JodaBeanUtils.hashCode(getZs());
    hash += hash * 31 + JodaBeanUtils.hashCode(getVs());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(352);
    buf.append("VolatilityCubeData{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("definitionName").append('=').append(JodaBeanUtils.toString(getDefinitionName())).append(',').append(' ');
    buf.append("specificationName").append('=').append(JodaBeanUtils.toString(getSpecificationName())).append(',').append(' ');
    buf.append("values").append('=').append(JodaBeanUtils.toString(getValues())).append(',').append(' ');
    buf.append("xLabel").append('=').append(JodaBeanUtils.toString(getXLabel())).append(',').append(' ');
    buf.append("yLabel").append('=').append(JodaBeanUtils.toString(getYLabel())).append(',').append(' ');
    buf.append("zLabel").append('=').append(JodaBeanUtils.toString(getZLabel())).append(',').append(' ');
    buf.append("xs").append('=').append(JodaBeanUtils.toString(getXs())).append(',').append(' ');
    buf.append("ys").append('=').append(JodaBeanUtils.toString(getYs())).append(',').append(' ');
    buf.append("zs").append('=').append(JodaBeanUtils.toString(getZs())).append(',').append(' ');
    buf.append("vs").append('=').append(JodaBeanUtils.toString(getVs())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilityCubeData}.
   */
  public static class Meta<X, Y, Z> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code definitionName} property.
     */
    private final MetaProperty<String> _definitionName = DirectMetaProperty.ofReadWrite(
        this, "definitionName", VolatilityCubeData.class, String.class);
    /**
     * The meta-property for the {@code specificationName} property.
     */
    private final MetaProperty<String> _specificationName = DirectMetaProperty.ofReadWrite(
        this, "specificationName", VolatilityCubeData.class, String.class);
    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Triple<X, Y, Z>, Double>> _values = DirectMetaProperty.ofReadWrite(
        this, "values", VolatilityCubeData.class, (Class) Map.class);
    /**
     * The meta-property for the {@code xLabel} property.
     */
    private final MetaProperty<String> _xLabel = DirectMetaProperty.ofReadWrite(
        this, "xLabel", VolatilityCubeData.class, String.class);
    /**
     * The meta-property for the {@code yLabel} property.
     */
    private final MetaProperty<String> _yLabel = DirectMetaProperty.ofReadWrite(
        this, "yLabel", VolatilityCubeData.class, String.class);
    /**
     * The meta-property for the {@code zLabel} property.
     */
    private final MetaProperty<String> _zLabel = DirectMetaProperty.ofReadWrite(
        this, "zLabel", VolatilityCubeData.class, String.class);
    /**
     * The meta-property for the {@code xs} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<X[]> _xs = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "xs", VolatilityCubeData.class, Object[].class);
    /**
     * The meta-property for the {@code ys} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Y[]> _ys = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "ys", VolatilityCubeData.class, Object[].class);
    /**
     * The meta-property for the {@code zs} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Z[]> _zs = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
        this, "zs", VolatilityCubeData.class, Object[].class);
    /**
     * The meta-property for the {@code vs} property.
     */
    private final MetaProperty<Double[]> _vs = DirectMetaProperty.ofReadWrite(
        this, "vs", VolatilityCubeData.class, Double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "definitionName",
        "specificationName",
        "values",
        "xLabel",
        "yLabel",
        "zLabel",
        "xs",
        "ys",
        "zs",
        "vs");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -962772354:  // definitionName
          return _definitionName;
        case -583993810:  // specificationName
          return _specificationName;
        case -823812830:  // values
          return _values;
        case -786294436:  // xLabel
          return _xLabel;
        case -757665285:  // yLabel
          return _yLabel;
        case -729036134:  // zLabel
          return _zLabel;
        case 3835:  // xs
          return _xs;
        case 3866:  // ys
          return _ys;
        case 3897:  // zs
          return _zs;
        case 3773:  // vs
          return _vs;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VolatilityCubeData<X, Y, Z>> builder() {
      return new DirectBeanBuilder<VolatilityCubeData<X, Y, Z>>(new VolatilityCubeData<X, Y, Z>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends VolatilityCubeData<X, Y, Z>> beanType() {
      return (Class) VolatilityCubeData.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code definitionName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> definitionName() {
      return _definitionName;
    }

    /**
     * The meta-property for the {@code specificationName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> specificationName() {
      return _specificationName;
    }

    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Triple<X, Y, Z>, Double>> values() {
      return _values;
    }

    /**
     * The meta-property for the {@code xLabel} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> xLabel() {
      return _xLabel;
    }

    /**
     * The meta-property for the {@code yLabel} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> yLabel() {
      return _yLabel;
    }

    /**
     * The meta-property for the {@code zLabel} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> zLabel() {
      return _zLabel;
    }

    /**
     * The meta-property for the {@code xs} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<X[]> xs() {
      return _xs;
    }

    /**
     * The meta-property for the {@code ys} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Y[]> ys() {
      return _ys;
    }

    /**
     * The meta-property for the {@code zs} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Z[]> zs() {
      return _zs;
    }

    /**
     * The meta-property for the {@code vs} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double[]> vs() {
      return _vs;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -962772354:  // definitionName
          return ((VolatilityCubeData<?, ?, ?>) bean).getDefinitionName();
        case -583993810:  // specificationName
          return ((VolatilityCubeData<?, ?, ?>) bean).getSpecificationName();
        case -823812830:  // values
          return ((VolatilityCubeData<?, ?, ?>) bean).getValues();
        case -786294436:  // xLabel
          return ((VolatilityCubeData<?, ?, ?>) bean).getXLabel();
        case -757665285:  // yLabel
          return ((VolatilityCubeData<?, ?, ?>) bean).getYLabel();
        case -729036134:  // zLabel
          return ((VolatilityCubeData<?, ?, ?>) bean).getZLabel();
        case 3835:  // xs
          return ((VolatilityCubeData<?, ?, ?>) bean).getXs();
        case 3866:  // ys
          return ((VolatilityCubeData<?, ?, ?>) bean).getYs();
        case 3897:  // zs
          return ((VolatilityCubeData<?, ?, ?>) bean).getZs();
        case 3773:  // vs
          return ((VolatilityCubeData<?, ?, ?>) bean).getVs();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -962772354:  // definitionName
          ((VolatilityCubeData<X, Y, Z>) bean).setDefinitionName((String) newValue);
          return;
        case -583993810:  // specificationName
          ((VolatilityCubeData<X, Y, Z>) bean).setSpecificationName((String) newValue);
          return;
        case -823812830:  // values
          ((VolatilityCubeData<X, Y, Z>) bean).setValues((Map<Triple<X, Y, Z>, Double>) newValue);
          return;
        case -786294436:  // xLabel
          ((VolatilityCubeData<X, Y, Z>) bean).setXLabel((String) newValue);
          return;
        case -757665285:  // yLabel
          ((VolatilityCubeData<X, Y, Z>) bean).setYLabel((String) newValue);
          return;
        case -729036134:  // zLabel
          ((VolatilityCubeData<X, Y, Z>) bean).setZLabel((String) newValue);
          return;
        case 3835:  // xs
          ((VolatilityCubeData<X, Y, Z>) bean).setXs((X[]) newValue);
          return;
        case 3866:  // ys
          ((VolatilityCubeData<X, Y, Z>) bean).setYs((Y[]) newValue);
          return;
        case 3897:  // zs
          ((VolatilityCubeData<X, Y, Z>) bean).setZs((Z[]) newValue);
          return;
        case 3773:  // vs
          ((VolatilityCubeData<X, Y, Z>) bean).setVs((Double[]) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._definitionName, "definitionName");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._specificationName, "specificationName");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._values, "values");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._xLabel, "xLabel");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._yLabel, "yLabel");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._zLabel, "zLabel");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._xs, "xs");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._ys, "ys");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._zs, "zs");
      JodaBeanUtils.notNull(((VolatilityCubeData<?, ?, ?>) bean)._vs, "vs");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
