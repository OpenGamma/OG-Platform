/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;

/**
 * Abstract class for generic futures securities.
 */
public abstract class FuturesSecurityDefinition  implements InstrumentDefinition<FuturesSecurity> {

  /**
   * The last trading date.
   */
  private final ZonedDateTime _tradingLastDate;

  /**
   * Constructor. 
   * @param tradingLastDate The last trading date of the futures.
   */
  public FuturesSecurityDefinition(ZonedDateTime tradingLastDate) {
    super();
    _tradingLastDate = tradingLastDate;
  }

  /**
   * Returns the last trading date of the futures. 
   * @return The date.
   */
  public ZonedDateTime getTradingLastDate() {
    return _tradingLastDate;
  }

}
