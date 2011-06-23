/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.LinkedHashMap;
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
    int length = value.getYKeys().length;
    int width = value.getXKeys().length;
    result.put("summary", length * width);
    
    String tabs = "\t\t\t\t\t";
    if (mode == ConversionMode.FULL) {
      Map<Object, Object> labelledValues = new LinkedHashMap<Object, Object>();
      for (int i = 0; i < length; i++) {
        Object label;
        String currentValue = "";
        if (i == 0) {
          label = "";
          for (int j = 0; j < width; j++) {
            currentValue += Double.toString(((double) (Math.round((Double) value.getXKeys()[j] * 10))) / 10.);
            currentValue += tabs;
          }
          Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
          labelledValues.put(currentLabel, currentValue);
        } 
        currentValue = "";
        label = Double.toString(((double) (Math.round((Double) value.getYKeys()[i] * 10))) / 10.);
        for (int j = 0; j < width; j++) {
          currentValue += context.getDoubleConverter().convertForDisplay(context, valueSpec, value.getValues()[i][j], mode);
          currentValue += tabs;        
        }
        Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }
    return result;
//    Map<String, Object> result = new HashMap<String, Object>();
//    int rows = value.getYKeys().length;
//    int columns = value.getXKeys().length;
//    result.put("summary", rows + " x " + columns);
//    if (mode == ConversionMode.FULL) {
//      String[] xStrings = new String[columns];
//      String[] yStrings = new String[rows];
//      for (int i = 0; i < xStrings.length; i++) {
//        xStrings[i] = value.getXLabels()[i].toString();
//      }
//      result.put("x", xStrings);
//      for (int i = 0; i < yStrings.length; i++) {
//        yStrings[i] = value.getYLabels()[i].toString();
//      }
//      result.put("y", yStrings);
//      result.put("surface", value.getValues());
//    }
//    return result;
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
