/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * An immutable pair consisting of two {@code Object} elements.
 * <p>
 * Although the implementation is immutable, there is no restriction on the objects
 * that may be stored. If mutable objects are stored in the pair, then the pair
 * itself effectively becomes mutable.
 * <p>
 * This class is immutable and thread-safe if the stored objects are immutable.
 *
 * @param <A> the type of the first side of the pair
 * @param <B> the type of the second side of the pair
 */
public final class ObjectsPair<A, B>
    extends Pair<A, B>
    implements Serializable {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final A first; // CSIGNORE
  /** The second element. */
  public final B second; // CSIGNORE

  //-------------------------------------------------------------------------
  /**
   * Creates a pair inferring the types.
   * 
   * @param <A> the first element type
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <A, B> ObjectsPair<A, B> of(A first, B second) {
    return new ObjectsPair<>(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a pair.
   * 
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @deprecated Use of(first, second)
   */
  @Deprecated
  public ObjectsPair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public A getFirst() {
    return first;
  }

  @Override
  public B getSecond() {
    return second;
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code ObjectsPair}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static ObjectsPair.Meta meta() {
    return ObjectsPair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ObjectsPair.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectsPair.Meta<A, B> metaBean() {
    return ObjectsPair.Meta.INSTANCE;
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
  public ObjectsPair<A, B> clone() {
    return this;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ObjectsPair}.
   */
  public static final class Meta<A, B> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Object> _first = DirectMetaProperty.ofImmutable(
        this, "first", ObjectsPair.class, Object.class);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Object> _second = DirectMetaProperty.ofImmutable(
        this, "second", ObjectsPair.class, Object.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "first",
        "second");

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
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ObjectsPair.Builder builder() {
      return new ObjectsPair.Builder();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends ObjectsPair> beanType() {
      return ObjectsPair.class;
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

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("rawtypes")
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName) {
        case "first":
          return ((ObjectsPair) bean).getFirst();
        case "second":
          return ((ObjectsPair) bean).getSecond();
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
   * The bean-builder for {@code ObjectsPair}.
   */
  @SuppressWarnings({"rawtypes" })
  private static final class Builder extends DirectFieldsBeanBuilder<ObjectsPair> {

    /** The first element. */
    private Object _first;
    /** The second element. */
    private Object _second;

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
    public ObjectsPair build() {
      return ObjectsPair.of(_first, _second);
    }

  }

}
