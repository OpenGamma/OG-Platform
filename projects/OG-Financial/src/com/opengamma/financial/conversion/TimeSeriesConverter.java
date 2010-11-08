/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.conversion;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class TimeSeriesConverter implements ResultConverter<DoubleTimeSeries<?>> {

  @Override
  public Map<String, Double> convert(String valueName, DoubleTimeSeries<?> value) {
    Map<String, Double> returnValue = new HashMap<String, Double>();
    for (Map.Entry<?, Double> point : value) {
      String key = valueName + "[" + point.getKey().toString() + "]";
      returnValue.put(key, point.getValue());      
    }
    return returnValue;
  }

  @Override
  public Class<?> getConvertedClass() {
    return DoubleTimeSeries.class;
  }
  
}
