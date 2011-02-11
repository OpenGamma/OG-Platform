/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.bond.definition.BondForward;

/**
 * 
 */
public class BondFuture implements InterestRateDerivative {
  private final BondForward[] _deliverables;
  private final double[] _conversionFactors;
  private final double _price;

  public BondFuture(final BondForward[] deliverables, final double[] conversionFactors, final double price) {
    Validate.noNullElements(deliverables, "null bond deliverables");
    Validate.notNull(conversionFactors, "null conversion factors");
    Validate.isTrue(deliverables.length > 0, "bond array was empty");
    Validate.isTrue(deliverables.length == conversionFactors.length);
    Validate.isTrue(price > 0, "bond future price must be positive");
    _deliverables = deliverables;
    _conversionFactors = conversionFactors;
    _price = price;
  }

  public double[] getConversionFactors() {
    return _conversionFactors;
  }

  public BondForward[] getBondForwards() {
    return _deliverables;
  }

  public double getPrice() {
    return _price;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    final long temp = Double.doubleToLongBits(_price);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_deliverables);
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
    if (Double.doubleToLongBits(_price) != Double.doubleToLongBits(other._price)) {
      return false;
    }
    if (!Arrays.equals(_deliverables, other._deliverables)) {
      return false;
    }
    return Arrays.equals(_conversionFactors, other._conversionFactors);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitBondFuture(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visitBondFuture(this);
  }

}
