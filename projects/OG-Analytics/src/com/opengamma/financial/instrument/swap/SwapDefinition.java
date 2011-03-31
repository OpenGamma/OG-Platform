/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.Swap;

/**
 * Class describing a generic swap with two legs. One should be payer and the other receiver.
 * @param <P1> The payment type on first leg.
 * @param <P2> The payment type on second leg.
 *
 */
public class SwapDefinition<P1 extends PaymentDefinition, P2 extends PaymentDefinition> implements FixedIncomeInstrumentDefinition<Swap<? extends Payment, ? extends Payment>> {

  private final AnnuityDefinition<P1> _firstLeg;
  private final AnnuityDefinition<P2> _secondLeg;

  public SwapDefinition(AnnuityDefinition<P1> firstLeg, AnnuityDefinition<P2> secondLeg) {
    Validate.notNull(firstLeg, "first leg");
    Validate.notNull(secondLeg, "second leg");
    Validate.isTrue((firstLeg.isPayer() != secondLeg.isPayer()), "both legs have same payer flag");
    _firstLeg = firstLeg;
    _secondLeg = secondLeg;
  }

  /**
   * Gets the firstLeg field.
   * @return the first Leg
   */
  public AnnuityDefinition<P1> getFirstLeg() {
    return _firstLeg;
  }

  /**
   * Gets the secondLeg field.
   * @return the second Leg
   */
  public AnnuityDefinition<P2> getSecondLeg() {
    return _secondLeg;
  }

  @Override
  public String toString() {
    String result = "Swap : \n";
    result += "First leg: \n" + _firstLeg.toString();
    result += "\nSecond leg: \n" + _secondLeg.toString();
    return result;
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
    SwapDefinition<?, ?> other = (SwapDefinition<?, ?>) obj;
    if (!ObjectUtils.equals(_firstLeg, other._firstLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_secondLeg, other._secondLeg)) {
      return false;
    }
    return true;
  }

  @Override
  public Swap<? extends Payment, ? extends Payment> toDerivative(LocalDate date, String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapDefinition(this);
  }

}
