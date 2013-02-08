/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cashflow;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class FloatingPaymentMatrix {
  private final NavigableMap<LocalDate, List<Pair<CurrencyAmount, String>>> _values;
  private final int _maxEntries;

  public FloatingPaymentMatrix() {
    _values = new TreeMap<LocalDate, List<Pair<CurrencyAmount, String>>>();
    _maxEntries = 0;
  }

  public FloatingPaymentMatrix(final Map<LocalDate, List<Pair<CurrencyAmount, String>>> values) {
    ArgumentChecker.notNull(values, "values");
    _values = new TreeMap<LocalDate, List<Pair<CurrencyAmount, String>>>(values);
    int count = 0;
    for (final List<Pair<CurrencyAmount, String>> pairs : values.values()) {
      if (pairs.size() > count) {
        count = pairs.size();
      }
    }
    _maxEntries = count;
  }

  public FloatingPaymentMatrix(final Map<LocalDate, List<Pair<CurrencyAmount, String>>> values, final int maxEntries) {
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.notNegative(maxEntries, "max entries");
    _values = new TreeMap<LocalDate, List<Pair<CurrencyAmount, String>>>(values);
    _maxEntries = maxEntries;
  }

  public FloatingPaymentMatrix add(final Map<LocalDate, List<Pair<CurrencyAmount, String>>> resets) {
    ArgumentChecker.notNull(resets, "resets");
    final Map<LocalDate, List<Pair<CurrencyAmount, String>>> values = new TreeMap<LocalDate, List<Pair<CurrencyAmount, String>>>(_values);
    for (final Map.Entry<LocalDate, List<Pair<CurrencyAmount, String>>> entry : resets.entrySet()) {
      final LocalDate date = entry.getKey();
      final List<Pair<CurrencyAmount, String>> newList = entry.getValue();
      if (values.containsKey(date)) {
        final List<Pair<CurrencyAmount, String>> list = values.get(date);
        list.addAll(newList);
        values.put(date, list);
      } else {
        values.put(date, newList);
      }
    }
    return new FloatingPaymentMatrix(values);
  }

  public FloatingPaymentMatrix add(final FloatingPaymentMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    return add(matrix.getValues());
  }

  public Map<LocalDate, List<Pair<CurrencyAmount, String>>> getValues() {
    return _values;
  }

  public int getMaxEntries() {
    return _maxEntries;
  }

  public LocalDate[] getDatesAsArray() {
    return _values.keySet().toArray(new LocalDate[_values.size()]);
  }

  public String[][] getCurrencyAmountsAsStringArray() {
    final String[][] matrix = new String[_values.size()][_maxEntries];
    int i = 0;
    for (final List<Pair<CurrencyAmount, String>> list : _values.values()) {
      final int size = list.size();
      for (int j = 0; j < getMaxEntries(); j++) {
        if (j < size) {
          final StringBuffer sb = new StringBuffer(list.get(j).getFirst().toString());
          sb.append(", ");
          sb.append(list.get(j).getSecond());
          matrix[i][j] = sb.toString();
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
    final FloatingPaymentMatrix other = (FloatingPaymentMatrix) obj;
    return ObjectUtils.equals(_values, other._values);
  }

}
