/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;

/**
 * TODO make this non-static
 */
/* package */ class Inliner {

  // TODO this is crude and temporary
  private static final Set<String> INLINE_VALUE_NAMES = Sets.newHashSet(
      ValueRequirementNames.BUCKETED_CS01,
      ValueRequirementNames.BUCKETED_GAMMA_CS01,
      ValueRequirementNames.BUCKETED_IR01);

  public static boolean isDisplayableInline(Class<?> type, ColumnSpecification spec) {
    return type != null &&
        LocalDateLabelledMatrix1D.class.isAssignableFrom(type) &&
        INLINE_VALUE_NAMES.contains(spec.getValueName());
  }

  public static List<ColumnMeta> columnMeta(Object value) {
    if (!(value instanceof LocalDateLabelledMatrix1D)) {
      return Collections.emptyList();
    }
    LocalDateLabelledMatrix1D matrix = (LocalDateLabelledMatrix1D) value;
    List<ColumnMeta> meta = Lists.newArrayListWithCapacity(matrix.size());
    meta.add(new ColumnMeta(matrix.getKeys()[0], matrix.getKeys()[0].toString(), Double.class, LocalDateLabelledMatrix1D.class));
    for (int i = 1; i < matrix.size(); i++) {
      meta.add(new ColumnMeta(matrix.getKeys()[i], matrix.getKeys()[i].toString(), Double.class, null));
    }
    return meta;
  }
}

/* package */ class ColumnMeta implements Comparable<ColumnMeta> {

  private final Comparable _key;
  private final String _header;

  private final Class<?> _type;
  private final Class<?> _underlyingType;

  /* package */ ColumnMeta(Comparable key, String header, Class<?> type, Class<?> underlyingType) {
    _key = key;
    _header = header;
    _type = type;
    _underlyingType = underlyingType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int compareTo(ColumnMeta meta) {
    if (!meta._key.getClass().equals(_key.getClass())) {
      return 0;
    } else {
      return _key.compareTo(meta._key);
    }
  }

  /* package */ Comparable getKey() {
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
