/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class SnapshotDataBundleFormatter extends AbstractFormatter<SnapshotDataBundle> {

  private final DoubleFormatter _doubleFormatter;

  public SnapshotDataBundleFormatter(final DoubleFormatter doubleFormatter) {
    super(SnapshotDataBundle.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<SnapshotDataBundle>(Format.EXPANDED) {
      @Override
      List<List<String>> format(final SnapshotDataBundle value, final ValueSpecification valueSpec) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(final SnapshotDataBundle bundle, final ValueSpecification valueSpec) {
    return "Data Bundle (" + bundle.getDataPoints().size() + " points)";
  }

  private List<List<String>> formatExpanded(final SnapshotDataBundle bundle, final ValueSpecification valueSpec) {
    final Map<ExternalId, Double> dataPoints = bundle.getDataPoints();
    final List<List<String>> results = Lists.newArrayListWithCapacity(dataPoints.size());
    for (final Map.Entry<ExternalId, Double> entry : dataPoints.entrySet()) {
      final String idStr = entry.getKey().toString();
      final String formattedValue = _doubleFormatter.formatCell(entry.getValue(), valueSpec);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
