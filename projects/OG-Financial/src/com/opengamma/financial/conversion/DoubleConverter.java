/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.Collections;
import java.util.Map;

/**
 * 
 */
public class DoubleConverter implements ResultConverter<Double> {

  @Override
  public Map<String, Double> convert(String valueName, Double value) {
    return Collections.singletonMap(valueName, value);
  }

  @Override
  public Class<?> getConvertedClass() {
    return Double.class;
  }
  
}
