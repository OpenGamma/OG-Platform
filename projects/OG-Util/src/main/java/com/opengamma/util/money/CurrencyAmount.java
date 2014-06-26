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

import org.apache.commons.lang.StringUtils;
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

import com.opengamma.util.ArgumentChecker;

/**
 * An amount of a currency.
 * <p>
 * This class represents a {@code double} amount associated with a currency.
 * It is specifically named "CurrencyAmount" and not "Money" to indicate that
 * it simply holds a currency and an amount. By contrast, naming it "Money"
 * would imply it was a suitable choice for accounting purposes, which it is not.
 * <p>
 * This design approach has been chosen primarily for performance reasons.
 * Using a {@code BigDecimal} is markedly slower.
 * <p>
 * A {@code double} is a 64 bit floating point value suitable for most calculations.
 * Floating point maths is
 * <a href="http://docs.oracle.com/cd/E19957-01/806-3568/ncg_goldberg.html">inexact</a>
 * due to the conflict between binary and decimal arithmetic.
 * As such, there is the potential for data loss at the margins.
 * For example, adding the {@code double} values {@code 0.1d} and {@code 0.2d}
 * results in {@code 0.30000000000000004} rather than {@code 0.3}.
 * As can be seen, the level of error is small, hence providing this class is
 * used appropriately, the use of {@code double} is acceptable.
 * For example, using this class to provide a meaningful result type after
 * calculations have completed would be an appropriate use.
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class CurrencyAmount implements ImmutableBean, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The currency.
   * For example, in the value 'GBP 12.34' the currency is 'GBP'.
   */
  @PropertyDefinition(get = "manual", validate = "notNull")
  private final Currency _currency;
  /**
   * The amount of the currency.
   * For example, in the value 'GBP 12.34' the amount is 12.34.
   */
  @PropertyDefinition(get = "manual")
  private final double _amount;

  /**
   * Obtains an instance of {@code CurrencyAmount} for the specified currency and amount.
   *
   * @param currency  the currency the amount is in, not null
   * @param amount  the amount of the currency to represent
   * @return the currency amount, not null
   */
  public static CurrencyAmount of(final Currency currency, final double amount) {
    return new CurrencyAmount(currency, amount);
  }

  /**
   * Obtains an instance of {@code CurrencyAmount} for the specified ISO-4217
   * three letter currency code and amount.
   * <p>
   * A currency is uniquely identified by ISO-4217 three letter code.
   * This method creates the currency if it is not known.
   *
   * @param currencyCode  the three letter currency code, ASCII and upper case, not null
   * @param amount  the amount of the currency to represent
   * @return the currency amount, not null
   * @throws IllegalArgumentException if the currency code is invalid
   */
  public static CurrencyAmount of(final String currencyCode, final double amount) {
    return of(Currency.of(currencyCode), amount);
  }

  /**
   * Parses the string to produce a {@code CurrencyAmount}.
   * <p>
   * This parses the {@code toString} format of '${currency} ${amount}'.
   * 
   * @param amountStr  the amount string, not null
   * @return the currency amount
   * @throws IllegalArgumentException if the amount cannot be parsed
   */
  @FromString
  public static CurrencyAmount parse(final String amountStr) {
    ArgumentChecker.notNull(amountStr, "amountStr");
    String[] parts = StringUtils.split(amountStr, ' ');
    if (parts.length != 2) {
      throw new IllegalArgumentException("Unable to parse amount, invalid format: " + amountStr);
    }
    try {
      Currency cur = Currency.parse(parts[0]);
      double amount = Double.parseDouble(parts[1]);
      return new CurrencyAmount(cur, amount);
    } catch (RuntimeException ex) {
      throw new IllegalArgumentException("Unable to parse amount: " + amountStr, ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an instance.
   * 
   * @param currency  the currency, not null
   * @param amount  the amount
   */
  @ImmutableConstructor
  private CurrencyAmount(final Currency currency, final double amount) {
    ArgumentChecker.notNull(currency, "currency");
    _currency = currency;
    _amount = amount;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the currency.
   * For example, in the value 'GBP 12.34' the currency is 'GBP'.
   * 
   * @return the currency, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the amount of the currency.
   * For example, in the value 'GBP 12.34' the amount is 12.34.
   * 
   * @return the amount
   */
  public double getAmount() {
    return _amount;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code CurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param amountToAdd  the amount to add, in the same currency, not null
   * @return an amount based on this with the specified amount added, not null
   * @throws IllegalArgumentException if the currencies are not equal
   */
  public CurrencyAmount plus(final CurrencyAmount amountToAdd) {
    ArgumentChecker.notNull(amountToAdd, "amountToAdd");
    ArgumentChecker.isTrue(amountToAdd.getCurrency().equals(_currency), "Unable to add amounts in different currencies");
    return plus(amountToAdd.getAmount());
  }

  /**
   * Returns a copy of this {@code CurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param amountToAdd  the amount to add, in the same currency
   * @return an amount based on this with the specified amount added, not null
   */
  public CurrencyAmount plus(final double amountToAdd) {
    return new CurrencyAmount(_currency, _amount + amountToAdd);
  }

  /**
   * Returns a copy of this {@code CurrencyAmount} with the amount multiplied.
   * <p>
   * This takes this amount and multiplies it by the specified value.
   * The multiplication simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param valueToMultiplyBy  the scalar amount to multiply by
   * @return an amount based on this with the amount multiplied, not null
   */
  public CurrencyAmount multipliedBy(final double valueToMultiplyBy) {
    return new CurrencyAmount(_currency, _amount * valueToMultiplyBy);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount as a string.
   * <p>
   * The format is the currency code, followed by a space, followed by the
   * amount: '${currency} ${amount}'.
   * 
   * @return the currency amount, not null
   */
  @Override
  @ToString
  public String toString() {
    return _currency + " " + _amount;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurrencyAmount}.
   * @return the meta-bean, not null
   */
  public static CurrencyAmount.Meta meta() {
    return CurrencyAmount.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurrencyAmount.Meta.INSTANCE);
  }

  @Override
  public CurrencyAmount.Meta metaBean() {
    return CurrencyAmount.Meta.INSTANCE;
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
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CurrencyAmount other = (CurrencyAmount) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getAmount(), other.getAmount());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAmount());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurrencyAmount}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofImmutable(
        this, "currency", CurrencyAmount.class, Currency.class);
    /**
     * The meta-property for the {@code amount} property.
     */
    private final MetaProperty<Double> _amount = DirectMetaProperty.ofImmutable(
        this, "amount", CurrencyAmount.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "currency",
        "amount");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return _currency;
        case -1413853096:  // amount
          return _amount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CurrencyAmount.Builder builder() {
      return new CurrencyAmount.Builder();
    }

    @Override
    public Class<? extends CurrencyAmount> beanType() {
      return CurrencyAmount.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code amount} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Double> amount() {
      return _amount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((CurrencyAmount) bean).getCurrency();
        case -1413853096:  // amount
          return ((CurrencyAmount) bean).getAmount();
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
   * The bean-builder for {@code CurrencyAmount}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<CurrencyAmount> {

    private Currency _currency;
    private double _amount;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return _currency;
        case -1413853096:  // amount
          return _amount;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          this._currency = (Currency) newValue;
          break;
        case -1413853096:  // amount
          this._amount = (Double) newValue;
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
    public CurrencyAmount build() {
      return new CurrencyAmount(
          _currency,
          _amount);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("CurrencyAmount.Builder{");
      buf.append("currency").append('=').append(JodaBeanUtils.toString(_currency)).append(',').append(' ');
      buf.append("amount").append('=').append(JodaBeanUtils.toString(_amount));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
