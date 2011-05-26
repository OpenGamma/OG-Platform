/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * A currency amount in multiple currencies.
 * <p>
 * This is a container holding multiple {@link CurrencyAmount} instances.
 * The amounts do not necessarily the same worth or value in each currency.
 * <p>
 * This class behaves as a set - if an amount is added with the same currency as one of the
 * elements, the amounts are added. For example, adding EUR 100 to the container
 * (EUR 200, CAD 100) would give (EUR 300, CAD 100). 
 */
public class MultipleCurrencyAmount implements Iterable<Map.Entry<Currency, Double>> {
  //TODO does this need a copy method?
  private static final double DEFAULT_RETURN_VALUE = Double.MAX_VALUE;

  public static MultipleCurrencyAmount of(final Currency currency, final double amount) {
    return new MultipleCurrencyAmount(currency, amount);
  }

  public static MultipleCurrencyAmount of(final Currency[] currencies, final double[] amounts) {
    return new MultipleCurrencyAmount(currencies, amounts);
  }

  public static MultipleCurrencyAmount of(final List<Currency> currencies, final List<Double> amounts) {
    return new MultipleCurrencyAmount(currencies, amounts);
  }

  public static MultipleCurrencyAmount of(final Map<Currency, Double> amounts) {
    return new MultipleCurrencyAmount(amounts);
  }

  public static MultipleCurrencyAmount of(final CurrencyAmount[] amounts) {
    return new MultipleCurrencyAmount(amounts);
  }

  public static MultipleCurrencyAmount of(final List<CurrencyAmount> amounts) {
    return new MultipleCurrencyAmount(amounts);
  }

  public static MultipleCurrencyAmount of(final Set<CurrencyAmount> amounts) {
    return new MultipleCurrencyAmount(amounts);
  }

  private final Object2DoubleOpenHashMap<Currency> _backingMap; //REVIEW emcleod 23-05-2011 May not be the best choice - it doubles its size as new entries are added.

  public MultipleCurrencyAmount(final Currency currency, final double amount) {
    Validate.notNull(currency, "currency");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    add(currency, amount);
  }

  public MultipleCurrencyAmount(final Currency[] currencies, final double[] amounts) {
    Validate.notNull(currencies, "currency array");
    Validate.notNull(amounts, "amount array");
    final int length = currencies.length;
    Validate.isTrue(length == amounts.length, "currency array and amount array must be the same length");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (int i = 0; i < length; i++) {
      Validate.notNull(currencies[i], "currency");
      add(currencies[i], amounts[i]);
    }
  }

  public MultipleCurrencyAmount(final List<Currency> currencies, final List<Double> amounts) {
    Validate.notNull(currencies, "currency list");
    Validate.notNull(amounts, "amount list");
    final int length = currencies.size();
    Validate.isTrue(length == amounts.size(), "currency list and amount list must be the same length");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (int i = 0; i < length; i++) {
      final Currency ccy = currencies.get(i);
      Validate.notNull(ccy, "currency");
      Validate.notNull(amounts.get(i), "amount");
      add(ccy, amounts.get(i));
    }
  }

  public MultipleCurrencyAmount(final Map<Currency, Double> amounts) {
    Validate.notNull(amounts, "amounts");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (final Map.Entry<Currency, Double> entry : amounts.entrySet()) {
      final Currency ccy = entry.getKey();
      final Double amount = entry.getValue();
      Validate.notNull(ccy, "currency");
      Validate.notNull(amount, "amount");
      add(ccy, amount);
    }
  }

  public MultipleCurrencyAmount(final CurrencyAmount[] amounts) {
    Validate.notNull(amounts, "amounts");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (final CurrencyAmount ca : amounts) {
      Validate.notNull(ca, "currency amount");
      add(ca);
    }
  }

  public MultipleCurrencyAmount(final List<CurrencyAmount> amounts) {
    Validate.notNull(amounts, "amounts");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (final CurrencyAmount ca : amounts) {
      Validate.notNull(ca, "currency amount");
      add(ca);
    }
  }

