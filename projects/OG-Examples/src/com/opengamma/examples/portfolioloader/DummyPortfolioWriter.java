/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.examples.portfolioloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.security.ManageableSecurity;

/**
 * A dummy portfolio writer, which pretty-prints information instead of persisting
 * TODO implement portfolio tree methods
 */
public class DummyPortfolioWriter implements PortfolioWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(CommandLineTool.class);

  @Override
  public ManageableSecurity writeSecurity(ManageableSecurity security) {
    s_logger.info("Security: " + security.toString());
    return security;
  }

  @Override
  public ManageablePosition writePosition(ManageablePosition position) {
    s_logger.info("Position: " + position.toString());
    return position;
  }

  @Override
  public ManageablePortfolio getPortfolio() {
    return null;
  }

  @Override
  public ManageablePortfolioNode getCurrentNode() {
    return null;
  }

  @Override
  public ManageablePortfolioNode setCurrentNode(ManageablePortfolioNode node) {
    s_logger.info("Set node to: " + node.toString());
    return null;
  }

  @Override
  public void flush() {
    s_logger.info("Flushed writer");
  }

}
