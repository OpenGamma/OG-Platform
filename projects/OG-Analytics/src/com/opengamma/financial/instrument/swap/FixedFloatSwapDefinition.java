/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;

/**
 * 
 */
public class FixedFloatSwapDefinition implements FixedIncomeInstrumentDefinition<FixedCouponSwap<Payment>> {
  private final FixedSwapLegDefinition _fixedLeg;
  private final FloatingSwapLegDefinition _floatingLeg;

  public FixedFloatSwapDefinition(final FixedSwapLegDefinition fixedLeg, final FloatingSwapLegDefinition floatingLeg) {
    Validate.notNull(fixedLeg, "fixed leg");
    Validate.notNull(floatingLeg, "floating leg");
    _fixedLeg = fixedLeg;
    _floatingLeg = floatingLeg;
  }

  public FixedSwapLegDefinition getFixedLeg() {
    return _fixedLeg;
  }

  public FloatingSwapLegDefinition getFloatingLeg() {
    return _floatingLeg;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _fixedLeg.hashCode();
    result = prime * result + _floatingLeg.hashCode();
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
    final FixedFloatSwapDefinition other = (FixedFloatSwapDefinition) obj;
    if (!ObjectUtils.equals(_fixedLeg, other._fixedLeg)) {
      return false;
    }
    return ObjectUtils.equals(_floatingLeg, other._floatingLeg);
  }

  @Override
  public FixedCouponSwap<Payment> toDerivative(final LocalDate date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = _fixedLeg.toDerivative(date, yieldCurveNames);
    final GenericAnnuity<Payment> floatingLeg = _floatingLeg.toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Payment>(fixedLeg, floatingLeg);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitFixedFloatSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitFixedFloatSwapDefinition(this);
  }

}
