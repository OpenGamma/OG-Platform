/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange;

import javax.time.calendar.TimeZone;

import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * An exchange on which financial products can be traded or settled.
 * <p>
 * Financial products are often traded at a specific location known as an exchange.
 * This interface represents details of the exchange.
 */
public interface Exchange {

  /**
   * The unique identifier of the exchange.
   * 
   * @return the unique identifier for this exchange entry, not null
   */
  UniqueIdentifier getUniqueIdentifier();

  /**
   * Gets the bundle of identifiers that define the exchange.
   * 
   * @return the bundle of identifiers, not null
   */
  IdentifierBundle getIdentifiers();

  /**
   * Gets the name of the exchange intended for display purposes.
   * 
   * @return the name of the exchange, not null
   */
  String getName();

  /**
   * Gets the bundle of identifiers that define where the exchange is located.
   * 
   * @return the region identifier bundle of the exchange, null if no location
   */
  IdentifierBundle getRegionId();

  /**
   * Gets the time-zone of the exchange.
   * 
   * @return the time-zone of the exchange, null if time-zone unknown
   */
  TimeZone getTimeZone();

}
