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
@SuppressWarnings("rawtypes")
public class ListDoubleArrayConverter implements ResultConverter<List> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, List value, ConversionMode mode) {
    if (value.get(0).getClass().equals(double[].class)) {
      Map<String, Object> result = new HashMap<String, Object>();
      int rowCount = value.size();
      int columnCount = ((double[]) value.get(0)).length;
      Map<String, Object> summary = new HashMap<String, Object>();
      summary.put("rowCount", rowCount);
      summary.put("colCount", columnCount);
      result.put("summary", summary);
      double[][] array = new double[columnCount][rowCount];
      for (int i = 0; i < rowCount; i++) {
        for (int j = 0; j < columnCount; j++) {
          array[j][i] = ((double[]) value.get(i))[j];
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
    } else if (value.get(0).getClass().equals(Double[].class)) {
      Map<String, Object> result = new HashMap<String, Object>();
      int rowCount = value.size();
      int columnCount = ((Double[]) value.get(0)).length;
      Map<String, Object> summary = new HashMap<String, Object>();
      summary.put("rowCount", rowCount);
      summary.put("colCount", columnCount);
      result.put("summary", summary);
      double[][] array = new double[columnCount][rowCount];
      for (int i = 0; i < rowCount; i++) {
        for (int j = 0; j < columnCount; j++) {
          array[j][i] = ((Double[]) value.get(i))[j];
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, List value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, List value) {
    return "List";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }
}
