/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.Swap;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a generic swap with two legs. One should be payer and the other receiver.
 *
 */
//TODO get rid when checkstyle can actually handle this class declaration
//CSOFF
public class SwapDefinition implements InstrumentDefinitionWithData<Swap<? extends Payment, ? extends Payment>, DoubleTimeSeries<ZonedDateTime>[]> {
  //CSON
  private final AnnuityDefinition<? extends PaymentDefinition> _firstLeg;
  private final AnnuityDefinition<? extends PaymentDefinition> _secondLeg;

  public SwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> firstLeg, final AnnuityDefinition<? extends PaymentDefinition> secondLeg) {
    Validate.notNull(firstLeg, "first leg");
    Validate.notNull(secondLeg, "second leg");
    Validate.isTrue((firstLeg.isPayer() != secondLeg.isPayer()), "both legs have same payer flag");
    _firstLeg = firstLeg;
    _secondLeg = secondLeg;
  }

  /**
   * Gets the first leg.
   * @return The first leg.
   */
  public AnnuityDefinition<? extends PaymentDefinition> getFirstLeg() {
    return _firstLeg;
  }

  /**
   * Gets the second leg.
   * @return The second leg.
   */
  public AnnuityDefinition<? extends PaymentDefinition> getSecondLeg() {
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
    final SwapDefinition other = (SwapDefinition) obj;
    if (!ObjectUtils.equals(_firstLeg, other._firstLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_secondLeg, other._secondLeg)) {
      return false;
    }
    return true;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapDefinition(this);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Swap<? extends Payment, ? extends Payment> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<? extends Payment> firstLeg = getFirstLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Payment> secondLeg = getSecondLeg().toDerivative(date, yieldCurveNames);
    return new Swap(firstLeg, secondLeg);
  }

  @SuppressWarnings({"unchecked", "rawtypes" })
  @Override
  public Swap<? extends Payment, ? extends Payment> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] data, final String... yieldCurveNames) {
    Validate.notNull(data, "index data time series array");
    Validate.isTrue(data.length > 1, "index data time series must contain at least two elements");
    final GenericAnnuity<? extends Payment> firstLeg = getFirstLeg().toDerivative(date, data[0], yieldCurveNames);
    final GenericAnnuity<? extends Payment> secondLeg = getSecondLeg().toDerivative(date, data[1], yieldCurveNames);
    return new Swap(firstLeg, secondLeg);
  }
}
