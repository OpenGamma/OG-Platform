/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class PaymentScheduleMatrix {
  private final NavigableMap<LocalDate, MultipleCurrencyAmount> _values;
  private final int _maxCurrencyAmounts;

  public PaymentScheduleMatrix() {
    _values = new TreeMap<LocalDate, MultipleCurrencyAmount>();
    _maxCurrencyAmounts = 0;
  }

  public PaymentScheduleMatrix(final Map<LocalDate, MultipleCurrencyAmount> values) {
    ArgumentChecker.notNull(values, "values");
    _values = new TreeMap<LocalDate, MultipleCurrencyAmount>(values);
    int count = 0;
    for (final MultipleCurrencyAmount mca : values.values()) {
      if (mca.size() > count) {
        count = mca.size();
      }
    }
    _maxCurrencyAmounts = count;
  }

  public PaymentScheduleMatrix(final Map<LocalDate, MultipleCurrencyAmount> values, final int maxCurrencyAmounts) {
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.notNegative(maxCurrencyAmounts, "max currency amounts");
    _values = new TreeMap<LocalDate, MultipleCurrencyAmount>(values);
    _maxCurrencyAmounts = maxCurrencyAmounts;
  }

  public PaymentScheduleMatrix add(final Map<LocalDate, MultipleCurrencyAmount> payments) {
    ArgumentChecker.notNull(payments, "payments");
    final Map<LocalDate, MultipleCurrencyAmount> values = new TreeMap<LocalDate, MultipleCurrencyAmount>(_values);
    int count = getMaxCurrencyAmounts();
    for (final Map.Entry<LocalDate, MultipleCurrencyAmount> entry : payments.entrySet()) {
      final LocalDate date = entry.getKey();
      final MultipleCurrencyAmount mca = entry.getValue();
      if (mca.size() > count) {
        count = mca.size();
      }
      if (values.containsKey(date)) {
        values.put(date, values.get(date).plus(mca));
      } else {
        values.put(date, mca);
      }
    }
    return new PaymentScheduleMatrix(values, count);
  }

  public PaymentScheduleMatrix add(final PaymentScheduleMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    return add(matrix.getValues());
  }

  public Map<LocalDate, MultipleCurrencyAmount> getValues() {
    return _values;
  }

  public int getMaxCurrencyAmounts() {
    return _maxCurrencyAmounts;
  }

  public LocalDate[] getDatesAsArray() {
    return _values.keySet().toArray(new LocalDate[0]);
  }

  public CurrencyAmount[][] getCurrencyAmountsAsArray() {
    final CurrencyAmount[][] matrix = new CurrencyAmount[_values.size()][getMaxCurrencyAmounts()];
    int i = 0;
    for (final MultipleCurrencyAmount mca : _values.values()) {
      final CurrencyAmount[] ca = new CurrencyAmount[getMaxCurrencyAmounts()];
      System.arraycopy(mca.getCurrencyAmounts(), 0, ca, 0, getMaxCurrencyAmounts());
      matrix[i] = ca;
      i++;
    }
    return matrix;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _values.hashCode();
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
    final PaymentScheduleMatrix other = (PaymentScheduleMatrix) obj;
    return ObjectUtils.equals(_values, other._values);
  }

}
