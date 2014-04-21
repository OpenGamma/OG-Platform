/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.reader;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Abstract portfolio loader class that merely specifies the ability to write imported trades/positions to a PositionWriter
 * (This tight linkage between reader and writer might have to change)
 */
public interface PositionReader {

  /**
   * Read the next row as a position, possibly containing trades, and one or more securities
   * @return a pair containing the position and its securities
   */
  ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext();

  /**
   * Get the current portfolio path.
   * @return  the current node
   */
  String[] getCurrentPath();

  void close();

  /**
   * Read the name of the portfolio from the source. Only some readers have this capability,
   * those that don't will return a null value.
   *
   * @return the portfolio name if the reader supports it, null otherwise
   */
  String getPortfolioName();
}
