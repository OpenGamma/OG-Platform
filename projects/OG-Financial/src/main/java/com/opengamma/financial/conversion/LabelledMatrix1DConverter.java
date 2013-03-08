/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.analytics.LabelledMatrix1D;

/**
 * 
 */
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D<?, ?>> {

  @Override
  public Map<String, Double> convert(String valueName, LabelledMatrix1D<?, ?> value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    Object[] keys = value.getKeys();
    double[] values = value.getValues();
    for (int i = 0; i < values.length; i++) {
      Object k = keys[i];
      double v = values[i];
      returnValue.put(valueName + "[" + k.toString() + "]", v);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return LabelledMatrix1D.class;
  }
  
}
