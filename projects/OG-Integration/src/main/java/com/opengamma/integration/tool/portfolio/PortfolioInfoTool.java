/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.copier.portfolio.reader.MasterPositionReader;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioInfoTool extends AbstractTool<IntegrationToolContext> {

  private static final String PORTFOLIO_NAME = "n";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new PortfolioInfoTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    PositionReader positionReader = new MasterPositionReader(getCommandLine().getOptionValue(PORTFOLIO_NAME),
                                                                getToolContext().getPortfolioMaster(),
                                                                getToolContext().getPositionMaster(),
                                                                getToolContext().getSecuritySource());

    int positionCount = 0;
    while (positionReader.readNext() != null) {
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
