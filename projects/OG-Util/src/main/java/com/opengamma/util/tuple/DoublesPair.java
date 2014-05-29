/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;

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
 * An immutable pair consisting of two {@code double} elements.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class DoublesPair
    extends Pair<Double, Double>
    implements Double2DoubleMap.Entry {
  // this ImmutableBean is not auto-generated

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /** The first element. */
  public final double first; // CSIGNORE
  /** The second element. */
  public final double second; // CSIGNORE

  //-------------------------------------------------------------------------
  /**
   * Obtains a {@code DoublesPair} from a {@code DoublesPair} checking for null.
   * <p>
   * This method exists to catch instances of {@code DoublesPair} being passed to
   * {@link #of(Pair)} in an optimal way.
   * 
   * @param pair  the pair to convert, not null
   * @return the input pair, not null
   */
  public static DoublesPair of(final DoublesPair pair) {
    ArgumentChecker.notNull(pair, "pair");
    return pair;
  }

  /**
   * Obtains a {@code DoublesPair} from a {@code Pair}.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static DoublesPair of(final Pair<Double, Double> pair) {
    if (pair instanceof DoublesPair) {
      return (DoublesPair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new DoublesPair(pair.getFirst(), pair.getSecond());
  }

  /**
   * Obtains a {@code DoublesPair} from a {@code Pair} of {@code Number} values.
   * <p>
   * This uses {@link Number#doubleValue()}.
   * 
   * @param pair  the pair to convert, not null
   * @return a pair formed by extracting values from the pair, not null
   */
  public static DoublesPair ofNumbers(final Pair<? extends Number, ? extends Number> pair) {
    if (pair instanceof DoublesPair) {
      return (DoublesPair) pair;
    }
    ArgumentChecker.notNull(pair, "pair");
    ArgumentChecker.notNull(pair.getFirst(), "pair.first");
    ArgumentChecker.notNull(pair.getSecond(), "pair.second");
    return new DoublesPair(pair.getFirst().doubleValue(), pair.getSecond().doubleValue());
  }

  /**
   * Obtains a {@code DoublesPair} from two {@code double} values.
   * 
   * @param first  the first element
   * @param second  the second element
   * @return a pair formed from the two parameters, not null
   */
  public static DoublesPair of(final double first, final double second) {
    return new DoublesPair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Parses a {@code DoublesPair} from the standard string format.
   * <p>
   * The standard format is '[$first, $second]'. Spaces around the values are trimmed.
   * 
   * @param pairStr  the text to parse, not null
   * @return the parsed pair, not null
   */
  @FromString
  public static DoublesPair parse(final String pairStr) {
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
    double first = Double.parseDouble(split[0].trim());
    double second = Double.parseDouble(split[1].trim());
    return new DoublesPair(first, second);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a pair.
   * 
   * @param first  the first element
   * @param second  the second element
   * @deprecated Use public factory of(double,double)
   */
  @Deprecated
  public DoublesPair(final double first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Double getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code double}.
   * 
   * @return the primitive
   */
  public double getFirstDouble() {
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
  public double getDoubleKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(double newValue) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  /**
   * The meta-bean for {@code DoublesPair}.
   * @return the meta-bean, not null
   */
  public static DoublesPair.Meta meta() {
    return DoublesPair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DoublesPair.Meta.INSTANCE);
  }

  @Override
  public DoublesPair.Meta metaBean() {
    return DoublesPair.Meta.INSTANCE;
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
  public DoublesPair clone() {
    return this;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof DoublesPair) {
      final DoublesPair other = (DoublesPair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long f = Double.doubleToLongBits(first);
    final long s = Double.doubleToLongBits(second);
    return ((int) (f ^ (f >>> 32))) ^ ((int) (s ^ (s >>> 32)));
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
   * The meta-bean for {@code DoublesPair}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code first} property.
     */
    private final MetaProperty<Double> _first = DirectMetaProperty.ofImmutable(
        this, "first", DoublesPair.class, Double.TYPE);
    /**
     * The meta-property for the {@code second} property.
     */
    private final MetaProperty<Double> _second = DirectMetaProperty.ofImmutable(
        this, "second", DoublesPair.class, Double.TYPE);
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
    public DoublesPair.Builder builder() {
      return new DoublesPair.Builder();
    }

    @Override
    public Class<? extends DoublesPair> beanType() {
      return DoublesPair.class;
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
    public MetaProperty<Double> first() {
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
          return ((DoublesPair) bean).getFirst();
        case "second":
          return ((DoublesPair) bean).getSecond();
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
   * The bean-builder for {@code DoublesPair}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<DoublesPair> {

    /** The first element. */
    private double _first;
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
          _first = (Double) newValue;
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
    public DoublesPair build() {
      return DoublesPair.of(_first, _second);
    }

  }

}
