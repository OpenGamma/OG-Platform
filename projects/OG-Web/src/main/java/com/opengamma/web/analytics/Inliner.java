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
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;

/**
 * TODO make this non-static
 */
/* package */ class Inliner {

  // TODO this is crude and temporary
  private static Set<String> INLINE_VALUE_NAMES = Sets.newHashSet(
      ValueRequirementNames.BUCKETED_CS01,
      ValueRequirementNames.BUCKETED_GAMMA_CS01,
      ValueRequirementNames.BUCKETED_IR01);

  /**
   * If the value can be inlined and displayed over multiple columns this method returns the number of columns.
   * If it can't be inlined then it returns null.
   * @param value A calculated value, possibly null
   * @return The number of columns required to display the inlined value or null if it is null or can't be inlined.
   */
  /* package */ static Integer columnCount(Object value) {
    // do something a bit more sophisticated if we need to support this for more types
    if (value instanceof LocalDateLabelledMatrix1D) {
      return ((LocalDateLabelledMatrix1D) value).getValues().length;
    } else {
      return null;
    }
  }

  public static boolean isDisplayableInline(Class<?> type, ColumnSpecification spec) {
    return type != null &&
        LocalDateLabelledMatrix1D.class.isAssignableFrom(type) &&
        INLINE_VALUE_NAMES.contains(spec.getValueName());
  }

  public static List<String> columnHeaders(Object value) {
    // do something a bit more generic. label generators etc registered by class
    if (!(value instanceof LocalDateLabelledMatrix1D)) {
      return Collections.emptyList();
    }
    Object[] labelsObjects = ((LabelledMatrix1D) value).getLabels();
    List<String> labels = Lists.newArrayListWithCapacity(labelsObjects.length);
    for (Object labelObject : labelsObjects) {
      labels.add(labelObject.toString());
    }
    return labels;
  }

  public static List<ColumnMeta> columnMeta(Object value) {
    if (!(value instanceof LocalDateLabelledMatrix1D)) {
      return Collections.emptyList();
    }
    LocalDateLabelledMatrix1D matrix = (LocalDateLabelledMatrix1D) value;
    List<ColumnMeta> meta = Lists.newArrayListWithCapacity(matrix.size());
    for (int i = 0; i < matrix.size(); i++) {
      meta.add(new ColumnMeta(matrix.getKeys()[i], matrix.getKeys()[i].toString()));
    }
    return meta;
  }
}

/* package */ class ColumnMeta implements Comparable<ColumnMeta> {

  private final Comparable _key;
  private final String _header;

  /* package */ ColumnMeta(Comparable key, String header) {
    _key = key;
    _header = header;
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
