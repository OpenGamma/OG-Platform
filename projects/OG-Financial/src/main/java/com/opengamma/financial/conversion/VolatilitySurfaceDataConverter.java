/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;

/**
 * 
 */
public class VolatilitySurfaceDataConverter implements ResultConverter<VolatilitySurfaceData<Object, Object>> {

  @Override
  public Map<String, Double> convert(String valueName, VolatilitySurfaceData<Object, Object> value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    for (int xIdx = 0; xIdx < value.getXs().length; xIdx++) {
      Object x = value.getXs()[xIdx];
      for (int yIdx = 0; yIdx < value.getYs().length; yIdx++) {
        Object y = value.getYs()[yIdx];
        returnValue.put(valueName + "[" + x + "]" + "[" + y + "]", value.getVolatility(x, y));
      }
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return VolatilitySurfaceData.class;
  }

}
