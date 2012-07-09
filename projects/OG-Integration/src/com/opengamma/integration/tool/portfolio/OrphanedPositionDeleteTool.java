/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.factory.tool.RemoteComponentFactoryToolContextAdapter;
import com.opengamma.component.tool.AbstractComponentTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.generate.scripts.Scriptable;

/**
 * Tool to delete positions that are not currently in a portfolio
 */
@Scriptable
public class OrphanedPositionDeleteTool extends AbstractComponentTool {
  
  private static final Logger s_logger = LoggerFactory.getLogger(OrphanedPositionDeleteTool.class);
  /**
   * Main method to run the tool.
   */
  public static void main(String[] args) { // CSIGNORE
    new OrphanedPositionDeleteTool().initAndRun(args);
    System.exit(0);
  }

  @Override
  protected void doRun() throws Exception {
    ToolContext toolContext = new RemoteComponentFactoryToolContextAdapter(getRemoteComponentFactory());
    OrphanedPositionRemover orphanedPositionRemover = new OrphanedPositionRemover(toolContext.getPortfolioMaster(), toolContext.getPositionMaster());
    s_logger.info("running orphanedPositionRemover");
    orphanedPositionRemover.run();
  }
  
}
