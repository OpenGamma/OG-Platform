/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;
import com.opengamma.financial.analytics.TenorLabelledLocalDateDoubleTimeSeriesMatrix1D;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * TODO make this non-static
 */
/* package */ class Inliner {

  // TODO this is crude and temporary
  
  private static final Set<String> LOCAL_DATE_LABELLED_MATRIX_1D_VALUE_NAMES = ImmutableSet.of(
      ValueRequirementNames.BUCKETED_CS01,
      ValueRequirementNames.BUCKETED_GAMMA_CS01,
      ValueRequirementNames.BUCKETED_IR01);
  
  private static final Set<String> TENOR_LABELLED_TIME_SERIES_MATRIX_1D_VALUE_NAMES = ImmutableSet.of(
      ValueRequirementNames.YIELD_CURVE_PNL_SERIES,
      ValueRequirementNames.CURVE_PNL_SERIES,
      ValueRequirementNames.YIELD_CURVE_RETURN_SERIES);

  public static boolean isDisplayableInline(Class<?> type, ColumnSpecification spec) {
    return type != null &&
        ((LocalDateLabelledMatrix1D.class.isAssignableFrom(type) && 
            LOCAL_DATE_LABELLED_MATRIX_1D_VALUE_NAMES.contains(spec.getValueName())) ||
        (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D.class.isAssignableFrom(type) &&
            TENOR_LABELLED_TIME_SERIES_MATRIX_1D_VALUE_NAMES.contains(spec.getValueName())));
  }

  public static List<ColumnMeta> columnMeta(Object value) {
    if (value instanceof LocalDateLabelledMatrix1D) {
      LocalDateLabelledMatrix1D matrix = (LocalDateLabelledMatrix1D) value;
      return getLocalDateDoubleLabelledMatrix1DColumnMeta(matrix);
    }
    if (value instanceof TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) {
      TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix = (TenorLabelledLocalDateDoubleTimeSeriesMatrix1D) value;
      return getTenorLabelledLocalDateDoubleTimeSeriesMatrix1DColumnMeta(matrix);
    }
    return Collections.emptyList();
  }

  private static List<ColumnMeta> getLocalDateDoubleLabelledMatrix1DColumnMeta(LocalDateLabelledMatrix1D matrix) {
    List<ColumnMeta> meta = Lists.newArrayListWithCapacity(matrix.size());
    meta.add(new ColumnMeta(matrix.getKeys()[0], matrix.getKeys()[0].toString(), Double.class, LocalDateLabelledMatrix1D.class));
    for (int i = 1; i < matrix.size(); i++) {
      meta.add(new ColumnMeta(matrix.getKeys()[i], matrix.getKeys()[i].toString(), Double.class, null));
    }
    return meta;
  }
  
  private static List<ColumnMeta> getTenorLabelledLocalDateDoubleTimeSeriesMatrix1DColumnMeta(TenorLabelledLocalDateDoubleTimeSeriesMatrix1D matrix) {
    List<ColumnMeta> meta = Lists.newArrayListWithCapacity(matrix.size());
    meta.add(new ColumnMeta(matrix.getKeys()[0], matrix.getKeys()[0].toString(), LocalDateDoubleTimeSeries.class, TenorLabelledLocalDateDoubleTimeSeriesMatrix1D.class));
    for (int i = 1; i < matrix.size(); i++) {
      meta.add(new ColumnMeta(matrix.getKeys()[i], matrix.getKeys()[i].toString(), LocalDateDoubleTimeSeries.class, null));
    }
    return meta;
  }
  
}

//-------------------------------------------------------------------------
/* package */ class ColumnMeta implements Comparable<ColumnMeta> {

  private final Comparable<Object> _key;
  private final String _header;

  private final Class<?> _type;
  private final Class<?> _underlyingType;

  @SuppressWarnings("unchecked")
  /* package */ <T> ColumnMeta(Comparable<? super T> key, String header, Class<?> type, Class<?> underlyingType) {
    _key = (Comparable<Object>) key;
    _header = header;
    _type = type;
    _underlyingType = underlyingType;
  }

  //-------------------------------------------------------------------------
  /* package */ Comparable<Object> getKey() {
    return _key;
  }

  /* package */ String getHeader() {
    return _header;
  }

  /* package */ Class<?> getType() {
    return _type;
  }

  /* package */ Class<?> getUnderlyingType() {
    return _underlyingType;
  }

  //-------------------------------------------------------------------------
  @Override
  public int compareTo(ColumnMeta meta) {
    if (!meta._key.getClass().equals(_key.getClass())) {
      return 0;
    } else {
      return _key.compareTo(meta._key);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ColumnMeta that = (ColumnMeta) o;

    if (!_key.equals(that._key)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return _key.hashCode();
  }

}
