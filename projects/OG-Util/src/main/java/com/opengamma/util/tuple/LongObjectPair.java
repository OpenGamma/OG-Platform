/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
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
 * An immutable pair consisting of an {@code long} and {@code Object}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe if the stored object is immutable.
 *
 * @param <T> the type of the second side of the pair
 */
public class LongObjectPair<T>
    extends Pair<Long, T>
    implements Long2ObjectMap.Entry<T> {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final long first; // CSIGNORE
  /** The second element. */
  public final T second; // CSIGNORE

  //-------------------------------------------------------------------------
  /**
   * Creates a pair inferring the types.
   * 
   * @param <B> the second element type
   * @param first  the first element, may be null
   * @param second  the second element, may be null
   * @return a pair formed from the two parameters, not null
   */
  public static <B> LongObjectPair<B> of(final long first, final B second) {
    return new LongObjectPair<>(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a pair.
   * 
   * @param first  the first element
   * @param second  the second element
   * @deprecated Use public factory of(long,Object)
   */
  @Deprecated
  public LongObjectPair(final long first, final T second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getFirst() {
    return first;
  }

  @Override
  public T getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code long}.
   * 
   * @return the primitive
   */
  public long getFirstLong() {
    return first;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return first;
  }

  @Override
  public T setValue(final T value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code LongObjectPair}.
   * @return the meta-bean, not null
   */
  @SuppressWarnings("rawtypes")
  public static LongObjectPair.Meta meta() {
    return LongObjectPair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(LongObjectPair.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public LongObjectPair.Meta<T> metaBean() {
    return LongObjectPair.Meta.INSTANCE;
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
  public LongObjectPair<T> clone() {
    return this;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LongObjectPair) {
      final LongObjectPair<T> other = (LongObjectPair<T>) obj;
      return this.first == other.first && Objects.equals(this.second, other.second);
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    return ((int) (first ^ (first >>> 32))) ^ second.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code LongObjectPair}.
   */
  public static final class Meta<T> extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Long> _first = DirectMetaProperty.ofImmutable(
        this, "first", LongObjectPair.class, Long.TYPE);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Object> _second = DirectMetaProperty.ofImmutable(
        this, "second", LongObjectPair.class, Object.class);
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
    public LongObjectPair.Builder builder() {
      return new LongObjectPair.Builder();
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Class<? extends LongObjectPair> beanType() {
      return LongObjectPair.class;
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
    public MetaProperty<Long> first() {
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
          return ((LongObjectPair) bean).getFirst();
        case "second":
          return ((LongObjectPair) bean).getSecond();
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
   * The bean-builder for {@code LongObjectPair}.
   */
  @SuppressWarnings({"rawtypes" })
  private static final class Builder extends DirectFieldsBeanBuilder<LongObjectPair> {

    /** The first element. */
    private long _first;
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
          _first = (Long) newValue;
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
    public LongObjectPair build() {
      return LongObjectPair.of(_first, _second);
    }

  }

}
