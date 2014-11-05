/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.definition;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Each time a view is recalculated, the security definition
 * creates an analytic derivative for the current time.
 */
public class EquityIndexDividendFutureDefinition extends EquityFutureDefinition {

  /**
   * @param expiryDate The expiry date
   * @param settlementDate The settlement date
   * @param strikePrice The strike price
   * @param currency The currency
   * @param unitValue The unit value
   */
  public EquityIndexDividendFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double strikePrice, final Currency currency, final double unitValue) {
    super(expiryDate, settlementDate, strikePrice, currency, unitValue);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexDividendFutureDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexDividendFutureDefinition(this);
  }

}
