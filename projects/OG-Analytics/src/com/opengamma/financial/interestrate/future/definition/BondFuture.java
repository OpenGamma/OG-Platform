/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.definition.Bond;

/**
 * 
 */
public class BondFuture {

  private final Bond[] _bonds;
  private final double[] _convertionFactors;

  public BondFuture(final Bond[] bonds, final double[] convertionFactors) {
    Validate.noNullElements(bonds, "null bonds");
    Validate.notNull(convertionFactors, "null convertion facrtors");
    Validate.isTrue(bonds.length == convertionFactors.length);

    _bonds = bonds;
    _convertionFactors = convertionFactors;
  }

  /**
   * Gets the convertionFactors field.
   * @return the convertionFactors
   */
  public double[] getConvertionFactors() {
    return _convertionFactors;
  }

  /**
   * Gets the bonds field.
   * @return the bonds
   */
  public Bond[] getBonds() {
    return _bonds;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_bonds);
    result = prime * result + Arrays.hashCode(_convertionFactors);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFuture other = (BondFuture) obj;
    if (!Arrays.equals(_bonds, other._bonds)) {
      return false;
    }
    if (!Arrays.equals(_convertionFactors, other._convertionFactors)) {
      return false;
    }
    return true;
  }

}
