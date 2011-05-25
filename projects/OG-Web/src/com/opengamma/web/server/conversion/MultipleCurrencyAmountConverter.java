/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class MultipleCurrencyAmountConverter implements ResultConverter<MultipleCurrencyAmount> {

  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int size = value.size();
    result.put("summary", size);
    if (mode == ConversionMode.FULL) {
      Set<Object> values = new LinkedHashSet<Object>();
      for (Map.Entry<Currency, Double> entry : value) {
        Object converted = context.convert(CurrencyAmount.of(entry.getKey(), entry.getValue()), mode);
        values.add(converted);
      }
      result.put("full", values);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value) {
    return null;
  }

  @Override
  public String getFormatterName() {
    return "MULTIPLE_CURRENCY_AMOUNT";
  }

}
