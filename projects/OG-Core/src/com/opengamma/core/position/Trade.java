/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * A single trade against a particular counterparty.
 * <p>
 * A trade is fundamentally a quantity of a security.
 * It differs from a position in that it represents a real transaction with a
 * counterparty at an instant in time.
 */
@PublicSPI
public interface Trade extends PositionOrTrade {
  
  /**
   * Gets the unique identifier of the position within the portfolio this trade belongs.
   * 
   * @return the unique identifier
   */
  UniqueIdentifier getPositionId();
  
  /**
   * Gets the counterparty associated with the trade.
   * 
   * @return the counterparty
   */
  Counterparty getCounterparty();

  /**
   * Gets the trade date.
   * 
   * @return the trade date, not  null
   */
  LocalDate getTradeDate();
  
  /**
   * Gets the trade time with offset
   * 
   * @return the trade time with offset if available
   */
  OffsetTime getTradeTime();

}
