/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.reader;

import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;

/**
 * Abstract portfolio loader class that merely specifies the ability to write imported trades/positions to a PortfolioWriter
 * (This tight linkage between reader and writer might have to change)
 */
public abstract interface PortfolioReader {

  /**
   * Write the multisheet contents to the specified portfolio
   * @param portfolioWriter The portfolio writer to use for writing
   */
  void writeTo(PortfolioWriter portfolioWriter);
  
}
