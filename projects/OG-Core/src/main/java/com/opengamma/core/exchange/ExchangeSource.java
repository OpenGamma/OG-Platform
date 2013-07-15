/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import com.opengamma.core.SourceWithExternalBundle;
import com.opengamma.id.ExternalId;
import com.opengamma.util.PublicSPI;

/**
 * A source of exchanges as accessed by the main application.
 * <p>
 * This interface provides a simple view of exchanges as used by most parts of the application. This may be backed by a full-featured exchange master, or by a much simpler data structure.
 * <p>
 * This interface is read-only. Implementations must be thread-safe.
 */
@PublicSPI
public interface ExchangeSource extends SourceWithExternalBundle<Exchange> {

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Finds a specific exchange by identifier.
   * <p>
   * This should only be used when you know there is a single result. For example, a search by MIC should return one result.
   * 
   * @param identifier the identifier, null returns null
   * @return the exchange, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Exchange getSingle(ExternalId identifier);

}
