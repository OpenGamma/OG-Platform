/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *
 */
public class FixedPaymentMatrix {
  private final NavigableMap<LocalDate, MultipleCurrencyAmount> _values;
  private final int _maxCurrencyAmounts;

  public FixedPaymentMatrix() {
    _values = new TreeMap<LocalDate, MultipleCurrencyAmount>();
    _maxCurrencyAmounts = 0;
  }

  public FixedPaymentMatrix(final Map<LocalDate, MultipleCurrencyAmount> values) {
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

  public FixedPaymentMatrix(final Map<LocalDate, MultipleCurrencyAmount> values, final int maxCurrencyAmounts) {
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.notNegative(maxCurrencyAmounts, "max currency amounts");
    _values = new TreeMap<LocalDate, MultipleCurrencyAmount>(values);
    _maxCurrencyAmounts = maxCurrencyAmounts;
  }

  public FixedPaymentMatrix add(final Map<LocalDate, MultipleCurrencyAmount> payments) {
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
    return new FixedPaymentMatrix(values, count);
  }

  public FixedPaymentMatrix add(final FixedPaymentMatrix matrix) {
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
    return _values.keySet().toArray(new LocalDate[_values.size()]);
  }

  public String[][] getCurrencyAmountsAsStringArray() {
    final String[][] matrix = new String[_values.size()][getMaxCurrencyAmounts()];
    int i = 0;
    for (final MultipleCurrencyAmount mca : _values.values()) {
      final CurrencyAmount[] ca = mca.getCurrencyAmounts();
      for (int j = 0; j < getMaxCurrencyAmounts(); j++) {
        if (j < mca.size()) {
          matrix[i][j] = ca.toString();
        } else {
          matrix[i][j] = StringUtils.EMPTY;
        }
      }
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
    final FixedPaymentMatrix other = (FixedPaymentMatrix) obj;
    return ObjectUtils.equals(_values, other._values);
  }

}
