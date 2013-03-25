/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public class CurrencyAmountConverter implements ResultConverter<CurrencyAmount> {

  @Override
  public Map<String, Double> convert(String valueName, CurrencyAmount value) {
    return ImmutableMap.of(valueName, value.getAmount());
  }

  @Override
  public Class<?> getConvertedClass() {
    return CurrencyAmount.class;
  }

}
