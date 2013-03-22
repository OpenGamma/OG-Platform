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
    if (value instanceof LabelledMatrix1D) {
      return ((LabelledMatrix1D) value).getValues().length;
    } else {
      return null;
    }
  }

  public static boolean isDisplayableInline(Class<?> type, ColumnSpecification spec) {
    return type != null && LabelledMatrix1D.class.isAssignableFrom(type) && INLINE_VALUE_NAMES.contains(spec.getValueName());
  }

  public static List<String> columnHeaders(Object value) {
    // do something a bit more generic. label generators etc registered by class
    if (!(value instanceof LabelledMatrix1D)) {
      return Collections.emptyList();
    }
    Object[] labelsObjects = ((LabelledMatrix1D) value).getLabels();
    List<String> labels = Lists.newArrayListWithCapacity(labelsObjects.length);
    for (Object labelObject : labelsObjects) {
      labels.add(labelObject.toString());
    }
    return labels;
  }
}
