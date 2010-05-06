/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.Map;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.Matrix;

/**
 *
 */
public class PnLDataBundle {
  private final Map<Underlying, DoubleMatrix1D[]> _underlyingData;
  private final Map<Sensitivity<?>, Matrix<?>> _matrices;
  private final int _n;

  public PnLDataBundle(final Map<Underlying, DoubleMatrix1D[]> underlyingData, final Map<Sensitivity<?>, Matrix<?>> matrices) {
    if (underlyingData == null)
      throw new IllegalArgumentException("Underlying data map was null");
    if (underlyingData.size() == 0)
      throw new IllegalArgumentException("Underlying data map was empty");
    if (matrices == null)
      throw new IllegalArgumentException("Matrix map was null");
    if (matrices.isEmpty())
      throw new IllegalArgumentException("Matrix map was empty");
    _underlyingData = underlyingData;
    _matrices = matrices;
    _n = underlyingData.values().iterator().next().length;
  }

  public Map<Underlying, DoubleMatrix1D[]> getUnderlyingData() {
    return _underlyingData;
  }

  public Map<Sensitivity<?>, Matrix<?>> getMatrices() {
    return _matrices;
  }

  public int getLength() {
    return _n;
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
    result = prime * result + ((_matrices == null) ? 0 : _matrices.hashCode());
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
    final PnLDataBundle other = (PnLDataBundle) obj;
    if (_matrices == null) {
      if (other._matrices != null)
        return false;
    } else if (!_matrices.equals(other._matrices))
      return false;
    if (_underlyingData == null) {
      if (other._underlyingData != null)
        return false;
    } else if (!_underlyingData.equals(other._underlyingData))
      return false;
    return true;
  }

}
