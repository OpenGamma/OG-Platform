/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilitySensitivity;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class PresentValueVolatilitySensitivityConverter implements ResultConverter<PresentValueForexBlackVolatilitySensitivity> {
  private DoubleConverter _doubleConverter;

  public PresentValueVolatilitySensitivityConverter(DoubleConverter doubleConverter) {
    _doubleConverter = doubleConverter;
  }
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, PresentValueForexBlackVolatilitySensitivity value, ConversionMode mode) {
    final Map<String, Object> result = new HashMap<String, Object>();
    final HashMap<DoublesPair, Double> map = value.getVega().getMap();
    final int length = value.getVega().getMap().size();
    result.put("summary", length);
    if (mode == ConversionMode.FULL) {
      final Map<Object, Object> matrix = new LinkedHashMap<Object, Object>();
      for (final Map.Entry<DoublesPair, Double> entry : map.entrySet()) {
        final StringBuffer sb = new StringBuffer();
        sb.append(_doubleConverter.convertForDisplay(context, valueSpec, entry.getKey().first, mode));
        sb.append(", ");
        sb.append(_doubleConverter.convertForDisplay(context, valueSpec, entry.getKey().second, mode));
        matrix.put(sb.toString(), _doubleConverter.convertForDisplay(context, valueSpec, entry.getValue(), mode));
      }
      result.put("full", matrix);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, PresentValueForexBlackVolatilitySensitivity value) {
    return null;
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_1D";
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, PresentValueForexBlackVolatilitySensitivity value) {
    return value.toString();
  }

}
