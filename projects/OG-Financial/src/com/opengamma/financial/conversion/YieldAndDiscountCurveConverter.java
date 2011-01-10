/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * 
 */
public class YieldAndDiscountCurveConverter implements ResultConverter<YieldAndDiscountCurve> {
  
  @Override
  public Map<String, Double> convert(String valueName, YieldAndDiscountCurve value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    
    for (Double x : value.getCurve().getXData()) {
      Double y = value.getCurve().getYValue(x);
      returnValue.put(valueName + "[" + x + "]", y);
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return YieldAndDiscountCurve.class;
  }

}
