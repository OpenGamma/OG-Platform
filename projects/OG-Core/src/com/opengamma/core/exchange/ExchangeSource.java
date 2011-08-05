/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A source of exchanges as accessed by the main application.
 * <p>
 * This interface provides a simple view of exchanges as used by most parts of the application.
 * This may be backed by a full-featured exchange master, or by a much simpler data structure.
 */
@PublicSPI
public interface ExchangeSource {

  /**
   * Finds a specific exchange by unique identifier.
   * 
   * @param uniqueId  the unique identifier, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Exchange getExchange(UniqueId uniqueId);

  /**
   * Finds a specific exchange by identifier.
   * <p>
   * This should only be used when you know there is a single result.
   * For example, a search by MIC should return one result.
   * 
   * @param identifier  the identifier, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Exchange getSingleExchange(ExternalId identifier);

  /**
   * Finds a specific exchange by identifier bundle.
   * <p>
   * This should only be used when you know there is a single result.
   * For example, a search by MIC should return one result.
   * 
   * @param identifierBundle  the identifier bundle, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Exchange getSingleExchange(ExternalIdBundle identifierBundle);

}
