/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * A standard immutable triple implementation consisting of three elements.
 * <p>
 * This implementation refers to the elements as 'first', 'second' and 'third'.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the triple, then the triple
 * itself effectively becomes mutable.
 * <p>
 * This class is immutable and thread-safe if the stored objects are immutable.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 */
public final class Triple<A, B, C> implements ImmutableBean, Comparable<Triple<A, B, C>>, Serializable {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  private final A _first;
  /** The second element. */
  private final B _second;
  /** The third element. */
  private final C _third;

  /**
   * Factory method creating a triple inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param <C> the third element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   * @return a triple formed from the three parameters, not null
   */
  public static <A, B, C> Triple<A, B, C> of(A first, B second, C third) {
    return new Triple<A, B, C>(first, second, third);
  }

  /**
   * Constructs a triple.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   * @deprecated Use of(first, second, third)
   */
  @Deprecated
  public Triple(A first, B second, C third) {
    _first = first;
    _second = second;
    _third = third;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the first element from this pair.
   * 
   * @return the first element, may be null
   */
  public A getFirst() {
    return _first;
  }

  /**
   * Gets the second element from this pair.
   * 
   * @return the second element, may be null
   */
  public B getSecond() {
    return _second;
  }

  /**
   * Gets the third element from this pair.
   * 
   * @return the third element, may be null
   */
  public C getThird() {
    return _third;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the elements from this triple as a list.
   * <p>
   * This method supports auto-casting as they is no way in generics to provide
   * a more specific type.
   * 
   * @param <T> an auto-cast list type
   * @return the elements as a list, not null
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> toList() {
    ArrayList<Object> list = new ArrayList<Object>();
    list.add(getFirst());
    list.add(getSecond());
    list.add(getThird());
    return (List<T>) list;
  }

  /**
   * Gets the first and second elements from this triple as a pair.
   * 
   * @return the first and second elements, not null
   */
  public Pair<A, B> toFirstPair() {
    return Pairs.ofOptimized(getFirst(), getSecond());
  }

  /**
   * Gets the first and second elements from this triple as a pair.
   * 
   * @return the second and third elements, not null
   */
  public Pair<B, C> toSecondPair() {
    return Pairs.ofOptimized(getSecond(), getThird());
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the pair based on the first element followed by the second element.
   * 
   * @param other  the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(Triple<A, B, C> other) {
    return new CompareToBuilder().append(_first, other._first)
        .append(_second, other._second).append(_third, other._third).toComparison();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Triple<?, ?, ?>) {
      Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
      return ObjectUtils.equals(getFirst(), other.getFirst()) &&
          ObjectUtils.equals(getSecond(), other.getSecond()) &&
          ObjectUtils.equals(getThird(), other.getThird());
    }
    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((getFirst() == null) ? 0 : getFirst().hashCode());
    result = prime * result + ((getSecond() == null) ? 0 : getSecond().hashCode());
    result = prime * result + ((getThird() == null) ? 0 : getThird().hashCode());
    return result;
  }

  @Override
  public String toString() {
    return new StringBuilder()
        .append("[")
        .append(getFirst())
        .append(", ")
        .append(getSecond())
        .append(", ")
        .append(getThird())
        .append("]").toString();
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code Triple}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static Triple.Meta meta() {
    return Triple.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Triple.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Triple.Meta<A, B, C> metaBean() {
    return Triple.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  @Override
  public Triple<A, B, C> clone() {
    return this;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Triple}.
   */
  public static final class Meta<A, B, C> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Object> _first = DirectMetaProperty.ofDerived(
        this, "first", Triple.class, Object.class);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Object> _second = DirectMetaProperty.ofDerived(
        this, "second", Triple.class, Object.class);
    /**
     * The meta-property for the {@code third} property.
     */
    private final MetaProperty<Object> _third = DirectMetaProperty.ofDerived(
        this, "third", Triple.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "first",
        "second",
        "third");

    /**
     * Restricted constructor.
     */
    Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName) {
        case "first":
          return _first;
        case "second":
          return _second;
        case "third":
          return _third;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public Triple.Builder builder() {
      return new Triple.Builder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends Triple> beanType() {
      return Triple.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code first} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Object> first() {
      return _first;
    }

    /**
     * The meta-property for the {@code second} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Object> second() {
      return _second;
    }

    /**
     * The meta-property for the {@code third} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Object> third() {
      return _third;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("rawtypes")
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName) {
        case "first":
          return ((Triple) bean).getFirst();
        case "second":
          return ((Triple) bean).getSecond();
        case "third":
          return ((Triple) bean).getThird();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code Triple}.
   */
  @SuppressWarnings({"unchecked", "rawtypes" })
  private static final class Builder extends DirectFieldsBeanBuilder<Triple> {

    /** The first element. */
    private Object _first;
    /** The second element. */
    private Object _second;
    /** The third element. */
    private Object _third;

    /**
     * Restricted constructor.
     */
    private Builder() {
      super();
    }

    //-----------------------------------------------------------------------
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName) {
        case "first":
          _first = newValue;
          break;
        case "second":
          _second = newValue;
          break;
        case "third":
          _third = newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Triple build() {
      return new Triple(_first, _second, _third);
    }

  }

}
