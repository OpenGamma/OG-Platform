/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

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
   * Gets the unique identifier of the parent position.
   * 
   * @return the unique identifier of the parent position, null if not attached to a position
   */
  UniqueIdentifier getParentPositionId();

  /**
   * Gets the counterparty associated with the trade.
   * 
   * @return the counterparty
   */
  Counterparty getCounterparty();

  /**
   * Gets the trade date.
   * 
   * @return the trade date, not null
   */
  LocalDate getTradeDate();

  /**
   * Gets the trade time with offset.
   * <p>
   * The offset time and local date can be combined to find the instant of the trade.
   * 
   * @return the trade time with offset, null if unknown
   */
  OffsetTime getTradeTime();
  
  /**
   * Gets the amount paid for trade at time of purchase.
   * 
   * @return the premium, can be null
   */
  Double getPremium();
  
  /**
   * Gets the currency of payment at time of purchase.
   * 
   * @return the premium currency, can be null
   */
  Currency getPremiumCurrency();
  
  /**
   * Gets the date of payment at time of purchase, possibly different from trade date.
   * 
   * @return the premium date, can be null
   */
  LocalDate getPremiumDate();
  
  /**
   * Gets the premuim time with offset.
   * <p>
   * The offset time and local date can be combined to find the instant of when premuim was paid.
   * @return the premium time with offset, can be null
   */
  OffsetTime getPremiumTime();
  
  /**
   * Gets the attributes to use for trade aggregation
   * 
   * @return the attributes, not null
   */
  Map<String, String> getAttributes();
}
