/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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

import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.util.ArgumentChecker;

/**
 * A map of currency amounts keyed by currency.
 * <p>
 * This is a container holding multiple {@link CurrencyAmount} instances.
 * The amounts do not necessarily the same worth or value in each currency.
 * <p>
 * This class behaves as a set - if an amount is added with the same currency as one of the
 * elements, the amounts are added. For example, adding EUR 100 to the container
 * (EUR 200, CAD 100) would give (EUR 300, CAD 100).
 * <p>
 * This class is immutable and thread-safe.
 */
@BeanDefinition(builderScope = "private")
public final class MultipleCurrencyAmount implements ImmutableBean,
    Iterable<CurrencyAmount>, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The map of {@code CurrencyAmount} keyed by currency.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSortedMap<Currency, CurrencyAmount> _currencyAmountMap;

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a currency and amount.
   * 
   * @param currency  the currency, not null
   * @param amount  the amount
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Currency currency, final double amount) {
    return new MultipleCurrencyAmount(ImmutableSortedMap.of(currency, CurrencyAmount.of(currency, amount)));
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a paired array of currencies and amounts.
   * 
   * @param currencies  the currencies, not null
   * @param amounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Currency[] currencies, final double[] amounts) {
    ArgumentChecker.noNulls(currencies, "currencies");
    ArgumentChecker.notNull(amounts, "amounts");
    final int length = currencies.length;
    ArgumentChecker.isTrue(length == amounts.length, "Currency array and amount array must be the same length");
    List<CurrencyAmount> list = new ArrayList<CurrencyAmount>(length);
    for (int i = 0; i < length; i++) {
      list.add(CurrencyAmount.of(currencies[i], amounts[i]));
    }
    return of(list);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a paired list of currencies and amounts.
   * 
   * @param currencies  the currencies, not null
   * @param amounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final List<Currency> currencies, final List<Double> amounts) {
    ArgumentChecker.noNulls(currencies, "currencies");
    ArgumentChecker.noNulls(amounts, "amounts");
    final int length = currencies.size();
    ArgumentChecker.isTrue(length == amounts.size(), "Currency array and amount array must be the same length");
    List<CurrencyAmount> list = new ArrayList<CurrencyAmount>(length);
    for (int i = 0; i < length; i++) {
      list.add(CurrencyAmount.of(currencies.get(i), amounts.get(i)));
    }
    return of(list);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a map of currency to amount.
   * 
   * @param amountMap  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Map<Currency, Double> amountMap) {
    ArgumentChecker.notNull(amountMap, "amountMap");
    TreeMap<Currency, CurrencyAmount> map = new TreeMap<Currency, CurrencyAmount>();
    for (Entry<Currency, Double> entry : amountMap.entrySet()) {
      ArgumentChecker.notNull(entry.getValue(), "amount");
      map.put(entry.getKey(), CurrencyAmount.of(entry.getKey(), entry.getValue()));
    }
    return new MultipleCurrencyAmount(map);
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a list of {@code CurrencyAmount}.
   * 
   * @param currencyAmounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final CurrencyAmount... currencyAmounts) {
    ArgumentChecker.notNull(currencyAmounts, "currencyAmounts");
    return of(Arrays.asList(currencyAmounts));
  }

  /**
   * Obtains a {@code MultipleCurrencyAmount} from a list of {@code CurrencyAmount}.
   * 
   * @param currencyAmounts  the amounts, not null
   * @return the amount, not null
   */
  public static MultipleCurrencyAmount of(final Iterable<CurrencyAmount> currencyAmounts) {
    ArgumentChecker.notNull(currencyAmounts, "currencyAmounts");
    TreeMap<Currency, CurrencyAmount> map = new TreeMap<Currency, CurrencyAmount>();
    for (CurrencyAmount currencyAmount : currencyAmounts) {
      ArgumentChecker.notNull(currencyAmount, "currencyAmount");
      CurrencyAmount existing = map.get(currencyAmount.getCurrency());
      if (existing != null) {
        map.put(currencyAmount.getCurrency(), existing.plus(currencyAmount));
      } else {
        map.put(currencyAmount.getCurrency(), currencyAmount);
      }
    }
    return new MultipleCurrencyAmount(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of stored amounts.
   * 
   * @return the number of amounts
   */
  public int size() {
    return _currencyAmountMap.size();
  }

  /**
   * Iterates though the currency-amounts.
   * 
   * @return the iterator, not null
   */
  @Override
  public Iterator<CurrencyAmount> iterator() {
    return _currencyAmountMap.values().iterator();
  }

  /**
   * Gets the currency amounts as an array.
   * 
   * @return the independent, modifiable currency amount array, not null
   */
  public CurrencyAmount[] getCurrencyAmounts() {
    return _currencyAmountMap.values().toArray(new CurrencyAmount[_currencyAmountMap.size()]);
  }

  /**
   * Gets the amount for the specified currency.
   * 
   * @param currency  the currency to find an amount for, not null
   * @return the amount
   * @throws IllegalArgumentException if the currency is not present
   */
  public double getAmount(final Currency currency) {
    CurrencyAmount currencyAmount = getCurrencyAmount(currency);
    if (currencyAmount == null) {
      throw new IllegalArgumentException("Do not have an amount with currency " + currency);
    }
    return currencyAmount.getAmount();
  }

  /**
   * Gets the {@code CurrencyAmount} for the specified currency.
   * 
   * @param currency  the currency to find an amount for, not null
   * @return the amount, null if no amount for the currency
   */
  public CurrencyAmount getCurrencyAmount(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    return _currencyAmountMap.get(currency);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currencyAmountToAdd  the amount to add, in the same currency, not null
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final CurrencyAmount currencyAmountToAdd) {
    ArgumentChecker.notNull(currencyAmountToAdd, "currencyAmountToAdd");
    ImmutableSortedMap.Builder<Currency, CurrencyAmount> copy = ImmutableSortedMap.naturalOrder();
    CurrencyAmount previous = getCurrencyAmount(currencyAmountToAdd.getCurrency());
    for (CurrencyAmount amount : _currencyAmountMap.values()) {
      if (amount.getCurrency().equals(currencyAmountToAdd.getCurrency())) {
        copy.put(amount.getCurrency(), previous.plus(currencyAmountToAdd));
      } else {
        copy.put(amount.getCurrency(), amount);
      }
    }
    if (previous == null) {
      copy.put(currencyAmountToAdd.getCurrency(), currencyAmountToAdd);
    }
    return new MultipleCurrencyAmount(copy.build());
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to add to, not null
   * @param amountToAdd  the amount to add
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final Currency currency, final double amountToAdd) {
    ArgumentChecker.notNull(currency, "currency");
    ImmutableSortedMap.Builder<Currency, CurrencyAmount> copy = ImmutableSortedMap.naturalOrder();
    CurrencyAmount previous = getCurrencyAmount(currency);
    for (CurrencyAmount amount : _currencyAmountMap.values()) {
      if (amount.getCurrency().equals(currency)) {
        copy.put(amount.getCurrency(), previous.plus(amountToAdd));
      } else {
        copy.put(amount.getCurrency(), amount);
      }
    }
    if (previous == null) {
      copy.put(currency, CurrencyAmount.of(currency, amountToAdd));
    }
    return new MultipleCurrencyAmount(copy.build());
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified amount added.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * If the currency is already present, the amount is added to the existing amount.
   * If the currency is not yet present, the currency-amount is added to the map.
   * The addition simply uses standard {@code double} arithmetic.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param multipleCurrencyAmountToAdd  the currency to add to, not null
   * @return an amount based on this with the specified amount added, not null
   */
  public MultipleCurrencyAmount plus(final MultipleCurrencyAmount multipleCurrencyAmountToAdd) {
    ArgumentChecker.notNull(multipleCurrencyAmountToAdd, "multipleCurrencyAmountToAdd");
    MultipleCurrencyAmount result = this;
    for (CurrencyAmount currencyAmount : multipleCurrencyAmountToAdd) {
      result = result.plus(currencyAmount);
    }
    return result;
  }


  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with all the amounts multiplied by the factor.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param factor The multiplicative factor.
   * @return An amount based on this with all the amounts multiplied by the factor. Not null
   */
  public MultipleCurrencyAmount multipliedBy(final double factor) {
    TreeMap<Currency, Double> map = new TreeMap<Currency, Double>();
    for (CurrencyAmount currencyAmount : this) {
      map.put(currencyAmount.getCurrency(), currencyAmount.getAmount() * factor);
    }
    return MultipleCurrencyAmount.of(map);
  }

  //-------------------------------------------------------------------------
  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} with the specified currency.
   * <p>
   * This adds the specified amount to this monetary amount, returning a new object.
   * Any previous amount for the specified currency is replaced.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to replace, not null
   * @param amount  the new amount
   * @return an amount based on this with the specified currency replaced, not null
   */
  public MultipleCurrencyAmount with(final Currency currency, final double amount) {
    ArgumentChecker.notNull(currency, "currency");
    TreeMap<Currency, CurrencyAmount> copy = new TreeMap<Currency, CurrencyAmount>(_currencyAmountMap);
    copy.put(currency, CurrencyAmount.of(currency, amount));
    return new MultipleCurrencyAmount(copy);
  }

  /**
   * Returns a copy of this {@code MultipleCurrencyAmount} without the specified currency.
   * <p>
   * This removes the specified currency from this monetary amount, returning a new object.
   * <p>
   * This instance is immutable and unaffected by this method. 
   * 
   * @param currency  the currency to replace, not null
   * @return an amount based on this with the specified currency removed, not null
   */
  public MultipleCurrencyAmount without(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    TreeMap<Currency, CurrencyAmount> copy = new TreeMap<Currency, CurrencyAmount>(_currencyAmountMap);
    if (copy.remove(currency) == null) {
      return this;
    }
    return new MultipleCurrencyAmount(copy);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount as a string.
   * <p>
   * The format includes each currency-amount.
   * 
   * @return the currency amount, not null
   */
  @Override
  public String toString() {
    return _currencyAmountMap.values().toString();
  }

  @ImmutableConstructor
  private MultipleCurrencyAmount(SortedMap<Currency, CurrencyAmount> currencyAmountMap) {
    JodaBeanUtils.notNull(currencyAmountMap, "currencyAmountMap");
    this._currencyAmountMap = ImmutableSortedMap.copyOfSorted(currencyAmountMap);
  }

  private MultipleCurrencyAmount(ImmutableSortedMap<Currency, CurrencyAmount> currencyAmountMap) {
    JodaBeanUtils.notNull(currencyAmountMap, "currencyAmountMap");
    this._currencyAmountMap = currencyAmountMap;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MultipleCurrencyAmount}.
   * @return the meta-bean, not null
   */
  public static MultipleCurrencyAmount.Meta meta() {
    return MultipleCurrencyAmount.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MultipleCurrencyAmount.Meta.INSTANCE);
  }

  @Override
  public MultipleCurrencyAmount.Meta metaBean() {
    return MultipleCurrencyAmount.Meta.INSTANCE;
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
   * Gets the map of {@code CurrencyAmount} keyed by currency.
   * @return the value of the property, not null
   */
  public ImmutableSortedMap<Currency, CurrencyAmount> getCurrencyAmountMap() {
    return _currencyAmountMap;
  }

  //-----------------------------------------------------------------------
  @Override
  public MultipleCurrencyAmount clone() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MultipleCurrencyAmount other = (MultipleCurrencyAmount) obj;
      return JodaBeanUtils.equal(getCurrencyAmountMap(), other.getCurrencyAmountMap());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrencyAmountMap());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MultipleCurrencyAmount}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currencyAmountMap} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSortedMap<Currency, CurrencyAmount>> _currencyAmountMap = DirectMetaProperty.ofImmutable(
        this, "currencyAmountMap", MultipleCurrencyAmount.class, (Class) ImmutableSortedMap.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "currencyAmountMap");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -218001197:  // currencyAmountMap
          return _currencyAmountMap;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public MultipleCurrencyAmount.Builder builder() {
      return new MultipleCurrencyAmount.Builder();
    }

    @Override
    public Class<? extends MultipleCurrencyAmount> beanType() {
      return MultipleCurrencyAmount.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code currencyAmountMap} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSortedMap<Currency, CurrencyAmount>> currencyAmountMap() {
      return _currencyAmountMap;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -218001197:  // currencyAmountMap
          return ((MultipleCurrencyAmount) bean).getCurrencyAmountMap();
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
   * The bean-builder for {@code MultipleCurrencyAmount}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<MultipleCurrencyAmount> {

    private SortedMap<Currency, CurrencyAmount> _currencyAmountMap = new TreeMap<Currency, CurrencyAmount>();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -218001197:  // currencyAmountMap
          return _currencyAmountMap;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -218001197:  // currencyAmountMap
          this._currencyAmountMap = (SortedMap<Currency, CurrencyAmount>) newValue;
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
    public MultipleCurrencyAmount build() {
      return new MultipleCurrencyAmount(
          _currencyAmountMap);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("MultipleCurrencyAmount.Builder{");
      buf.append("currencyAmountMap").append('=').append(JodaBeanUtils.toString(_currencyAmountMap));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
