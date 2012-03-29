/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import com.opengamma.financial.analytics.LabelledMatrix1D;
import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;

import javax.time.calendar.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class LabelledMatrix1DConverter implements ResultConverter<LabelledMatrix1D> {

  @Override
  public Map<String, Double> convert(String valueName, LabelledMatrix1D value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    Object[] keys = value.getKeys();
    double[] values = value.getValues();

    for (int i = 0; i < values.length; i++) {
      Object k = keys[i];
      double v = values[i];
      returnValue.put(k.toString(), v);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return LocalDateLabelledMatrix1D.class;
  }
  
}