  public MultipleCurrencyAmount(final Set<CurrencyAmount> amounts) {
    Validate.notNull(amounts, "amounts");
    _backingMap = new Object2DoubleOpenHashMap<Currency>();
    _backingMap.defaultReturnValue(DEFAULT_RETURN_VALUE);
    for (final CurrencyAmount ca : amounts) {
      Validate.notNull(ca, "currency amount");
      add(ca);
    }
  }

  //TODO this iterator needs to be over CurrencyAmount
  @Override
  public Iterator<Map.Entry<Currency, Double>> iterator() {
    return _backingMap.entrySet().iterator();
  }

  public int size() {
    return _backingMap.size();
  }

  public CurrencyAmount[] getCurrencyAmounts() {
    final CurrencyAmount[] amounts = new CurrencyAmount[_backingMap.size()];
    int i = 0;
    for (final Map.Entry<Currency, Double> entry : _backingMap.entrySet()) {
      amounts[i++] = CurrencyAmount.of(entry.getKey(), entry.getValue());
    }
    return amounts;
  }

  public double getAmountFor(final Currency ccy) {
    Validate.notNull(ccy, "currency");
    final double amount = _backingMap.getDouble(ccy);
    if (amount == DEFAULT_RETURN_VALUE) {
      throw new IllegalArgumentException("Do not have an amount with currency " + ccy); //REVIEW emcleod 23-05-2011 Too strict?
    }
    return amount;
  }

  public CurrencyAmount getCurrencyAmountFor(final Currency ccy) {
    Validate.notNull(ccy, "currency");
    final double amount = _backingMap.getDouble(ccy);
    if (amount == DEFAULT_RETURN_VALUE) {
      throw new IllegalArgumentException("Do not have an amount with currency " + ccy);
    }
    return CurrencyAmount.of(ccy, amount);
  }

  /**
   * Add a given currency amount. If the currency is already present, the amount is added to the existing amount. If the currency is not yet present, the currency is added
   * to the list with the given amount.
   * @param amount The currency amount.
   */
  public void add(final CurrencyAmount amount) {
    Validate.notNull(amount, "currency amount");
    final Currency ccy = amount.getCurrency();
    final double a = amount.getAmount();
    if (_backingMap.containsKey(ccy)) {
      _backingMap.add(ccy, a);
    } else {
      _backingMap.put(ccy, a);
    }
  }

  /**
   * Add an amount in a given currency. If the currency is already present, the amount is added to the existing amount. If the currency is not yet present, the currency is added
   * to the list with the given amount.
   * @param currency The currency.
   * @param amount The amount.
   */
  public void add(final Currency currency, final double amount) {
    Validate.notNull(currency, "currency");
    if (_backingMap.containsKey(currency)) {
      _backingMap.add(currency, amount);
    } else {
      _backingMap.put(currency, amount);
    }
  }

  public void add(final MultipleCurrencyAmount amounts) {
    Validate.notNull(amounts, "multiple currency amount");
    for (final Entry<Currency, Double> a : amounts) {
      add(a.getKey(), a.getValue());
    }
  }

  public void remove(final Currency currency) {
    Validate.notNull(currency);
    if (_backingMap.containsKey(currency)) {
      _backingMap.remove(currency);
      return;
    }
    throw new IllegalArgumentException("Could not remove entry for " + currency + "; was not present"); //REVIEW emcleod 23-5-2011 too strict?
  }

  public void replace(final Currency currency, final double amount) {
    Validate.notNull(currency, "currency");
    if (_backingMap.containsKey(currency)) {
      _backingMap.put(currency, amount);
      return;
    }
    throw new IllegalArgumentException("Could not replace entry for " + currency + "; was not present");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _backingMap.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultipleCurrencyAmount other = (MultipleCurrencyAmount) obj;
    return ObjectUtils.equals(_backingMap, other._backingMap);
  }

}
