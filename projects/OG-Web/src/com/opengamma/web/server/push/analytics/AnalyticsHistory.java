/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 *
 */
/* package */ class AnalyticsHistory {

  private static final Set<Class<?>> s_historyTypes = ImmutableSet.<Class<?>>of(Double.class);

  private final Map<ValueSpecification, List<Object>> _history = Maps.newHashMap();

  /* package */ void addResults(ViewComputationResultModel fullResult) {
    // TODO implement AnalyticsHistory.addResults()
  }

  /* package */ List<Object> getHistory(ValueSpecification valueSpec, Object currentValue) {
    List<Object> valueHistory = _history.get(valueSpec);
    if (valueHistory != null) {
      return Collections.unmodifiableList(valueHistory);
    }
    if (s_historyTypes.contains(currentValue.getClass())) {
      return Collections.emptyList();
    }
    return null;
  }
}
