/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Volatility index future definition. An IndexFuture is always cash-settled.
 * eg UXH5 Index, http://cfe.cboe.com/products/Products_VIX.aspx
 */
public class VolatilityIndexFutureDefinition extends IndexFutureDefinition {

  /**
   * Constructor for Equity Index Futures, always cash-settled.
   *
   * @param expiryDate  the time and the day that a particular delivery month of a futures contract stops trading, as well as the final settlement price for that contract
   * @param settlementDate settlement date
   * @param strikePrice reference price
   * @param currency currency
   * @param unitAmount  size of a unit
   * @param underlying  identifier of the underlying commodity
   */
  public VolatilityIndexFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double strikePrice,
      final Currency currency, final double unitAmount, final ExternalId underlying) {
    super(expiryDate, settlementDate, strikePrice, currency, unitAmount, underlying);
  }

  @Override
  public VolatilityIndexFuture toDerivative(final ZonedDateTime date, final Double referencePrice, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public VolatilityIndexFuture toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    final VolatilityIndexFuture newDeriv = new VolatilityIndexFuture(timeToFixing, timeToDelivery, getReferencePrice(), getCurrency(), getUnitAmount());
    return newDeriv;
  }

  @Override
  public VolatilityIndexFuture toDerivative(final ZonedDateTime date, final Double referencePrice) {
    ArgumentChecker.notNull(date, "date");
    if (referencePrice == null) {
      return toDerivative(date);
    }
    final double timeToFixing = TimeCalculator.getTimeBetween(date, getExpiryDate());
    final double timeToDelivery = TimeCalculator.getTimeBetween(date, getSettlementDate());
    final VolatilityIndexFuture newDeriv = new VolatilityIndexFuture(timeToFixing, timeToDelivery, referencePrice, getCurrency(), getUnitAmount());
    return newDeriv;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilityIndexFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitVolatilityIndexFutureDefinition(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof VolatilityIndexFutureDefinition)) {
      return false;
    }
    return super.equals(obj);
  }
}
