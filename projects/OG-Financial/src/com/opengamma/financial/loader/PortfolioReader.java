/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

/**
 * Abstract portfolio loader class that merely specifies the ability to write imported trades/positions to a PortfolioWriter
 */
public abstract interface PortfolioReader {

  /**
   * Write the multisheet contents to the specified portfolio
   * @param portfolioWriter The portfolio writer to use for writing
   */
  void writeTo(PortfolioWriter portfolioWriter);
  
}
