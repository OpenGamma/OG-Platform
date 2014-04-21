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

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A curve.
 * 
 * @param <T>  the type of the x data
 * @param <U>  the type of the y data
 */
@BeanDefinition
public class NodalObjectsCurve<T extends Comparable<T>, U>
    extends ObjectsCurve<T, U> {

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

  //-------------------------------------------------------------------------
  /**
   * Constructor for Joda-Beans.
   */
  protected NodalObjectsCurve() {
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

  //-------------------------------------------------------------------------
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
  @SuppressWarnings("rawtypes")
  public static NodalObjectsCurve.Meta meta() {
    return NodalObjectsCurve.Meta.INSTANCE;
  }

  /**
   * The meta-bean for {@code NodalObjectsCurve}.
   * @param <R>  the first generic type
   * @param <S>  the second generic type
   * @param cls1  the first generic type
   * @param cls2  the second generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R extends Comparable<R>, S> NodalObjectsCurve.Meta<R, S> metaNodalObjectsCurve(Class<R> cls1, Class<S> cls2) {
    return NodalObjectsCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(NodalObjectsCurve.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public NodalObjectsCurve.Meta<T, U> metaBean() {
    return NodalObjectsCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  @Override
  public NodalObjectsCurve<T, U> clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

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
    buf.append("NodalObjectsCurve{");
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
   * The meta-bean for {@code NodalObjectsCurve}.
   */
  public static class Meta<T extends Comparable<T>, U> extends ObjectsCurve.Meta<T, U> {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
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
    public BeanBuilder<? extends NodalObjectsCurve<T, U>> builder() {
      return new DirectBeanBuilder<NodalObjectsCurve<T, U>>(new NodalObjectsCurve<T, U>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends NodalObjectsCurve<T, U>> beanType() {
      return (Class) NodalObjectsCurve.class;
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
