/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;
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
  public ForexSwapDefinition(final ForexDefinition nearLeg, final ForexDefinition farLeg) {
    ArgumentChecker.notNull(nearLeg, "Near leg");
    ArgumentChecker.notNull(farLeg, "Far leg");
    _nearLeg = nearLeg;
    _farLeg = farLeg;
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
  public ForexSwapDefinition(final Currency currency1, final Currency currency2, final ZonedDateTime nearDate, final ZonedDateTime farDate, final double amount1,
      final double forexRate, final double forwardPoints) {
    ArgumentChecker.notNull(currency1, "Currency 1");
    ArgumentChecker.notNull(currency2, "Currency 2");
    ArgumentChecker.notNull(nearDate, "Near date");
    ArgumentChecker.notNull(farDate, "Far date");
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

  /**
   * {@inheritDoc}
   * The first curve is the discounting curve for the first currency and the second curve is the discounting curve for the second currency.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());  }

  /**
   * {@inheritDoc}
   * The first curve is the discounting curve for the first currency and the second curve is the discounting curve for the second currency.
   */
  @Override
  public InstrumentDerivative toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(_farLeg.getExchangeDate()), "date is after payment far date");
    if (date.isAfter(_nearLeg.getExchangeDate())) { // Implementation note: only the far leg left.
      return _farLeg.toDerivative(date);
    }
    final Forex nearLeg = _nearLeg.toDerivative(date);
    final Forex farLeg = _farLeg.toDerivative(date);
    return new ForexSwap(nearLeg, farLeg);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexSwapDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
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
    final ForexSwapDefinition other = (ForexSwapDefinition) obj;
    if (!ObjectUtils.equals(_farLeg, other._farLeg)) {
      return false;
    }
    if (!ObjectUtils.equals(_nearLeg, other._nearLeg)) {
      return false;
    }
    return true;
  }

}
