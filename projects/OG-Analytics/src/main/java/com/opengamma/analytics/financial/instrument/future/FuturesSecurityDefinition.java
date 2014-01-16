/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for generic futures securities.
 * @param <F> A futures security type.
 */
public abstract class FuturesSecurityDefinition<F extends FuturesSecurity> implements InstrumentDefinition<F> {

  /**
   * The last trading date. Not null.
   */
  private final ZonedDateTime _lastTradingDate;

  /**
   * Constructor. 
   * @param lastTradingDate The last trading date of the futures.
   */
  public FuturesSecurityDefinition(ZonedDateTime lastTradingDate) {
    ArgumentChecker.notNull(lastTradingDate, "last trading date");
    _lastTradingDate = lastTradingDate;
  }

  /**
   * Returns the last trading date of the futures. 
   * @return The date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _lastTradingDate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _lastTradingDate.hashCode();
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
    @SuppressWarnings("unchecked")
    FuturesSecurityDefinition<F> other = (FuturesSecurityDefinition<F>) obj;
    if (!ObjectUtils.equals(_lastTradingDate, other._lastTradingDate)) {
      return false;
    }
    return true;
  }

}
