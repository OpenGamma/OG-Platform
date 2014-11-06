/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a foreign exchange non-deliverable forward transaction.
 * The transaction is XXX/YYY where YYY is the currency for the cash-settlement. A NDF KRW/USD with USD cash settlement is stored with KRW as currency1 and USD as currency2.
 */
// TODO: Review: Should the transaction be stored as KRW/USD or USD/KRW?
// REVIEW: should we have a "fixing process" like we have for CouponIbor?
public class ForexNonDeliverableForwardDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * First currency of the transaction.
   */
  private final Currency _currency1;
  /**
   * Second currency of the transaction. The cash settlement is done in this currency.
   */
  private final Currency _currency2;
  /**
   * Notional of the transaction (in currency2).
   */
  private final double _notional;
  /**
   * The reference exchange rate for the settlement (1 currency2 = _rate * currency1).
   */
  private final double _exchangeRate;
  /**
   * The exchange rate fixing date.
   */
  private final ZonedDateTime _fixingDate;
  /**
   * The transaction payment or settlement date.
   */
  private final ZonedDateTime _paymentDate;

  /**
   * Constructor.
   * @param currency1 First currency of the transaction.
   * @param currency2 Second currency of the transaction. The cash settlement is done in this currency.
   * @param notional Notional of the transaction (in currency2).
   * @param exchangeRate The reference exchange rate for the settlement (1 currency2 = _rate * currency1).
   * @param fixingDate The exchange rate fixing date.
   * @param paymentDate The transaction payment or settlement date.
   */
  public ForexNonDeliverableForwardDefinition(final Currency currency1, final Currency currency2, final double notional, final double exchangeRate,
      final ZonedDateTime fixingDate, final ZonedDateTime paymentDate) {
    ArgumentChecker.notNull(currency1, "First currency");
    ArgumentChecker.notNull(currency2, "Second currency");
    ArgumentChecker.notNull(fixingDate, "Fixing date");
    ArgumentChecker.notNull(paymentDate, "Payment date");
    ArgumentChecker.isTrue(!paymentDate.isBefore(fixingDate), "Payment date should be on or after fixing date");
    _currency1 = currency1;
    _currency2 = currency2;
    _notional = notional;
    _exchangeRate = exchangeRate;
    _fixingDate = fixingDate;
    _paymentDate = paymentDate;
  }

  /**
   * Gets the first currency of the transaction.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _currency1;
  }

  /**
   * Gets the second currency of the transaction. The cash settlement is done in this currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _currency2;
  }

  /**
   * Gets the notional of the transaction (in currency2).
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the reference exchange rate for the settlement.
   * @return The rate.
   */
  public double getExchangeRate() {
    return _exchangeRate;
  }

  /**
   * Gets The exchange rate fixing date.
   * @return The date.
   */
  public ZonedDateTime getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets The transaction payment (or settlement) date.
   * @return The date.
   */
  public ZonedDateTime getPaymentDate() {
    return _paymentDate;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public ForexNonDeliverableForward toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public ForexNonDeliverableForward toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!date.isAfter(_fixingDate), "Date is after fixing date");
    return new ForexNonDeliverableForward(_currency1, _currency2, _notional, _exchangeRate, TimeCalculator.getTimeBetween(date, _fixingDate),
        TimeCalculator.getTimeBetween(date, _paymentDate));
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexNonDeliverableForwardDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexNonDeliverableForwardDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency1.hashCode();
    result = prime * result + _currency2.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_exchangeRate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingDate.hashCode();
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _paymentDate.hashCode();
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
    final ForexNonDeliverableForwardDefinition other = (ForexNonDeliverableForwardDefinition) obj;
    if (!ObjectUtils.equals(_currency1, other._currency1)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency2, other._currency2)) {
      return false;
    }
    if (Double.doubleToLongBits(_exchangeRate) != Double.doubleToLongBits(other._exchangeRate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingDate, other._fixingDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_paymentDate, other._paymentDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return true;
  }

}
