/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicAPI;

/**
 * Resolver capable of providing portfolios.
 * <p>
 * This resolver provides lookup of a {@link Portfolio portfolio} to the engine functions.
 * The lookup may require selecting a single "best match" from a set of potential options.
 * The best match behavior is the key part that distinguishes one implementation from another.
 * Best match selection may use a version-correction, configuration or code as appropriate.
 * Implementations of this interface must specify the rules they use to best match.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicAPI
public interface PortfolioResolver {

  /**
   * Gets a portfolio by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single portfolio at a single version-correction.
   * As such, there should be no complex matching issues in this lookup.
   * However, if the underlying data store does not handle versioning correctly,
   * then a best match selection may be required.
   * 
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched portfolio, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the portfolio could not be found
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(UniqueId uniqueId);

  /**
   * Gets a portfolio by object identifier.
   * <p>
   * An object identifier exactly specifies a single portfolio, but it provide no information
   * about the version-correction required.
   * As such, it is likely that multiple versions/corrections will match the object identifier.
   * The resolver implementation is responsible for selecting the best match.
   * 
   * @param objectId  the object identifier to find, not null
   * @return the matched portfolio, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the portfolio could not be found
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(ObjectId objectId);

}
