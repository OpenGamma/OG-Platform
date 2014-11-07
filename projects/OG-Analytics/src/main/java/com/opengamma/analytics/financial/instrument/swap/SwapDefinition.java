/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a generic swap with two legs. One should be payer and the other receiver.
 *
 */
//TODO get rid when checkstyle can actually handle this class declaration
//CSOFF
public class SwapDefinition implements InstrumentDefinitionWithData<Swap<? extends Payment, ? extends Payment>, ZonedDateTimeDoubleTimeSeries[]> {
  //CSON
  /** The first swap leg */
  private final AnnuityDefinition<? extends PaymentDefinition> _firstLeg;
  /** The second swap leg */
  private final AnnuityDefinition<? extends PaymentDefinition> _secondLeg;

  /**
   * @param firstLeg The first swap leg, not null
   * @param secondLeg The second swap leg, not null
   */
  public SwapDefinition(final AnnuityDefinition<? extends PaymentDefinition> firstLeg, final AnnuityDefinition<? extends PaymentDefinition> secondLeg) {
    ArgumentChecker.notNull(firstLeg, "first leg");
    ArgumentChecker.notNull(secondLeg, "second leg");
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
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitSwapDefinition(this);
  }

  @Override
  public Swap<? extends Payment, ? extends Payment> toDerivative(final ZonedDateTime date) {
    final Annuity<? extends Payment> firstLeg = getFirstLeg().toDerivative(date);
    final Annuity<? extends Payment> secondLeg = getSecondLeg().toDerivative(date);
    return new Swap<>(firstLeg, secondLeg);
  }

  @Override
  public Swap<? extends Payment, ? extends Payment> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] data) {
    ArgumentChecker.notNull(data, "index data time series array");
    ArgumentChecker.isTrue(data.length >= 2, "Generic swaps require two time series");
    final Annuity<? extends Payment> firstLeg = getFirstLeg().toDerivative(date, data[0]);
    final Annuity<? extends Payment> secondLeg = getSecondLeg().toDerivative(date, data[1]);
    return new Swap<>(firstLeg, secondLeg);
  }

}
