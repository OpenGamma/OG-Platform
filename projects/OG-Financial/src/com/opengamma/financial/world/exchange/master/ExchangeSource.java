/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master;

import com.opengamma.financial.world.exchange.Exchange;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * A source of exchanges as accessed by the main application.
 * <p>
 * This interface provides a simple view of exchanges as used by most parts of the application.
 * This may be backed by a full-featured exchange master, or by a much simpler data structure.
 */
public interface ExchangeSource {

  /**
   * Finds a specific exchange by unique identifier.
   * 
   * @param uid  the unique identifier, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   */
  Exchange getExchange(UniqueIdentifier uid);

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
  Exchange getSingleExchange(Identifier identifier);

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
  Exchange getSingleExchange(IdentifierBundle identifierBundle);

}
