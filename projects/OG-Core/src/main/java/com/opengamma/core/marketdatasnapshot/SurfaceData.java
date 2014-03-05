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
import com.opengamma.util.tuple.Pairs;

/**
 * Data structure to hold the data points of a surface.
 * Note no interpolation or fitting is done in this code.
 * 
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
@BeanDefinition
public class SurfaceData<X, Y> implements Bean, Serializable {

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
   * Comparator for the pairs.
   */
  private static final Comparator<Pair<?, ?>> COMPARATOR = FirstThenSecondPairComparator.INSTANCE;

  /**
   * The name.
   */
  @PropertyDefinition(validate = "notNull")
  private String _name;

  /**
   * A (x, y) to value map.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Map<Pair<X, Y>, Double> _values;

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
   * The x values
   */
  private X[] _xs;

  /**
   * The y values
   */
  private Y[] _ys;

  /**
   * The volatilities
   */
  private Double[] _vs;

  /**
   * A set of unique x values
   */
  private SortedSet<X> _uniqueXs;

  /**
   * A set of strips in the x plane
   */
  private Map<X, List<ObjectsPair<Y, Double>>> _strips;

  /**
   * For the builder.
   */
  /* package */SurfaceData() {
  }

  /**
   * Constructor that uses the default axis labels.
   * @param name The surface name, not null
   * @param values The (x, y, value) points, not null
   */
  public SurfaceData(final String name, final Map<Pair<X, Y>, Double> values) {
    this(name, DEFAULT_X_LABEL, DEFAULT_Y_LABEL, values);
  }

  /**
   * @param name The name, not null
   * @param xLabel The x axis label, not null
   * @param yLabel The y axis label, not null
   * @param values The (x, y, value) points, not null
   */
  public SurfaceData(final String name,
      final String xLabel,
      final String yLabel,
      final Map<Pair<X, Y>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    setName(name);
    setXLabel(xLabel);
    setYLabel(yLabel);
    init(values);
  }

  /**
   * Initializes data structures and divides the surface into x strips.
   * @param values The values, not null
   */
  private void init(final Map<Pair<X, Y>, Double> values) {
    _values = values;
    _strips = new HashMap<>();
    _uniqueXs = new TreeSet<>();
    final List<X> xs = new ArrayList<>();
    final List<Y> ys = new ArrayList<>();
    final List<Double> vs = new ArrayList<>();
    for (final Map.Entry<Pair<X, Y>, Double> entries : values.entrySet()) {
      final X x = entries.getKey().getFirst();
      final Y y = entries.getKey().getSecond();
      if (!_strips.containsKey(x)) {
        final List<ObjectsPair<Y, Double>> map = new ArrayList<>();
        _strips.put(x, map);
        _uniqueXs.add(x);
      }
      xs.add(x);
      ys.add(y);
      vs.add(entries.getValue());
      _strips.get(x).add(ObjectsPair.of(y, entries.getValue()));
    }
    _xs = (X[]) xs.toArray();
    _ys = (Y[]) ys.toArray();
    _vs = vs.toArray(new Double[vs.size()]);
  }

  /**
   * Gets the number of points in this surface.
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
   * Gets the values.
   * @return The values
   */
  public Double[] getVs() {
    return _vs;
  }

  /**
   * Gets the value for a particular x, y point.
   * @param x The x value
   * @param y The y value
   * @return The value
   */
  public Double getValue(final X x, final Y y) {
    return _values.get(Pairs.of(x, y));
  }

  /**
   * Gets a sorted set of unique x values.
   * @return The unique x values
   */
  public SortedSet<X> getUniqueXValues() {
    return _uniqueXs;
  }

  /**
   * Gets a slice through the surface in the x plane.
   * @param x The x value, not null
   * @return A slice of the cube
   */
  public List<ObjectsPair<Y, Double>> getYValuesForX(final X x) {
    ArgumentChecker.notNull(x, "x");
    if (!_strips.containsKey(x)) {
      throw new OpenGammaRuntimeException("Could not get strip for x value " + x);
    }
    final List<ObjectsPair<Y, Double>> result = _strips.get(x);
    if (result != null) {
      Collections.sort(result, COMPARATOR);
      return result;
    }
    return Collections.emptyList();
  }

  /**
   * Gets the surface data as a map.
   * @return The surface data
   */
  public Map<Pair<X, Y>, Double> asMap() {
    return _values;
  }

  /**
   * Sets a (x, y) to value map.
   * @param values  the new value of the property, not null
   */
  public void setValues(final Map<Pair<X, Y>, Double> values) {
    ArgumentChecker.notNull(values, "values");
    init(values);
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code SurfaceData}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static SurfaceData.Meta meta() {
    return SurfaceData.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code SurfaceData}.
   * @param <R>  the first generic type
   * @param <S>  the second generic type
   * @param cls1  the first generic type
   * @param cls2  the second generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R, S> SurfaceData.Meta<R, S> metaSurfaceData(Class<R> cls1, Class<S> cls2) {
    return SurfaceData.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SurfaceData.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SurfaceData.Meta<X, Y> metaBean() {
    return SurfaceData.Meta.INSTANCE;
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
   * Gets the name.
   * @return the value of the property, not null
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
   * @param name  the new value of the property, not null
   */
  public void setName(String name) {
    JodaBeanUtils.notNull(name, "name");
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets a (x, y) to value map.
   * @return the value of the property, not null
   */
  public Map<Pair<X, Y>, Double> getValues() {
    return _values;
  }

  /**
   * Gets the the {@code values} property.
   * @return the property, not null
   */
  public final Property<Map<Pair<X, Y>, Double>> values() {
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
  @Override
  public SurfaceData<X, Y> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      SurfaceData<?, ?> other = (SurfaceData<?, ?>) obj;
      return JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getXLabel(), other.getXLabel()) &&
          JodaBeanUtils.equal(getYLabel(), other.getYLabel());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValues());
    hash += hash * 31 + JodaBeanUtils.hashCode(getXLabel());
    hash += hash * 31 + JodaBeanUtils.hashCode(getYLabel());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("SurfaceData{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("values").append('=').append(JodaBeanUtils.toString(getValues())).append(',').append(' ');
    buf.append("xLabel").append('=').append(JodaBeanUtils.toString(getXLabel())).append(',').append(' ');
    buf.append("yLabel").append('=').append(JodaBeanUtils.toString(getYLabel())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SurfaceData}.
   */
  public static class Meta<X, Y> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", SurfaceData.class, String.class);
    /**
     * The meta-property for the {@code values} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<Pair<X, Y>, Double>> _values = DirectMetaProperty.ofReadWrite(
        this, "values", SurfaceData.class, (Class) Map.class);
    /**
     * The meta-property for the {@code xLabel} property.
     */
    private final MetaProperty<String> _xLabel = DirectMetaProperty.ofReadWrite(
        this, "xLabel", SurfaceData.class, String.class);
    /**
     * The meta-property for the {@code yLabel} property.
     */
    private final MetaProperty<String> _yLabel = DirectMetaProperty.ofReadWrite(
        this, "yLabel", SurfaceData.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "name",
        "values",
        "xLabel",
        "yLabel");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return _name;
        case -823812830:  // values
          return _values;
        case -786294436:  // xLabel
          return _xLabel;
        case -757665285:  // yLabel
          return _yLabel;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SurfaceData<X, Y>> builder() {
      return new DirectBeanBuilder<SurfaceData<X, Y>>(new SurfaceData<X, Y>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends SurfaceData<X, Y>> beanType() {
      return (Class) SurfaceData.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<Pair<X, Y>, Double>> values() {
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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return ((SurfaceData<?, ?>) bean).getName();
        case -823812830:  // values
          return ((SurfaceData<?, ?>) bean).getValues();
        case -786294436:  // xLabel
          return ((SurfaceData<?, ?>) bean).getXLabel();
        case -757665285:  // yLabel
          return ((SurfaceData<?, ?>) bean).getYLabel();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          ((SurfaceData<X, Y>) bean).setName((String) newValue);
          return;
        case -823812830:  // values
          ((SurfaceData<X, Y>) bean).setValues((Map<Pair<X, Y>, Double>) newValue);
          return;
        case -786294436:  // xLabel
          ((SurfaceData<X, Y>) bean).setXLabel((String) newValue);
          return;
        case -757665285:  // yLabel
          ((SurfaceData<X, Y>) bean).setYLabel((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((SurfaceData<?, ?>) bean)._name, "name");
      JodaBeanUtils.notNull(((SurfaceData<?, ?>) bean)._values, "values");
      JodaBeanUtils.notNull(((SurfaceData<?, ?>) bean)._xLabel, "xLabel");
      JodaBeanUtils.notNull(((SurfaceData<?, ?>) bean)._yLabel, "yLabel");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
