/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.util.ArgumentChecker;

/**
 * An immutable pair consisting of an {@code int} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe.
 */
public class IntDoublePair
    extends Pair<Integer, Double>
    implements Int2DoubleMap.Entry {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final int first; // CSIGNORE
  /** The second element. */
  public final double second; // CSIGNORE

  //-------------------------------------------------------------------------
  /**
   * Obtains an {@code IntDoublePair} from a {@code Pair}.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static IntDoublePair of(final Pair<Integer, Double> pair) {
    if (pair instanceof IntDoublePair) {
      return (IntDoublePair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new IntDoublePair(pair.getFirst(), pair.getSecond());
  }

  /**
   * Obtains an {@code IntDoublePair} from two values.
   * 
   * @param first  the first element
   * @param second  the second element
   * @return a pair formed from the two parameters, not null
   */
  public static IntDoublePair of(final int first, final double second) {
    return new IntDoublePair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Parses an {@code IntDoublePair} from the standard string format.
   * <p>
   * The standard format is '[$first, $second]'. Spaces around the values are trimmed.
   * 
   * @param pairStr  the text to parse, not null
   * @return the parsed pair, not null
   */
  @FromString
  public static IntDoublePair parse(final String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() < 5) {
      throw new IllegalArgumentException("Invalid pair format, too short: " + pairStr);
    }
    if (pairStr.charAt(0) != '[') {
      throw new IllegalArgumentException("Invalid pair format, must start with [: " + pairStr);
    }
    if (pairStr.charAt(pairStr.length() - 1) != ']') {
      throw new IllegalArgumentException("Invalid pair format, must end with ]: " + pairStr);
    }
    String[] split = StringUtils.split(pairStr.substring(1, pairStr.length() - 1), ',');
    if (split.length != 2) {
      throw new IllegalArgumentException("Invalid pair format, must have two values: " + pairStr);
    }
    int first = Integer.parseInt(split[0].trim());
    double second = Double.parseDouble(split[1].trim());
    return new IntDoublePair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a pair.
   * 
   * @param first  the first element
   * @param second  the second element
   * @deprecated Use public factory of(int,double)
   */
  @Deprecated
  public IntDoublePair(final int first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Integer getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code int}.
   * 
   * @return the primitive
   */
  public int getFirstInt() {
    return first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * 
   * @return the primitive
   */
  public double getSecondDouble() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public int getIntKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(final double value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code IntDoublePair}.
   * @return the meta-bean, not null
   */
  public static IntDoublePair.Meta meta() {
    return IntDoublePair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(IntDoublePair.Meta.INSTANCE);
  }

  @Override
  public IntDoublePair.Meta metaBean() {
    return IntDoublePair.Meta.INSTANCE;
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
  public IntDoublePair clone() {
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(Pair<Integer, Double> other) {
    if (other instanceof IntDoublePair) {
      return compareTo((IntDoublePair) other);
    }
    return super.compareTo(other);
  }

  /**
   * Compares this pair to another.
   * <p>
   * This compares the first elements, then the second elements.
   * 
   * @param other  the other pair
   * @return negative if this is less, zero if equal, positive if greater
   */
  public int compareTo(IntDoublePair other) {
    int cmp = Integer.compare(first, other.first);
    if (cmp == 0) {
      cmp = Double.compare(second, other.second);
    }
    return cmp;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof IntDoublePair) {
      final IntDoublePair other = (IntDoublePair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long d = Double.doubleToLongBits(second);
    return first ^ ((int) (d ^ (d >>> 32)));
  }

  /**
   * Gets the pair using a standard string format.
   * <p>
   * The standard format is '[$first, $second]'. Spaces around the values are trimmed.
   * 
   * @return the pair as a string, not null
   */
  @Override
  @ToString
  public String toString() {
    return new StringBuilder()
        .append("[")
        .append(first)
        .append(", ")
        .append(second)
        .append("]").toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code IntDoublePair}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Integer> _first = DirectMetaProperty.ofImmutable(
        this, "first", IntDoublePair.class, Integer.TYPE);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Double> _second = DirectMetaProperty.ofImmutable(
        this, "second", IntDoublePair.class, Double.TYPE);
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
    public IntDoublePair.Builder builder() {
      return new IntDoublePair.Builder();
    }

    @Override
    public Class<? extends IntDoublePair> beanType() {
      return IntDoublePair.class;
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
    public MetaProperty<Integer> first() {
      return _first;
    }

    /**
     * The meta-property for the {@code second} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> second() {
      return _second;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName) {
        case "first":
          return ((IntDoublePair) bean).getFirst();
        case "second":
          return ((IntDoublePair) bean).getSecond();
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
   * The bean-builder for {@code IntDoublePair}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<IntDoublePair> {

    /** The first element. */
    private int _first;
    /** The second element. */
    private double _second;

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
          _first = (Integer) newValue;
          break;
        case "second":
          _second = (Double) newValue;
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
    public IntDoublePair build() {
      return IntDoublePair.of(_first, _second);
    }

  }

}
