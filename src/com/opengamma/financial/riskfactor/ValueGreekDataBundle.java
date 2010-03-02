/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emcleod
 * 
 */
public class ValueGreekDataBundle {
  private static final Logger s_Log = LoggerFactory.getLogger(ValueGreekDataBundle.class);
  private final Map<DataType, Double> _data;

  public enum DataType {
    UNDERLYING_PRICE, OPTION_POINT_VALUE, NUMBER_OF_CONTRACTS
  }

  public ValueGreekDataBundle(final Map<DataType, Double> data) {
    if (data == null)
      throw new IllegalArgumentException("Data was null");
    _data = data;
  }

  public Map<DataType, Double> getData() {
    return _data;
  }

  public Double getDataForType(final DataType type) {
    if (!_data.containsKey(type)) {
      s_Log.info("Data map did not contain " + type + " data");
      return null;
    }
    return _data.get(type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ValueGreekDataBundle other = (ValueGreekDataBundle) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    return true;
  }
}
