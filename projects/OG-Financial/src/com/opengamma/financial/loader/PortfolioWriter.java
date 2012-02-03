/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.financial.loader;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Interface for a portfolio writer, which is able to write positions and securities, and manipulate the portfolio's
 * tree structure.
 */
public interface PortfolioWriter {

  ManageableSecurity writeSecurity(ManageableSecurity security);
  
  ManageablePosition writePosition(ManageablePosition position);
  
  ManageablePortfolio getPortfolio();
  
  ManageablePortfolioNode getCurrentNode();
  
  ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node);
  
  void flush();
  
}
