/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class MultipleCurrencyAmountConverter implements ResultConverter<MultipleCurrencyAmount> {

  private DoubleConverter _doubleConverter;

  public MultipleCurrencyAmountConverter(DoubleConverter doubleConverter) {
    _doubleConverter = doubleConverter;
  }
  
  @Override
  public Object convertForDisplay(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value, ConversionMode mode) {
    Map<String, Object> result = new HashMap<String, Object>();
    int length = value.size();
    result.put("summary", length);
    
    if (mode == ConversionMode.FULL) {
      Map<Object, Object> labelledValues = new LinkedHashMap<Object, Object>();
      Iterator<CurrencyAmount> iter = value.iterator();
      while (iter.hasNext()) {
        CurrencyAmount ca = iter.next();
        String label = ca.getCurrency().getCode(); 
        Object currentLabel = context.convert(label, ConversionMode.SUMMARY);
        Object currentValue = _doubleConverter.convertForDisplay(context, valueSpec, ca.getAmount(), ConversionMode.SUMMARY);
        labelledValues.put(currentLabel, currentValue);
      }
      result.put("full", labelledValues);
    }
    return result;
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value) {
    return null;
  }

  @Override
  public String getFormatterName() {
    return "LABELLED_MATRIX_1D";
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value) {
    return value.toString();
  }

}
