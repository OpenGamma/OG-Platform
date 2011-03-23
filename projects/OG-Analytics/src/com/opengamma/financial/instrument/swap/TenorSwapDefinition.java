/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class TenorSwapDefinition implements FixedIncomeInstrumentDefinition<TenorSwap<Payment>> {
  private static final Logger s_logger = LoggerFactory.getLogger(TenorSwapDefinition.class);
  private final FloatingSwapLegDefinition _firstLeg;
  private final FloatingSwapLegDefinition _secondLeg;

  public TenorSwapDefinition(final FloatingSwapLegDefinition firstLeg, final FloatingSwapLegDefinition secondLeg) {
    Validate.notNull(firstLeg, "first leg");
    Validate.notNull(secondLeg, "second leg");
    Validate.isTrue(CompareUtils.closeEquals(firstLeg.getSpread(), 0), "Spread on first leg must be zero"); //TODO:why?
    _firstLeg = firstLeg;
    _secondLeg = secondLeg;
  }

  public FloatingSwapLegDefinition getFirstLeg() {
    return _firstLeg;
  }

  public FloatingSwapLegDefinition getSecondLeg() {
    return _secondLeg;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _firstLeg.hashCode();
    result = prime * result + _secondLeg.hashCode();
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
    final TenorSwapDefinition other = (TenorSwapDefinition) obj;
    if (!ObjectUtils.equals(_firstLeg, other._firstLeg)) {
      return false;
    }
    return ObjectUtils.equals(_secondLeg, other._secondLeg);
  }

  @Override
  public TenorSwap<Payment> toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 2);
    s_logger.info("Using first yield curve name as the funding curve name, the second as the pay leg libor curve name and the third as the receive leg libor curve name");
    final GenericAnnuity<Payment> payLeg = _firstLeg.toDerivative(date, yieldCurveNames[0], yieldCurveNames[1]);
    final GenericAnnuity<Payment> receiveLeg = _secondLeg.toDerivative(date, yieldCurveNames[0], yieldCurveNames[2]);
    return new TenorSwap<Payment>(payLeg, receiveLeg);
  }

  @Override
  public <U, V> V accept(final FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitTenorSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitTenorSwapDefinition(this);
  }

}
