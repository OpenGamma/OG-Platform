/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.LocalDateLabelledMatrix1D;

/**
 * 
 */
public class LocalDateLabelledMatrix1DConverter implements ResultConverter<LocalDateLabelledMatrix1D> {

  @Override
  public Map<String, Double> convert(String valueName, LocalDateLabelledMatrix1D value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    LocalDate[] keys = value.getKeys();
    double[] values = value.getValues();

    for (int i = 0; i < values.length; i++) {
      LocalDate k = keys[i];
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
