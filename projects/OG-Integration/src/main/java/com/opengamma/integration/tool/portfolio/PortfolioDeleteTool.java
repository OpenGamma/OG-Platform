/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.integration.copier.portfolio.DeletingPortfolioCopier;
import com.opengamma.integration.copier.portfolio.reader.MasterPositionReader;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPositionWriter;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;

/**
 * The portfolio loader tool
 */
//@Scriptable disabled because this tool basically doesn't work properly and leaves orphaned positions.
public class PortfolioDeleteTool extends AbstractTool<ToolContext> {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioDeleteTool.class);

  /** Delete portfolio names option flag */
  private static final String PORTFOLIO_NAMES_OPT = "n";
  /** Delete portfolio ids option flag */
  private static final String PORTFOLIO_IDS_OPT = "i";  
  /** Delete positions option flag */
  private static final String DELETE_POSITIONS_OPT = "dp";
  /** Delete securities option flag */
  private static final String DELETE_SECURITIES_OPT = "ds";
  /** Delete portfolios option flag */
  private static final String DELETE_PORTFOLIOS_OPT = "d";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioDeleteTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {    
    PortfolioSearchRequest portfolioSearchRequest = new PortfolioSearchRequest();
    
    if (getCommandLine().hasOption(PORTFOLIO_NAMES_OPT)) {
      portfolioSearchRequest.setName(
          getCommandLine().getOptionValue(PORTFOLIO_NAMES_OPT));
    } 
    
    if (getCommandLine().hasOption(PORTFOLIO_IDS_OPT)) {
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for (String s : getCommandLine().getOptionValues(PORTFOLIO_IDS_OPT)) {
        ids.add(ObjectId.parse(s));
      }
      portfolioSearchRequest.setPortfolioObjectIds(ids);
    }

    PortfolioSearchResult portSearchResult = getToolContext().getPortfolioMaster().search(portfolioSearchRequest);
      
    for (PortfolioDocument portfolioDocument : portSearchResult.getDocuments()) {

      DeletingPortfolioCopier deletingPortfolioCopier = 
          new DeletingPortfolioCopier(
              getToolContext().getSecurityMaster(), 
              getToolContext().getPositionMaster(),
              getCommandLine().hasOption(WRITE_OPT));
      
      deletingPortfolioCopier.copy(
          new MasterPositionReader(
              portfolioDocument.getPortfolio().getName(), 
              getToolContext().getPortfolioMaster(), 
              getToolContext().getPositionMaster(), 
              getToolContext().getSecuritySource()), 
          new PrettyPrintingPositionWriter(false),
          getCommandLine().hasOption(DELETE_POSITIONS_OPT),
          getCommandLine().hasOption(DELETE_SECURITIES_OPT));
      
      if (getCommandLine().hasOption(DELETE_PORTFOLIOS_OPT)) {
        if (getCommandLine().hasOption(WRITE_OPT)) {
          getToolContext().getPortfolioMaster().remove(portfolioDocument.getUniqueId());
          s_logger.warn("Deleted " + portfolioDocument.getPortfolio().getUniqueId() + 
              " (" + portfolioDocument.getPortfolio().getName() + ")");
        } else {
          s_logger.warn("Matched " + portfolioDocument.getPortfolio().getUniqueId() + 
              " (" + portfolioDocument.getPortfolio().getName() + ")");
        }
      }
    }
  }
  
  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option portfolioNamesOption = new Option(
        PORTFOLIO_NAMES_OPT, "name", true, "Regular expression to match portfolio names");    
//    options.addOption(portfolioNamesOption);
    
    Option deletePortfolioIdsOption = new Option(
        PORTFOLIO_IDS_OPT, "portfolioid", true, "Portfolio IDs to match");
//    options.addOption(deletePortfolioIdsOption);

    OptionGroup group = new OptionGroup();
    group.addOption(deletePortfolioIdsOption);
    group.addOption(portfolioNamesOption);
    group.setRequired(true);
    
    options.addOptionGroup(group);
    
    Option deletePositionsOption = new Option(
        DELETE_POSITIONS_OPT, "delpositions", false, "Match/delete positions referenced in matching portfolios");
    options.addOption(deletePositionsOption);
    
    Option deleteSecuritiesOption = new Option(
        DELETE_SECURITIES_OPT, "delsecurities", false, "Match/delete securities referenced in matching portfolios");
    options.addOption(deleteSecuritiesOption);
    
    Option deletePortfoliosOption = new Option(
        DELETE_PORTFOLIOS_OPT, "delportfolios", false, 
        "Actually delete matching portfolios");
    options.addOption(deletePortfoliosOption);

    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persist the deletions");
    options.addOption(writeOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
  }

}
