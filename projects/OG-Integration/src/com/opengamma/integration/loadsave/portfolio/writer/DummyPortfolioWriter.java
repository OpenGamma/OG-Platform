/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.integration.loadsave.portfolio.writer;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A dummy portfolio writer, which pretty-prints information instead of persisting
 * TODO implement portfolio tree methods
 */
public class DummyPortfolioWriter implements PortfolioWriter {

  private ManageablePortfolioNode _node = new ManageablePortfolioNode();
  
  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    System.out.println("Security: " + security.toString());
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    System.out.println("Position: " + position.toString());
    return position;
  }

  @Override
  public ManageablePortfolio getPortfolio() {
    return null;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return _node;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    System.out.println("Set node to: " + node.toString());
    _node = node;
    return _node;
  }

  @Override
  public void flush() {
    System.out.println("Flushed writer");
  }

  @Override
  public void close() {
    System.out.println("Closed writer");
  }
  
}
