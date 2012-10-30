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
import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.id.ExternalId;

/**
 * Converter for {@link LabelledMatrix1D} results.
 */
@SuppressWarnings("rawtypes")
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D> {
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix1D value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int length = value.getKeys().length;
    result.put("summary", length);
    
    if (mode == ConversionMode.FULL) {
      // Only interested in labels and values
      Map<Object, Object> labelledValues = new LinkedHashMap<Object, Object>();
      for (int i = 0; i < length; i++) {
        Object labelObject = value.getLabels()[i];        
        String label = labelObject instanceof ExternalId ? ((ExternalId) labelObject).getValue() : labelObject.toString(); 
        Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
        Object currentValue = context.getDoubleConverter().convertForDisplay(context, valueSpec, value.getValues()[i], ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }
    
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix1D value) {
    return null;
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, LabelledMatrix1D value) {
    StringBuilder sb = new StringBuilder();
    boolean isFirst = true;
    for (int i = 0; i < value.getKeys().length; i++) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append("; ").append(value.getValues()[i]);
      }
      Object label = value.getLabels()[i];
      Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
      sb.append(currentLabel).append("=").append(value.getValues()[i]);
    }
    return sb.length() > 0 ? sb.toString() : null;
  }
  
  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_1D";
  }

}
