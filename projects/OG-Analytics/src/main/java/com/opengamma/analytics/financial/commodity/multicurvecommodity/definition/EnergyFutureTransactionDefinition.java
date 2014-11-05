/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.definition;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExpiredException;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureSecurity;
import com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative.EnergyFutureTransaction;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EnergyFutureTransactionDefinition extends CommodityFutureTransactionDefinition<EnergyFutureTransaction> {

  public EnergyFutureTransactionDefinition(final CommodityFutureSecurityDefinition<?> underlying, final ZonedDateTime transactionDate, final double transactionPrice, final int quantity) {
    super(underlying, transactionDate, transactionPrice, quantity);
  }

  @Override
  public EnergyFutureTransaction toDerivative(final ZonedDateTime date, final Double data, final String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public EnergyFutureTransaction toDerivative(final ZonedDateTime date, final Double lastMarginPrice) {
    ArgumentChecker.notNull(date, "date");
    final LocalDate dateLocal = date.toLocalDate();
    final LocalDate transactionDateLocal = getTransactionDate().toLocalDate();
    final LocalDate lastTradingDateLocal = getLastTradingDate().toLocalDate();
    if (dateLocal.isAfter(lastTradingDateLocal)) {
      throw new ExpiredException("Valuation date, " + date + ", is after last margin date, " + lastTradingDateLocal);
    }
    double referencePrice;
    if (transactionDateLocal.isBefore(dateLocal)) { // Transaction was before last margining.
      referencePrice = lastMarginPrice;
    } else { // Transaction is today
      referencePrice = getTransactionPrice();
    }
    final EnergyFutureSecurity underlying = (EnergyFutureSecurity) getUnderlying().toDerivative(date);
    return new EnergyFutureTransaction(underlying, getQuantity(), referencePrice);
  }

  @Override
  public EnergyFutureTransaction toDerivative(final ZonedDateTime date) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() + " does not support the two argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureTransactionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureTransactionDefinition(this);
  }

  @Override
  public CommodityFutureTransactionDefinition<?> withNewTransactionPrice(final double transactionPrice) {
    return new EnergyFutureTransactionDefinition(getUnderlying(), getTransactionDate(), transactionPrice, getQuantity());
  }

}
