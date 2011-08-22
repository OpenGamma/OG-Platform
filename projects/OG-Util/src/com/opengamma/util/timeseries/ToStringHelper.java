/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Utility to assist with converting a time-series to a string.
 */
public class ToStringHelper {

  /**
   * Converts the series to a string.
   * 
   * @param ts  the series, not null
   * @return the string, not null
   */
  public static String toString(@SuppressWarnings("rawtypes") TimeSeries ts) {
    StringBuilder sb = new StringBuilder();
    sb.append(ts.getClass().getSimpleName());
    sb.append("["); 
    @SuppressWarnings("unchecked")
    Iterator<Entry<?, ?>> iterator = ts.iterator();
    while (iterator.hasNext()) {
      Entry<?, ?> next = iterator.next();
      sb.append("(");
      sb.append(next.getKey());
      sb.append(", ");
      sb.append(next.getValue());
      sb.append(")");
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append("]");
    return sb.toString();
  }

}
