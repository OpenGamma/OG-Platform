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
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class SnapshotDataBundleFormatter extends AbstractFormatter<SnapshotDataBundle> {

  private final DoubleFormatter _doubleFormatter;

  public SnapshotDataBundleFormatter(DoubleFormatter doubleFormatter) {
    super(SnapshotDataBundle.class);
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<SnapshotDataBundle>(Format.EXPANDED) {
      @Override
      List<List<String>> format(SnapshotDataBundle value, ValueSpecification valueSpec) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(SnapshotDataBundle bundle, ValueSpecification valueSpec) {
    return "Data Bundle (" + bundle.getDataPoints().size() + " points)";
  }

  private List<List<String>> formatExpanded(SnapshotDataBundle bundle, ValueSpecification valueSpec) {
    Map<UniqueId, Double> dataPoints = bundle.getDataPoints();
    List<List<String>> results = Lists.newArrayListWithCapacity(dataPoints.size());
    for (Map.Entry<UniqueId, Double> entry : dataPoints.entrySet()) {
      String idStr = entry.getKey().toString();
      String formattedValue = _doubleFormatter.formatCell(entry.getValue(), valueSpec);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    return results;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
