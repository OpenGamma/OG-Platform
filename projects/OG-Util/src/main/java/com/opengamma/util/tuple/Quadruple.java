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
import org.joda.beans.impl.BasicImmutableBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * A standard immutable quadruple implementation consisting of three elements.
 * <p>
 * This implementation refers to the elements as 'first', 'second', 'third' and 'fourth'.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the quadruple, then the quadruple
 * itself effectively becomes mutable.
 * <p>
 * This class is immutable and thread-safe if the stored objects are immutable.
 *
 * @param <A> the first element type
 * @param <B> the second element type
 * @param <C> the third element type
 * @param <D> the fourth element type
 */
public final class Quadruple<A, B, C, D>
    implements ImmutableBean, Comparable<Quadruple<A, B, C, D>>, Serializable {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  private final A _first;
  /** The second element. */
  private final B _second;
  /** The third element. */
  private final C _third;
  /** The fourth element. */
  private final D _fourth;

  /**
   * Factory method creating a quadruple inferring the types.
   *
   * @param <A> the first element type
   * @param <B> the second element type
   * @param <C> the third element type
   * @param <D> the fourth element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   * @param fourth the fourth element, may be null    
   * @return a quadruple formed from the three parameters, not null
   */
  public static <A, B, C, D> Quadruple<A, B, C, D> of(A first, B second, C third, D fourth) {
    return new Quadruple<>(first, second, third, fourth);
  }

  /**
   * Constructs a quadruple.
   *
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @param third  the third element, may be null
   * @param fourth the fourth element, may be null
   */
  private Quadruple(A first, B second, C third, D fourth) {
    _first = first;
    _second = second;
    _third = third;
    _fourth = fourth;
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

  /**
   * Gets the fourth element from this pair.
   *
   * @return the fourth element, may be null
   */
  public D getFourth() {
    return _fourth;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the elements from this quadruple as a list.
   * <p>
   * This method supports auto-casting as they is no way in generics to provide
   * a more specific type.
   *
   * @param <T> an auto-cast list type
   * @return the elements as a list, not null
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> toList() {
    ArrayList<Object> list = new ArrayList<>();
    list.add(getFirst());
    list.add(getSecond());
    list.add(getThird());
    list.add(getFourth());
    return (List<T>) list;
  }

  //-------------------------------------------------------------------------
  /**
   * Compares the quadruple based on the first element followed by the second
   * element followed by the third element followed by the fourth element.
   * <p>
   * The element types must be {@code Comparable}.
   *
   * @param other  the other pair, not null
   * @return negative if this is less, zero if equal, positive if greater
   */
  @Override
  public int compareTo(Quadruple<A, B, C, D> other) {
    return new CompareToBuilder()
        .append(_first, other._first)
        .append(_second, other._second)
        .append(_third, other._third)
        .append(_fourth, other._fourth)
        .toComparison();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof Quadruple<?, ?, ?, ?>) {
      Quadruple<?, ?, ?, ?> other = (Quadruple<?, ?, ?, ?>) obj;
      return ObjectUtils.equals(getFirst(), other.getFirst()) &&
          ObjectUtils.equals(getSecond(), other.getSecond()) &&
          ObjectUtils.equals(getThird(), other.getThird()) &&
          ObjectUtils.equals(getFourth(), other.getFourth());
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
    result = prime * result + ((getFourth() == null) ? 0 : getFourth().hashCode());
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
        .append(", ")
        .append(getFourth())
        .append("]").toString();
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code Triple}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static Quadruple.Meta meta() {
    return Quadruple.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Quadruple.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Quadruple.Meta<A, B, C, D> metaBean() {
    return Quadruple.Meta.INSTANCE;
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
  public Quadruple<A, B, C, D> clone() {
    return this;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Triple}.
   */
  public static final class Meta<A, B, C, D> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Object> _first = DirectMetaProperty.ofDerived(
        this, "first", Quadruple.class, Object.class);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Object> _second = DirectMetaProperty.ofDerived(
        this, "second", Quadruple.class, Object.class);
    /**
     * The meta-property for the {@code third} property.
     */
    private final MetaProperty<Object> _third = DirectMetaProperty.ofDerived(
        this, "third", Quadruple.class, Object.class);
    /**
     * The meta-property for the {@code fourth} property.
     */
    private final MetaProperty<Object> _fourth = DirectMetaProperty.ofDerived(
        this, "fourth", Quadruple.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "first",
        "second",
        "third",
        "fourth");

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
        case "fourth":
          return _fourth;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public Quadruple.Builder builder() {
      return new Quadruple.Builder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends Quadruple> beanType() {
      return Quadruple.class;
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

    /**
     * The meta-property for the {@code fourth} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Object> fourth() {
      return _fourth;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("rawtypes")
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName) {
        case "first":
          return ((Quadruple) bean).getFirst();
        case "second":
          return ((Quadruple) bean).getSecond();
        case "third":
          return ((Quadruple) bean).getThird();
        case "fourth":
          return ((Quadruple) bean).getFourth();
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
  private static final class Builder extends BasicImmutableBeanBuilder<Quadruple> {

    /** The first element. */
    private Object _first;
    /** The second element. */
    private Object _second;
    /** The third element. */
    private Object _third;
    /** The fourth element. */
    private Object _fourth;

    /**
     * Restricted constructor.
     */
    private Builder() {
      super(Quadruple.Meta.INSTANCE);
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
        case "fourth":
          _fourth = newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Quadruple build() {
      return new Quadruple(_first, _second, _third, _fourth);
    }

  }

}
