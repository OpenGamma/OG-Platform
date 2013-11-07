/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

/**
 * Converts {@link BigDecimal} into a double result.
 */
public class BigDecimalConverter implements ResultConverter<BigDecimal> {

  @Override
  public Map<String, Double> convert(String valueName, BigDecimal value) {
    return Collections.singletonMap(valueName, value.doubleValue());
  }

  @Override
  public Class<?> getConvertedClass() {
    return BigDecimal.class;
  }

}
