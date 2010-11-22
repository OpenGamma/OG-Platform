/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import javax.time.Instant;

import com.opengamma.util.PublicSPI;

/**
 * A position of a single trade against a particular {@code Counterparty}.
 */
@PublicSPI
public interface Trade extends TradeOrPosition {

  /**
   * Returns the {@link Counterparty} associated with the trade.
   * 
   * @return the {@code Counterprty}
   */
  Counterparty getCounterparty();
  
  /**
   * Returns the instant the trade happened
   * 
   * @return the instant
   */
  Instant getTradeInstant();
  
}
