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
public class SnapshotDataBundleFormatter extends NoHistoryFormatter<SnapshotDataBundle> {

  private final DoubleFormatter _doubleFormatter;

  public SnapshotDataBundleFormatter(DoubleFormatter doubleFormatter) {
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
  }

  @Override
  public String formatForDisplay(SnapshotDataBundle bundle, ValueSpecification valueSpec) {
    return "Data Bundle (" + bundle.getDataPoints().size() + " points)";
  }

  @Override
  public List<List<String>> formatForExpandedDisplay(SnapshotDataBundle bundle, ValueSpecification valueSpec) {
    Map<ExternalId, Double> dataPoints = bundle.getDataPoints();
    List<List<String>> results = Lists.newArrayListWithCapacity(dataPoints.size());
    for (Map.Entry<ExternalId, Double> entry : dataPoints.entrySet()) {
      String idStr = entry.getKey().toString();
      String formattedValue = _doubleFormatter.formatForDisplay(entry.getValue(), valueSpec);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    return results;
  }

  @Override
  public FormatType getFormatForType() {
    return FormatType.LABELLED_MATRIX_1D;
  }
}
