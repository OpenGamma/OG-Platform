/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioNodeTraversalCallback;
import com.opengamma.core.position.impl.PortfolioNodeTraverser;
import com.opengamma.integration.copier.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioInfoTool extends AbstractTool<IntegrationToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioInfoTool.class);

  private static final String PORTFOLIO_NAME = "n";

  /**
   * Runs the tool.
   *
   * @param args  empty arguments
   */
  public static void main(String[] args) {  // CSIGNORE
    new PortfolioInfoTool().initAndRun(args, IntegrationToolContext.class);
  }

  @Override
  protected void doRun() {
    PortfolioReader portfolioReader = new MasterPortfolioReader(getCommandLine().getOptionValue(PORTFOLIO_NAME),
                                                                getToolContext().getPortfolioMaster(),
                                                                getToolContext().getPositionMaster(),
                                                                getToolContext().getSecuritySource());

    int positionCount = 0;
    while (portfolioReader.readNext() != null) {
      positionCount++;
    }
    System.out.println("Number of positions in " + getCommandLine().getOptionValue(PORTFOLIO_NAME) + ": " + positionCount);
  }

  protected Options createOptions(boolean contextProvided) {
    Options options = super.createOptions(contextProvided);

    Option origNameOption = new Option(
        PORTFOLIO_NAME, "origname", true, "The name of the OpenGamma portfolio to copy or rename");
    origNameOption.setRequired(true);
    options.addOption(origNameOption);

    return options;
  }
}
