/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of portfolios and positions/trades as accessed by the engine.
 * <p>
 * This interface provides a simple view of portfolios and positions as needed by the engine.
 * This may be backed by a full-featured position master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface PositionSource extends ChangeProvider {

  /**
   * Gets a portfolio by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single portfolio at a single version-correction.
   * 
   * @param uniqueId the unique identifier, not null
   * @param versionCorrection the version/correction to use for deep resolution of the portfolio structure, not null
   * @return the portfolio, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the portfolio cannot be found
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(UniqueId uniqueId, VersionCorrection versionCorrection);

  // REVIEW 2012-10-19 Andrew -- If PortfolioNode had a PositionLink members (like Position does to security) then we wouldn't need the v/c parameter to getPortfolio(UID)

  /**
   * Gets a portfolio by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single portfolio at a single version-correction.
   * 
   * @param objectId  the object identifier, not null
   * @param versionCorrection  the version-correction, not null
   * @return the portfolio, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the portfolio cannot be found
   * @throws RuntimeException if an error occurs
   */
  Portfolio getPortfolio(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a node by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single node at a single version-correction.
   * 
   * @param uniqueId the unique identifier, not null
   * @param versionCorrection the version/correction to use for deep resolution of the portfolio structure, not null
   * @return the node, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the node cannot be found
   * @throws RuntimeException if an error occurs
   */
  PortfolioNode getPortfolioNode(UniqueId uniqueId, VersionCorrection versionCorrection);

  // REVIEW 2012-10-19 Andrew -- If PortfolioNode had a PositionLink members (like Position does to security) then we wouldn't need the v/c parameter to getPortfolioNode(UID)

  /**
   * Gets a position by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single position at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the position, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the position cannot be found
   * @throws RuntimeException if an error occurs
   */
  Position getPosition(UniqueId uniqueId);

  /**
   * Gets a position by its object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify a single position at a single version-correction that can then be referenced by its unique identifier.
   * 
   * @param objectId the object identifier, not null
   * @param versionCorrection the version-correction, not null
   * @return the position, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the position cannot be found
   * @throws RuntimeException if an error occurs
   */
  Position getPosition(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a trade by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single trade at a single version-correction.
   * 
   * @param uniqueId  the unique identifier, not null
   * @return the trade, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the trade cannot be found
   * @throws RuntimeException if an error occurs
   */
  Trade getTrade(UniqueId uniqueId);

}
