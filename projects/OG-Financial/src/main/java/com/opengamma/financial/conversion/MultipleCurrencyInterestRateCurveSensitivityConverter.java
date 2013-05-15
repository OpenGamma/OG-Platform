/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class MultipleCurrencyInterestRateCurveSensitivityConverter implements ResultConverter<MultipleCurrencyInterestRateCurveSensitivity> {

  @Override
  public Map<String, Double> convert(String valueName, MultipleCurrencyInterestRateCurveSensitivity value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    for (Currency ccy : value.getCurrencies()) {
      InterestRateCurveSensitivity ccySensitivity = value.getSensitivity(ccy);
      for (Map.Entry<String, List<DoublesPair>> curveSensitivities : ccySensitivity.getSensitivities().entrySet()) {
        String curveName = curveSensitivities.getKey();
        for (DoublesPair sensitivityEntry : curveSensitivities.getValue()) {
          Double cashFlowTime = sensitivityEntry.getFirst();
          Double sensitivityValue = sensitivityEntry.getSecond();
          String key = valueName + "[" + ccy.getCode() + "][" + curveName + "]";
          returnValue.put(key + "[time]", cashFlowTime);
          returnValue.put(key + "[value]", sensitivityValue);
        }
      }
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return MultipleCurrencyInterestRateCurveSensitivity.class;
  }

}
