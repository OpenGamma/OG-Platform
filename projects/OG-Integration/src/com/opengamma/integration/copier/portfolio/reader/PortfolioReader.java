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
 * Abstract portfolio loader class that merely specifies the ability to write imported trades/positions to a PortfolioWriter
 * (This tight linkage between reader and writer might have to change)
 */
public abstract interface PortfolioReader {

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

}
