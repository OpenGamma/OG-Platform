/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix3D;
import com.opengamma.util.time.Tenor;

/**
 * Converter for {@link LabelledMatrix3D} results.
 */
@SuppressWarnings("rawtypes")
public class LabelledMatrix3DConverter implements ResultConverter<LabelledMatrix3D> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix3D value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int tablesCount = value.getZKeys().length;
    int rowCount = value.getYKeys().length;
    int columnCount = value.getXKeys().length;
    final Map<String, Object> summary = new HashMap<String, Object>();
    summary.put("zCount", tablesCount);
    summary.put("rowCount", rowCount);
    summary.put("colCount", columnCount);
    result.put("summary", summary);

    if (mode == ConversionMode.FULL) {
      String[] xLabels = new String[columnCount];
      String[] yLabels = new String[rowCount * tablesCount];
      for (int i = 0; i < xLabels.length; i++) {
        xLabels[i] = value.getXLabels()[i].toString();
      }
      result.put("x", xLabels);
      double[][][] values = value.getValues();
      double[][] formattedValues = new double[yLabels.length][xLabels.length];
      int rowNumber = 0;
      for (int i = 0; i < tablesCount; i++) {
        for (int j = 0; j < rowCount; j++) {
          yLabels[rowNumber] = ((Tenor) value.getYLabels()[j]).getPeriod().toString() + " x " + ((Tenor) value.getZLabels()[i]).getPeriod().toString();
          formattedValues[rowNumber++] = values[i][j];
        }
      }
      result.put("y", yLabels);
      result.put("matrix", formattedValues);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix3D value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix3D value) {
    return "Labelled Matrix 3D (" + value.getZKeys().length + " x " + value.getYKeys().length + " x " + value.getXKeys().length + ")";
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }

}
