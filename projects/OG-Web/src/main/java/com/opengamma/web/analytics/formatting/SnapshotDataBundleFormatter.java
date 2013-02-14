/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/* package */class SnapshotDataBundleFormatter extends AbstractFormatter<SnapshotDataBundle> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String ID = "ID";
  private static final String VALUE = "Value";

  private final DoubleFormatter _doubleFormatter;

  /* package */SnapshotDataBundleFormatter(final DoubleFormatter doubleFormatter) {
    super(SnapshotDataBundle.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<SnapshotDataBundle>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(final SnapshotDataBundle value, final ValueSpecification valueSpec) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final SnapshotDataBundle bundle, final ValueSpecification valueSpec) {
    return "Data Bundle (" + bundle.size() + " points)";
  }

  private Map<String, Object> formatExpanded(final SnapshotDataBundle bundle, final ValueSpecification valueSpec) {
    final List<List<String>> results = Lists.newArrayListWithCapacity(bundle.size());
    final Map<String, Object> resultsMap = Maps.newHashMap();
    for (final Map.Entry<ExternalIdBundle, Double> entry : bundle.getDataPointSet()) {
      final String idStr;
      // TODO: [PLAT-3044] This pattern might be quite common now we've used bundles more often; factor somewhere else
      if (entry.getKey().isEmpty()) {
        idStr = "";
      } else if (entry.getKey().size() == 1) {
        idStr = entry.getKey().iterator().next().toString();
      } else {
        // TODO: [PLAT-3044] Use an ExternalIdOrderConfig object to pick out an identifier from the bundle to display
        idStr = entry.getKey().toString();
      }
      final String formattedValue = _doubleFormatter.formatCell(entry.getValue(), valueSpec);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    resultsMap.put(DATA, results);
    resultsMap.put(LABELS, ImmutableList.of(ID, VALUE));
    return resultsMap;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
