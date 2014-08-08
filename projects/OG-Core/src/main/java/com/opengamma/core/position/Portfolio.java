/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import com.opengamma.core.Attributable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.PublicSPI;

/**
 * A portfolio of positions, typically having business-level meaning.
 * <p>
 * A portfolio is the primary element of business-level grouping within the source of positions.
 * It consists of a number of positions which are grouped using a flexible tree structure.
 * <p>
 * A portfolio typically has meta-data.
 * <p>
 * This interface is read-only.
 * Implementations may be mutable.
 */
@PublicSPI
public interface Portfolio extends UniqueIdentifiable, Attributable {

  /**
   * Gets the unique identifier of the portfolio.
   * <p>
   * This specifies a single version-correction of the portfolio.
   * 
   * @return the unique identifier for this portfolio, not null within the engine
   */
  @Override
  UniqueId getUniqueId();

  /**
   * Gets the root node in the portfolio.
   * <p>
   * The positions stored in a portfolios are held in a tree structure.
   * This method accesses the root of the tree structure.
   * 
   * @return the root node of the tree structure, not null
   */
  PortfolioNode getRootNode();

  /**
   * Gets the name of the portfolio intended for display purposes.
   * 
   * @return the display name, not null
   */
  String getName();

}
