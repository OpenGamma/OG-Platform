/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import java.util.Iterator;

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
//    Map<String, Object> result = new HashMap<String, Object>();
//    System.err.println(value);
//    int size = value.size();
//    result.put("summary", size);
//    if (mode == ConversionMode.FULL) {
//      Set<Object> values = new LinkedHashSet<Object>();
//      for (CurrencyAmount entry : value) {
//        Object converted = context.convert(entry, mode);
//        values.add(converted);
//      }
//      result.put("full", values);
//    }
    StringBuilder sb = new StringBuilder();
    Iterator<CurrencyAmount> iterator = value.iterator();
    while (iterator.hasNext()) {
      sb.append(_doubleConverter.convertForDisplay(context, valueSpec, iterator.next(), mode));
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }

  @Override
  public Object convertForHistory(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value) {
    if (value.size() > 0) {
      return _doubleConverter.convertForHistory(context, valueSpec, value.iterator().next()); 
    } else {
      return 0;
    }
  }

  @Override
  public String getFormatterName() {
    return "MULTIPLE_CURRENCY_AMOUNT";
  }

  @Override
  public String convertToText(ResultConverterCache context, ValueSpecification valueSpec, MultipleCurrencyAmount value) {
    return value.toString();
  }

}
