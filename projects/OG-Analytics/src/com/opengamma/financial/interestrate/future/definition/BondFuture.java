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

  public BondFuture(final BondForward[] deliverables, final double[] conversionFactors) {
    Validate.noNullElements(deliverables, "null bond deliverables");
    Validate.notNull(conversionFactors, "null conversion factors");
    Validate.isTrue(deliverables.length > 0, "bond array was empty");
    Validate.isTrue(deliverables.length == conversionFactors.length);
    _deliverables = deliverables;
    _conversionFactors = conversionFactors;
  }

  public double[] getConversionFactors() {
    return _conversionFactors;
  }

  public BondForward[] getBondForwards() {
    return _deliverables;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
    if (!Arrays.equals(_deliverables, other._deliverables)) {
      return false;
    }
    return Arrays.equals(_conversionFactors, other._conversionFactors);
  }

  @Override
  public <S, T> T accept(final InterestRateDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visit(this, data);
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<?, T> visitor) {
    return visitor.visit(this);
  }

}
