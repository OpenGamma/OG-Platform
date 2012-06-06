/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ListDoubleObjectArrayConverter implements ResultConverter<List<Double[]>> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, List<Double[]> value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int rowCount = value.size();
    int columnCount = value.get(0).length;
    Map<String, Object> summary = new HashMap<String, Object>();
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);
    double[][] array = new double[rowCount][columnCount];
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columnCount; j++) {
        array[i][j] = value.get(i)[j];
      }
    }
    if (mode == ConversionMode.FULL) {
      String[] xLabels = new String[columnCount];
      String[] yLabels = new String[rowCount];
      for (int i = 0; i < xLabels.length; i++) {
        xLabels[i] = "";
      }
      result.put("x", xLabels);
      for (int i = 0; i < yLabels.length; i++) {
        yLabels[i] = "".toString();
      }
      result.put("y", yLabels);
      result.put("matrix", array);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, List<Double[]> value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, List<Double[]> value) {
    return "Labelled Matrix 2D (" + value.size() + " x " + value.get(0).length + ")";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }
}
