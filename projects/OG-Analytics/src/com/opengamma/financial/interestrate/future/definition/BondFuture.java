/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
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
  private final double[] _conversionFactors;

  public BondFuture(final Bond[] bonds, final double[] conversionFactors) {
    Validate.noNullElements(bonds, "null bonds");
    Validate.notNull(conversionFactors, "null conversion factors");
    Validate.isTrue(bonds.length > 0, "bond array was empty");
    Validate.isTrue(bonds.length == conversionFactors.length);
    _bonds = bonds;
    _conversionFactors = conversionFactors;
  }

  /**
   * Gets the array of deliverable bond conversion factors
   * @return the conversionFactors
   */
  public double[] getConversionFactors() {
    return _conversionFactors;
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
    result = prime * result + Arrays.hashCode(_conversionFactors);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFuture other = (BondFuture) obj;
    if (!Arrays.equals(_bonds, other._bonds)) {
      return false;
    }
    return Arrays.equals(_conversionFactors, other._conversionFactors);
  }

}
