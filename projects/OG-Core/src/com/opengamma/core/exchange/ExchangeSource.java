/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of exchanges as accessed by the main application.
 * <p>
 * This interface provides a simple view of exchanges as used by most parts of the application.
 * This may be backed by a full-featured exchange master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface ExchangeSource {

  /**
   * Gets an exchange by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single exchange at a single version-correction.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched exchange, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the exchange could not be found
   * @throws RuntimeException if an error occurs
   */
  Exchange getExchange(UniqueId uniqueId);

  /**
   * Gets an exchange by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single exchange at a single version-correction.
   * 
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched exchange, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the exchange could not be found
   * @throws RuntimeException if an error occurs
   */
  Exchange getExchange(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets all exchanges at the given version-correction that match the specified
   * external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single exchange.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single exchange.
   * This method returns all exchanges that may match for {@link ExchangeResolver} to choose from.
   * 
   * @param bundle  the bundle keys to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return all exchanges matching the specified key, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<? extends Exchange> getExchanges(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Finds a specific exchange by identifier.
   * <p>
   * This should only be used when you know there is a single result.
   * For example, a search by MIC should return one result.
   * 
   * @param identifier  the identifier, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
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
   * @throws RuntimeException if an error occurs
   */
  Exchange getSingleExchange(ExternalIdBundle identifierBundle);

}
