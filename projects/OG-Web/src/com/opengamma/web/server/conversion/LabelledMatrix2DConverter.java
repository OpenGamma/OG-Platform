/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.LabelledMatrix2D;

/**
 * 
 */
@SuppressWarnings("rawtypes")
public class LabelledMatrix2DConverter implements ResultConverter<LabelledMatrix2D> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix2D value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int rows = value.getYKeys().length;
    int columns = value.getXKeys().length;
    result.put("summary", rows + " x " + columns);
    if (mode == ConversionMode.FULL) {
      String[] xStrings = new String[columns];
      String[] yStrings = new String[rows];
      for (int i = 0; i < xStrings.length; i++) {
        xStrings[i] = value.getXLabels()[i].toString();
      }
      result.put("x", xStrings);
      for (int i = 0; i < yStrings.length; i++) {
        yStrings[i] = value.getYLabels()[i].toString();
      }
      result.put("y", yStrings);
      result.put("surface", value.getValues());
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix2D value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix2D value) {
    return "Labelled Matrix 2D (" + value.getYKeys().length + " x " + value.getXKeys().length;
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_2D";
  }

}
