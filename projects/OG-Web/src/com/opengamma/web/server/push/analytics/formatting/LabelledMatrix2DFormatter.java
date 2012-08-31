/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import java.util.Map;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix2D;

/**
 *
 */
/* package */ class LabelledMatrix2DFormatter extends NoHistoryFormatter<LabelledMatrix2D> {

  @Override
  public String formatForDisplay(LabelledMatrix2D value, ValueSpecification valueSpec) {
    return "Matrix (" + value.getYKeys().length + " x " + value.getXKeys().length + ")";
  }

  @Override
  public Map<String, Object> formatForExpandedDisplay(LabelledMatrix2D value, ValueSpecification valueSpec) {
    Map<String, Object> results = Maps.newHashMap();
    int rowCount = value.getYKeys().length;
    int columnCount = value.getXKeys().length;
    String[] xLabels = new String[columnCount];
    String[] yLabels = new String[rowCount];
    for (int i = 0; i < xLabels.length; i++) {
      xLabels[i] = value.getXLabels()[i].toString();
    }
    results.put("xLabels", xLabels);
    for (int i = 0; i < yLabels.length; i++) {
      yLabels[i] = value.getYLabels()[i].toString();
    }
    results.put("yLabels", yLabels);
    results.put("matrix", value.getValues());
    return results;
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.LABELLED_MATRIX_2D;
  }
}
