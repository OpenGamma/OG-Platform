/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class DoubleMatrix1DConverter implements ResultConverter<DoubleMatrix1D> {

  @Override
  public Map<String, Double> convert(String valueName, DoubleMatrix1D value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    double[] data = value.getData();
    for (int i = 0; i < data.length; i++) {
      returnValue.put(valueName + "[" + i + "]", data[i]);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return DoubleMatrix1D.class;
  }
  
}
