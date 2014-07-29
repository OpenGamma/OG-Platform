/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.beans.Bean;
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

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Stores a pair of currencies without any implied ordering.
 * <p>
 * This acts like a two element {@code Set}, thus
 * {@code UnorderedCurrencyPair(USD, EUR) == UnorderedCurrencyPair(EUR, USD)}.
 */
@BeanDefinition(builderScope = "private")
public final class UnorderedCurrencyPair implements ImmutableBean,
    UniqueIdentifiable, ObjectIdentifiable, Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * The scheme to use in object identifiers.
   */
  public static final String OBJECT_SCHEME = "UnorderedCurrencyPair";

  /**
   * One of the two currencies.
   * The name 'first' has no meaning as this pair is unordered.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final Currency _firstCurrency;
  /**
   * One of the two currencies.
   * The name 'second' has no meaning as this pair is unordered.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final Currency _secondCurrency;
  /**
   * The cached value of the identifier.
   */
  private String _idValue;

  /**
   * Obtains an {@code UnorderedCurrencyPair} from two currencies.
   * 
   * @param ccy1  one of the currencies, not null
   * @param ccy2  one of the currencies, not null
   * @return the pair, not null
   */
  public static UnorderedCurrencyPair of(Currency ccy1, Currency ccy2) {
    return new UnorderedCurrencyPair(ccy1, ccy2);
  }

  /**
   * Extracts an {@code UnorderedCurrencyPair} from a unique identifier.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the pair, not null
   * @throws IllegalArgumentException if the input is invalid
   */
  public static UnorderedCurrencyPair of(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "unique id");
    if (uniqueId.getScheme().equals(OBJECT_SCHEME)) {
      Pattern validate = Pattern.compile("[A-Z]{6}");
      String value = uniqueId.getValue();
      if (validate.matcher(value).matches()) {
        Currency ccy1 = Currency.of(value.substring(0, 3));
        Currency ccy2 = Currency.of(value.substring(3));
        return new UnorderedCurrencyPair(ccy1, ccy2);
      }
    }
    throw new IllegalArgumentException("Cannot create an UnorderedCurrencyPair from this UniqueId; need an ObjectScheme of UnorderedCurrencyPair, have " + uniqueId.getScheme());
  }

  /**
   * Parses the string to produce a {@code UnorderedCurrencyPair}.
   * <p>
   * This parses the {@code toString} format of '${currency1}${currency2}'
   * where the currencies are in alphabetical order.
   * 
   * @param pairStr  the amount string, not null
   * @return the currency amount
   * @throws IllegalArgumentException if the amount cannot be parsed
   */
  @FromString
  public static UnorderedCurrencyPair parse(final String pairStr) {
    ArgumentChecker.notNull(pairStr, "pairStr");
    if (pairStr.length() != 6) {
      throw new IllegalArgumentException("Unable to parse amount, invalid format: " + pairStr);
    }
    try {
      Currency cur1 = Currency.parse(pairStr.substring(0, 3));
      Currency cur2 = Currency.parse(pairStr.substring(3));
      return new UnorderedCurrencyPair(cur1, cur2);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse pair: " + pairStr, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Constructs a new instance.
   * 
   * @param currency1  one of the currencies, not null
   * @param currency2  one of the currencies, not null
   */
  @ImmutableConstructor
  private UnorderedCurrencyPair(Currency currency1, Currency currency2) {
    ArgumentChecker.notNull(currency1, "currency1");
    ArgumentChecker.notNull(currency2, "currency2");
    if (currency1.getCode().compareTo(currency2.getCode()) <= 0) {
      _firstCurrency = currency1;
      _secondCurrency = currency2;
      _idValue = currency1.getCode() + currency2.getCode();
    } else {
      _firstCurrency = currency2;
      _secondCurrency = currency1;
      _idValue = currency2.getCode() + currency1.getCode();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Gets one of the two currencies.
   * The name 'first' has no meaning as this pair is unordered.
   * 
   * @return one of the two currencies, not null
   */
  public Currency getFirstCurrency() {
    return _firstCurrency;
  }

  /**
   * Gets one of the two currencies.
   * The name 'second' has no meaning as this pair is unordered.
   * 
   * @return one of the two currencies, not null
   */
  public Currency getSecondCurrency() {
    return _secondCurrency;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the object identifier for the pair.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME UnorderedCurrencyPair}.
   * 
   * @return the object identifier, not null
   */
  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(OBJECT_SCHEME, _idValue);
  }

  /**
   * Gets the unique identifier for the pair.
   * <p>
   * This uses the scheme {@link #OBJECT_SCHEME UnorderedCurrencyPair}.
   * 
   * @return the unique identifier, not null
   */
  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(OBJECT_SCHEME, _idValue);
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if this unordered pair equals another unordered pair.
   * <p>
   * The comparison checks both currencies.
   * 
   * @param obj  the other currency, null returns false
   * @return true if equal
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof UnorderedCurrencyPair) {
      return _idValue.equals(((UnorderedCurrencyPair) obj)._idValue);
    }
    return false;
  }

  /**
   * Returns a suitable hash code for the unordered pair.
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return _idValue.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unordered pair as a string.
   * <p>
   * This uses the format of '${currency1}${currency2}'
   * where the currencies are in alphabetical order.
   * 
   * @return the unordered pair, not null
   */
  @Override
  @ToString
  public String toString() {
    return _idValue;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code UnorderedCurrencyPair}.
   * @return the meta-bean, not null
   */
  public static UnorderedCurrencyPair.Meta meta() {
    return UnorderedCurrencyPair.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(UnorderedCurrencyPair.Meta.INSTANCE);
  }

  @Override
  public UnorderedCurrencyPair.Meta metaBean() {
    return UnorderedCurrencyPair.Meta.INSTANCE;
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
   * The meta-bean for {@code UnorderedCurrencyPair}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code firstCurrency} property.
     */
    private final MetaProperty<Currency> _firstCurrency = DirectMetaProperty.ofImmutable(
        this, "firstCurrency", UnorderedCurrencyPair.class, Currency.class);
    /**
     * The meta-property for the {@code secondCurrency} property.
     */
    private final MetaProperty<Currency> _secondCurrency = DirectMetaProperty.ofImmutable(
        this, "secondCurrency", UnorderedCurrencyPair.class, Currency.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "firstCurrency",
        "secondCurrency");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return _firstCurrency;
        case 564126885:  // secondCurrency
          return _secondCurrency;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public UnorderedCurrencyPair.Builder builder() {
      return new UnorderedCurrencyPair.Builder();
    }

    @Override
    public Class<? extends UnorderedCurrencyPair> beanType() {
      return UnorderedCurrencyPair.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code firstCurrency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> firstCurrency() {
      return _firstCurrency;
    }

    /**
     * The meta-property for the {@code secondCurrency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> secondCurrency() {
      return _secondCurrency;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return ((UnorderedCurrencyPair) bean).getFirstCurrency();
        case 564126885:  // secondCurrency
          return ((UnorderedCurrencyPair) bean).getSecondCurrency();
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
   * The bean-builder for {@code UnorderedCurrencyPair}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<UnorderedCurrencyPair> {

    private Currency _firstCurrency;
    private Currency _secondCurrency;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          return _firstCurrency;
        case 564126885:  // secondCurrency
          return _secondCurrency;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1878034719:  // firstCurrency
          this._firstCurrency = (Currency) newValue;
          break;
        case 564126885:  // secondCurrency
          this._secondCurrency = (Currency) newValue;
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
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public UnorderedCurrencyPair build() {
      return new UnorderedCurrencyPair(
          _firstCurrency,
          _secondCurrency);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("UnorderedCurrencyPair.Builder{");
      buf.append("firstCurrency").append('=').append(JodaBeanUtils.toString(_firstCurrency)).append(',').append(' ');
      buf.append("secondCurrency").append('=').append(JodaBeanUtils.toString(_secondCurrency));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
