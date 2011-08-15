/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import javax.time.calendar.TimeZone;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * An exchange on which financial products can be traded or settled.
 * <p>
 * Financial products are often traded at a specific location known as an exchange.
 * This interface represents details of the exchange.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface Exchange extends UniqueIdentifiable {

  /**
   * Gets the unique identifier of the exchange.
   * <p>
   * This specifies a single version-correction of the exchange.
   * 
   * @return the unique identifier for this exchange, not null
   */
  UniqueId getUniqueId();

  /**
   * Gets the external identifier bundle defining the exchange.
   * <p>
   * Each external system has one or more identifiers by which they refer to the exchange.
   * Some of these may be unique within that system, while others may be more descriptive.
   * This bundle stores the set of these external identifiers.
   * 
   * @return the bundle, null if not applicable
   */
  ExternalIdBundle getExternalIdBundle();

  /**
   * Gets the region external identifier bundle that defines where the exchange is located.
   * 
   * @return the region key identifier bundle of the exchange, null if no location
   */
  ExternalIdBundle getRegionIdBundle();

  /**
   * Gets the time-zone of the exchange.
   * 
   * @return the time-zone of the exchange, null if time-zone unknown
   */
  TimeZone getTimeZone();

  /**
   * Gets the name of the exchange intended for display purposes.
   * 
   * @return the name of the exchange, not null
   */
  String getName();

}
