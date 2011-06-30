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
   * Gets the payment amount for the trade.
   * 
   * @return the premium amount, can be null
   */
  Double getPremium();

  /**
   * Gets the payment currency for the trade.
   * 
   * @return the premium currency, can be null
   */
  Currency getPremiumCurrency();

  /**
   * Gets the payment date for the trade.
   * This may be different from the trade date.
   * 
   * @return the premium date, can be null
   */
  LocalDate getPremiumDate();

  /**
   * Gets the payment time for the trade.
   * This may be different from the trade time.
   * <p>
   * The offset time and local date can be combined to find the instant of when premium was paid.
   * 
   * @return the premium time with offset, can be null
   */
  OffsetTime getPremiumTime();

  /**
   * Gets the attributes to use for trade aggregation.
   * 
   * @return the attributes, not null
   */
  Map<String, String> getAttributes();

}
