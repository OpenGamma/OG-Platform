/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.currency;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.convert.FromString;
import org.joda.convert.ToString;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.PrimitiveComputationTargetType;
import com.opengamma.engine.target.resolver.AbstractPrimitiveResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An ordered pair of currencies for quoting rates in FX deals.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class CurrencyPair implements ImmutableBean, UniqueIdentifiable {

  /**
   * The scheme to use.
   */
  private static final String OBJECT_SCHEME = CurrencyPair.class.getSimpleName();
  /**
   * An OG-Engine type so an instance can be used as a target in a dependency graph.
   */
  public static final PrimitiveComputationTargetType<CurrencyPair> TYPE = PrimitiveComputationTargetType.of(ComputationTargetType.of(CurrencyPair.class), CurrencyPair.class,
      new AbstractPrimitiveResolver<CurrencyPair>(OBJECT_SCHEME) {
        @Override
        protected CurrencyPair resolveObject(final String identifier) {
          return parse(identifier);
        }
      }
  );

  /**
   * The base currency of the pair.
   */
  @PropertyDefinition
  private final Currency _base;
  /**
   * The counter currency of the pair.
   */
  @PropertyDefinition
  private final Currency _counter;

  //-------------------------------------------------------------------------
  /**
   * Obtains a currency pair from a string with format AAA/BBB.
   * 
   * @param base  the base currency, not null
   * @param counter  the counter currency, not null
   * @return the currency pair, not null
   */
  public static CurrencyPair of(Currency base, Currency counter) {
    return new CurrencyPair(base, counter);
  }

  /**
   * Parses a currency pair from a string with format AAA/BBB.
   * <p>
   * The parsed format is '${baseCurrency}/${counterCurrency}'.
   * 
   * @param pairStr  the currency pair as a string AAA/BBB, not null
   * @return the currency pair, not null
   */
  @FromString
  public static CurrencyPair parse(String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() != 7) {
      throw new IllegalArgumentException("Currency pair format must be AAA/BBB");
    }
    Currency base = Currency.of(pairStr.substring(0, 3));
    Currency counter = Currency.of(pairStr.substring(4));
    return new CurrencyPair(base, counter);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param base the base currency, not null
   * @param counter the counter currency, not null
   */
  @ImmutableConstructor
  private CurrencyPair(Currency base, Currency counter) {
    ArgumentChecker.notNull(base, "base");
    ArgumentChecker.notNull(counter, "counter");
    if (base.equals(counter)) {
      throw new IllegalArgumentException("A currency pair cannot have the same base and counter currency (" + base + ")");
    }
    _base = base;
    _counter = counter;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the pair, formed from the two currencies.
   * <p>
   * The format is '${baseCurrency}/${counterCurrency}'.
   * 
   * @return Base currency code / Counter currency code, not null
   */
  public String getName() {
    return _base.getCode() + "/" + _counter.getCode();
  }

  /**
   * Gets the inverse currency pair.
   * <p>
   * The inverse pair has the same currencies but in reverse order.
   * 
   * @return the inverse pair, not null
   */
  public CurrencyPair inverse() {
    return new CurrencyPair(_counter, _base);
  }

  /**
   * Indicates if the currency pair contains the supplied currency as either its base or counter.
   * 
   * @param currency  the currency to check against the pair, null returns false
   * @return true if the currency is either the base or counter currency in the pair
   */
  public boolean contains(Currency currency) {
    return _base.equals(currency) || _counter.equals(currency);
  }

  /**
   * Return the pair's complementing currency for the supplied currency.
   * <p>
   * If the supplied currency is the pair's base, then the counter currency is returned.
   * If the supplied currency is the pair's counter, then the base currency is returned.
   * Otherwise an exception is thrown.
   * 
   * @param currency  the currency to find the complement for
   * @return the complementing currency, not null
   * @throws IllegalArgumentException if the supplied currency is not a member of the pair
   */
  public Currency getComplement(Currency currency) {
    if (_base.equals(currency)) {
      return _counter;
    } else if (_counter.equals(currency)) {
      return _base;
    } else {
      throw new IllegalArgumentException("Currency [" + currency + "] is not a member of " + this.toString());
    }
  }

  /**
   * Gets a derived unique identifier.
   * <p>
   * This allows the pair to be used in certain contexts where a {@code UniqueId} is needed.
   * This uses the scheme 'CurrencyPair'.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, getName());
  }

  //-------------------------------------------------------------------------
  /**
   * Returns the formatted string version of the currency pair.
   * <p>
   * The format is '${baseCurrency}/${counterCurrency}'.
   * 
   * @return the formatted string, not null
   */
  @Override
  @ToString
  public String toString() {
    return getName();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurrencyPair}.
   * @return the meta-bean, not null
   */
  public static CurrencyPair.Meta meta() {
    return CurrencyPair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurrencyPair.Meta.INSTANCE);
  }

  @Override
  public CurrencyPair.Meta metaBean() {
    return CurrencyPair.Meta.INSTANCE;
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
   * Gets the base currency of the pair.
   * @return the value of the property
   */
  public Currency getBase() {
    return _base;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counter currency of the pair.
   * @return the value of the property
   */
  public Currency getCounter() {
    return _counter;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CurrencyPair other = (CurrencyPair) obj;
      return JodaBeanUtils.equal(getBase(), other.getBase()) &&
          JodaBeanUtils.equal(getCounter(), other.getCounter());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getBase());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCounter());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurrencyPair}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code base} property.
     */
    private final MetaProperty<Currency> _base = DirectMetaProperty.ofImmutable(
        this, "base", CurrencyPair.class, Currency.class);
    /**
     * The meta-property for the {@code counter} property.
     */
    private final MetaProperty<Currency> _counter = DirectMetaProperty.ofImmutable(
        this, "counter", CurrencyPair.class, Currency.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "base",
        "counter");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3016401:  // base
          return _base;
        case 957830652:  // counter
          return _counter;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CurrencyPair> builder() {
      return new CurrencyPair.Builder();
    }

    @Override
    public Class<? extends CurrencyPair> beanType() {
      return CurrencyPair.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code base} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> base() {
      return _base;
    }

    /**
     * The meta-property for the {@code counter} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> counter() {
      return _counter;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3016401:  // base
          return ((CurrencyPair) bean).getBase();
        case 957830652:  // counter
          return ((CurrencyPair) bean).getCounter();
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
   * The bean-builder for {@code CurrencyPair}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<CurrencyPair> {

    private Currency _base;
    private Currency _counter;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3016401:  // base
          return _base;
        case 957830652:  // counter
          return _counter;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3016401:  // base
          this._base = (Currency) newValue;
          break;
        case 957830652:  // counter
          this._counter = (Currency) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public CurrencyPair build() {
      return new CurrencyPair(
          _base,
          _counter);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("CurrencyPair.Builder{");
      buf.append("base").append('=').append(JodaBeanUtils.toString(_base)).append(',').append(' ');
      buf.append("counter").append('=').append(JodaBeanUtils.toString(_counter));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
