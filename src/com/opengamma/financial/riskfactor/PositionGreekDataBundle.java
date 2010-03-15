/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

/**
 * @author emcleod
 * 
 */
public class PositionGreekDataBundle {
  private final Double _data;

  public enum DataType {
    NUMBER_OF_CONTRACTS
  }

  public PositionGreekDataBundle(final Double data) {
    if (data == null)
      throw new IllegalArgumentException("Data was null");
    _data = data;
  }

  public Double getData() {
    return _data;
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
    final PositionGreekDataBundle other = (PositionGreekDataBundle) obj;
    if (_data == null) {
      if (other._data != null)
        return false;
    } else if (!_data.equals(other._data))
      return false;
    return true;
  }

}
