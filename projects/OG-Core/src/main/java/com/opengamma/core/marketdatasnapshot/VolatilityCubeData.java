/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

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
  private X[] _xs;

  /**
   * The y values
   */
  private Y[] _ys;

  /**
   * The z values
   */
  private Z[] _zs;

  /**
   * The volatilities
   */
  private Double[] _vs;

  /**
   * A set of unique x values
   */
  private SortedSet<X> _uniqueXs;

  /**
   * A set of unique y values
   */
  private SortedSet<Y> _uniqueYs;

  /**
   * A set of strips in the xy plane
   */
  private Map<X, Map<Y, List<ObjectsPair<Z, Double>>>> _strips;

  /**
   * For the builder.
   */
  /* package */ VolatilityCubeData() {
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
    JodaBeanUtils.notNull(values, "values");
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
      if (!_strips.containsKey(entries.getKey().getFirst())) {
        final Map<Y, List<ObjectsPair<Z, Double>>> map = newHashMap();
        _strips.put(entries.getKey().getFirst(), map);
        _uniqueXs.add(entries.getKey().getFirst());
      }
      if (!_strips.get(entries.getKey().getFirst()).containsKey(entries.getKey().getSecond())) {
        final List<ObjectsPair<Z, Double>> list = newArrayList();
        _strips.get(entries.getKey().getFirst()).put(entries.getKey().getSecond(), list);
        _uniqueYs.add(entries.getKey().getSecond());
      }
      xs.add(entries.getKey().getFirst());
      ys.add(entries.getKey().getSecond());
      zs.add(entries.getKey().getThird());
      vs.add(entries.getValue());
      _strips.get(entries.getKey().getFirst()).get(entries.getKey().getSecond()).add(ObjectsPair.of(entries.getKey().getThird(), entries.getValue()));
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
   * Gets the x values.
   * @return The x values.
   */
  public X[] getXs() {
    return _xs;
  }

  /**
   * Gets the y values.
   * @return The y values
   */
  public Y[] getYs() {
    return _ys;
  }

  /**
   * Gets the z values.
   * @return The z values
   */
  public Z[] getZs() {
    return _zs;
  }

  /**
   * Gets the volatilities.
   * @return The volatilities
   */
  public Double[] getVs() {
    return _vs;
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
    JodaBeanUtils.notNull(values, "values");
    init(values);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilityCubeData}.
   * @return the meta-bean, not null
   */
  public static VolatilityCubeData.Meta meta() {
    return VolatilityCubeData.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilityCubeData.Meta.INSTANCE);
  }

  @Override
  public VolatilityCubeData.Meta metaBean() {
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
  @Override
  public VolatilityCubeData clone() {
    BeanBuilder<? extends VolatilityCubeData> builder = metaBean().builder();
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
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VolatilityCubeData other = (VolatilityCubeData) obj;
      return JodaBeanUtils.equal(getDefinitionName(), other.getDefinitionName()) &&
          JodaBeanUtils.equal(getSpecificationName(), other.getSpecificationName()) &&
          JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getXLabel(), other.getXLabel()) &&
          JodaBeanUtils.equal(getYLabel(), other.getYLabel()) &&
          JodaBeanUtils.equal(getZLabel(), other.getZLabel());
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
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
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
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilityCubeData}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
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
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "definitionName",
        "specificationName",
        "values",
        "xLabel",
        "yLabel",
        "zLabel");

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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VolatilityCubeData> builder() {
      return new DirectBeanBuilder<VolatilityCubeData>(new VolatilityCubeData());
    }

    @Override
    public Class<? extends VolatilityCubeData> beanType() {
      return VolatilityCubeData.class;
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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -962772354:  // definitionName
          return ((VolatilityCubeData) bean).getDefinitionName();
        case -583993810:  // specificationName
          return ((VolatilityCubeData) bean).getSpecificationName();
        case -823812830:  // values
          return ((VolatilityCubeData) bean).getValues();
        case -786294436:  // xLabel
          return ((VolatilityCubeData) bean).getXLabel();
        case -757665285:  // yLabel
          return ((VolatilityCubeData) bean).getYLabel();
        case -729036134:  // zLabel
          return ((VolatilityCubeData) bean).getZLabel();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -962772354:  // definitionName
          ((VolatilityCubeData) bean).setDefinitionName((String) newValue);
          return;
        case -583993810:  // specificationName
          ((VolatilityCubeData) bean).setSpecificationName((String) newValue);
          return;
        case -823812830:  // values
          ((VolatilityCubeData) bean).setValues((Map<Triple<X, Y, Z>, Double>) newValue);
          return;
        case -786294436:  // xLabel
          ((VolatilityCubeData) bean).setXLabel((String) newValue);
          return;
        case -757665285:  // yLabel
          ((VolatilityCubeData) bean).setYLabel((String) newValue);
          return;
        case -729036134:  // zLabel
          ((VolatilityCubeData) bean).setZLabel((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._definitionName, "definitionName");
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._specificationName, "specificationName");
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._values, "values");
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._xLabel, "xLabel");
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._yLabel, "yLabel");
      JodaBeanUtils.notNull(((VolatilityCubeData) bean)._zLabel, "zLabel");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
