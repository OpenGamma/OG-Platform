/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResult;
import com.opengamma.financial.greeks.GreekResultCollection;

public class GreekDataBundle {
  private final GreekResultCollection _greekValues;
  private final Map<Object, Double> _underlyingData;

  public GreekDataBundle(final GreekResultCollection greekValues, final Map<Object, Double> underlyingData) {
    if (greekValues == null)
      throw new IllegalArgumentException("GreekResultCollection was null");
    if (greekValues.isEmpty())
      throw new IllegalArgumentException("GreekResultCollection was empty");
    if (underlyingData == null)
      throw new IllegalArgumentException("Underlying data map was null");
    if (underlyingData.isEmpty())
      throw new IllegalArgumentException("Underlying data map was empty");
    _greekValues = greekValues;
    _underlyingData = underlyingData;
  }

  public GreekResultCollection getGreekResults() {
    return _greekValues;
  }

  public Map<Object, Double> getUnderlyingData() {
    return _underlyingData;
  }

  public double getUnderlyingDataForObject(final Object o) {
    if (_underlyingData.containsKey(o))
      return _underlyingData.get(o);
    throw new IllegalArgumentException("Underlying data map did not contain a value for " + o);
  }

  public GreekResult<?> getGreekResultForGreek(final Greek greek) {
    if (_greekValues.containsKey(greek))
      return _greekValues.get(greek);
    throw new IllegalArgumentException("Greek result collection did not contain a value for " + greek);
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
    result = prime * result + ((_greekValues == null) ? 0 : _greekValues.hashCode());
    result = prime * result + ((_underlyingData == null) ? 0 : _underlyingData.hashCode());
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
    final GreekDataBundle other = (GreekDataBundle) obj;
    if (_greekValues == null) {
      if (other._greekValues != null)
        return false;
    } else if (!_greekValues.equals(other._greekValues))
      return false;
    if (_underlyingData == null) {
      if (other._underlyingData != null)
        return false;
    } else if (!_underlyingData.equals(other._underlyingData))
      return false;
    return true;
  }
}
