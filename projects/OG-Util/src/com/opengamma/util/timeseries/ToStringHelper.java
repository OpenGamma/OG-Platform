/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 
 */
public class ToStringHelper {
  
  public static String toString(TimeSeries ts) {
    StringBuilder sb = new StringBuilder();
    sb.append(ts.getClass().getSimpleName());
    sb.append("["); 
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
