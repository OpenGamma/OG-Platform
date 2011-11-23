/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Forex swap transaction (with a near and far leg).
 */
public class ForexSwapDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * The near leg.
   */
  private final ForexDefinition _nearLeg;
  /**
   * The far leg.
   */
  private final ForexDefinition _farLeg;

  /**
   * Constructor from the two Forex legs.
   * @param nearLeg The near leg.
   * @param farLeg The far leg.
   */
  public ForexSwapDefinition(ForexDefinition nearLeg, ForexDefinition farLeg) {
    Validate.notNull(nearLeg, "Near leg");
    Validate.notNull(farLeg, "Far leg");
    this._nearLeg = nearLeg;
    this._farLeg = farLeg;
  }

  /**
   * Constructor from the financial details.
   * @param currency1 The first currency.
   * @param currency2 The second currency.
   * @param nearDate The near date.
   * @param farDate The far date.
   * @param amount1 The amount of the near leg in the first currency.
   * @param forexRate The near leg forex rate.
   * @param forwardPoints The forward points, i.e. the far leg forex rate is forexRate+forwardPoints.
   */
  public ForexSwapDefinition(final Currency currency1, final Currency currency2, final ZonedDateTime nearDate, final ZonedDateTime farDate, final double amount1, double forexRate, 
      double forwardPoints) {
    Validate.notNull(currency1, "Currency 1");
    Validate.notNull(currency2, "Currency 2");
    Validate.notNull(nearDate, "Near date");
    Validate.notNull(farDate, "Far date");
    _nearLeg = new ForexDefinition(currency1, currency2, nearDate, amount1, forexRate);
    _farLeg = new ForexDefinition(currency1, currency2, farDate, -amount1, forexRate + forwardPoints);
  }

  /**
   * Gets the near leg.
   * @return The near leg.
   */
  public ForexDefinition getNearLeg() {
    return _nearLeg;
  }

  /**
   * Gets the far leg.
   * @return The far leg.
   */
  public ForexDefinition getFarLeg() {
    return _farLeg;
  }

  @Override
  /**
   * The first curve is the discounting curve for the first currency and the second curve is the discounting curve for the second currency.
   */
  public ForexSwap toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Forex nearLeg = _nearLeg.toDerivative(date, yieldCurveNames);
    Forex farLeg = _farLeg.toDerivative(date, yieldCurveNames);
    return new ForexSwap(nearLeg, farLeg);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitForexSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitForexSwapDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _farLeg.hashCode();
    result = prime * result + _nearLeg.hashCode();
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
    ForexSwapDefinition other = (ForexSwapDefinition) obj;
    if (!ObjectUtils.equals(_farLeg, other._farLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_nearLeg, other._nearLeg)) {
      return false;
    }
    return true;
  }

}
