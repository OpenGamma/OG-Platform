/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.financial.analytics.LabelledMatrix1D;

/**
 * 
 */
@SuppressWarnings("rawtypes")
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D> {
  
  @Override
  public Object convert(ResultConverterCache context, String valueName, LabelledMatrix1D value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int length = value.getKeys().length;
    result.put("summary", length);
    
    if (mode == ConversionMode.FULL) {
      // Only interested in labels and values
      Map<Object, Object> labelledValues = new LinkedHashMap<Object, Object>();
      for (int i = 0; i < length; i++) {
        Object label = value.getLabels()[i];
        Object currentLabel = context.convert(label, mode);
        Object currentValue = context.getDoubleConverter().convert(context, valueName, value.getValues()[i], ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }
    
    return result;
  }

  @Override
  public String getResultTypeName() {
    return "LABELLED_MATRIX_1D";
  }

}
