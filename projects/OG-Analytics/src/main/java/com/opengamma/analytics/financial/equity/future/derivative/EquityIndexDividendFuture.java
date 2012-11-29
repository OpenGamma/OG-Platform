/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.derivative;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A cash-settled futures contract on the index of the *dividends* of a given stock market index on the _timeToFixing
 */
public class EquityIndexDividendFuture extends EquityFuture {

  public EquityIndexDividendFuture(final double timeToFixing, final double timeToDelivery, final double strike, final Currency currency, final double unitValue) {
    super(timeToFixing, timeToDelivery, strike, currency, unitValue);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexDividendFuture(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexDividendFuture(this);
  }

}
