/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 * @param <T> Type of the x data
 * @param <U> Type of the y data
 */
public class NodalObjectsCurve<T extends Comparable<T>, U> extends ObjectsCurve<T, U> implements Bean {

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final T[] xData, final U[] yData) {
    return new NodalObjectsCurve<>(xData, yData, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Map<T, U> data) {
    return new NodalObjectsCurve<>(data, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Set<Pair<T, U>> data) {
    return new NodalObjectsCurve<>(data, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final List<T> xData, final List<U> yData) {
    return new NodalObjectsCurve<>(xData, yData, false);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final T[] xData, final U[] yData, final String name) {
    return new NodalObjectsCurve<>(xData, yData, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Map<T, U> data, final String name) {
    return new NodalObjectsCurve<>(data, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final Set<Pair<T, U>> data, final String name) {
    return new NodalObjectsCurve<>(data, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> from(final List<T> xData, final List<U> yData, final String name) {
    return new NodalObjectsCurve<>(xData, yData, false, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final T[] xData, final U[] yData) {
    return new NodalObjectsCurve<>(xData, yData, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Map<T, U> data) {
    return new NodalObjectsCurve<>(data, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Set<Pair<T, U>> data) {
    return new NodalObjectsCurve<>(data, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final List<T> xData, final List<U> yData) {
    return new NodalObjectsCurve<>(xData, yData, true);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final T[] xData, final U[] yData, final String name) {
    return new NodalObjectsCurve<>(xData, yData, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Map<T, U> data, final String name) {
    return new NodalObjectsCurve<>(data, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final Set<Pair<T, U>> data, final String name) {
    return new NodalObjectsCurve<>(data, true, name);
  }

  public static <T extends Comparable<T>, U> NodalObjectsCurve<T, U> fromSorted(final List<T> xData, final List<U> yData, final String name) {
    return new NodalObjectsCurve<>(xData, yData, true, name);
  }

  public NodalObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalObjectsCurve(final Map<T, U> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted) {
    super(data, isSorted);
  }

  public NodalObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted) {
    super(xData, yData, isSorted);
  }

  public NodalObjectsCurve(final T[] xData, final U[] yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  public NodalObjectsCurve(final Map<T, U> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalObjectsCurve(final Set<Pair<T, U>> data, final boolean isSorted, final String name) {
    super(data, isSorted, name);
  }

  public NodalObjectsCurve(final List<T> xData, final List<U> yData, final boolean isSorted, final String name) {
    super(xData, yData, isSorted, name);
  }

  @Override
  public U getYValue(final T x) {
    ArgumentChecker.notNull(x, "x");
    final int index = Arrays.binarySearch(getXData(), x);
    if (index < 0) {
      throw new IllegalArgumentException("Curve does not contain data for x point " + x);
    }
    return getYData()[index];
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code NodalObjectsCurve}.
   * @return the meta-bean, not null
   */
  public static NodalObjectsCurve.Meta meta() {
    return NodalObjectsCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(NodalObjectsCurve.Meta.INSTANCE);
  }

  @Override
  public NodalObjectsCurve.Meta metaBean() {
    return NodalObjectsCurve.Meta.INSTANCE;
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
  @Override
  public NodalObjectsCurve clone() {
    BeanBuilder<? extends NodalObjectsCurve> builder = metaBean().builder();
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
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("NodalObjectsCurve{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code NodalObjectsCurve}.
   */
  public static class Meta extends ObjectsCurve.Meta {
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
    public BeanBuilder<? extends NodalObjectsCurve> builder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends NodalObjectsCurve> beanType() {
      return NodalObjectsCurve.class;
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
