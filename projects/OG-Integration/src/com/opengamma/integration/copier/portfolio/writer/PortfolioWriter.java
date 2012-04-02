/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.copier.portfolio.writer;

import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * Interface for a portfolio writer, which is able to write positions and securities, and manipulate the portfolio's
 * tree structure.
 */
public interface PortfolioWriter {

  ManageableSecurity writeSecurity(ManageableSecurity security);
  
  ManageablePosition writePosition(ManageablePosition position);
    
  /**
   * Get the current portfolio path.
   * @return  the current node
   */
  String[] getCurrentPath();
  
  /**
   * Set the portfolio path, only makes sense in hierarchical portfolio writers
   * @param newPath the new path represented as an array of node names starting from the top of the hierarchy
   */
  void setPath(String[] newPath);
  
  void flush();
  
  void close();
}
