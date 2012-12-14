/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class MultipleCurrencyAmountConverter implements ResultConverter<MultipleCurrencyAmount> {

  @Override
  public Map<String, Double> convert(String valueName, MultipleCurrencyAmount value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    for (CurrencyAmount currencyAmount : value.getCurrencyAmounts()) {
      returnValue.put(valueName + "[" + currencyAmount.getCurrency().getCode() + "]", currencyAmount.getAmount());
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return MultipleCurrencyAmount.class;
  }

}
