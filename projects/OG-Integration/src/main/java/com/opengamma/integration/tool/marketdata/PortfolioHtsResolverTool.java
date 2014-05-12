/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.ResolvingPortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.MasterPositionReader;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPositionWriter;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * The portfolio loader tool
 */
@Scriptable
public class PortfolioHtsResolverTool extends AbstractTool<IntegrationToolContext> {

  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioHtsResolverTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {
    IntegrationToolContext context = getToolContext();

    // Create portfolio writer
    PositionWriter positionWriter = new PrettyPrintingPositionWriter(getCommandLine().hasOption(VERBOSE_OPT));

    // Construct portfolio reader
    PositionReader positionReader = new MasterPositionReader(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT), 
        context.getPortfolioMaster(), 
        context.getPositionMaster(), 
        context.getSecuritySource());
    
    // Create portfolio copier
    ResolvingPortfolioCopier portfolioCopier = new ResolvingPortfolioCopier(
        context.getHistoricalTimeSeriesMaster(),
        context.getHistoricalTimeSeriesProvider(),
        context.getBloombergReferenceDataProvider(),
        getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, "CMPL"),
        getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT) == null ? 
            new String[]{"PX_LAST"} : getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT)
    );
    
    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor; 
    if (getCommandLine().hasOption(VERBOSE_OPT)) {
      portfolioCopierVisitor = new VerbosePortfolioCopierVisitor();
    } else {
      portfolioCopierVisitor = new QuietPortfolioCopierVisitor();
    }
    
    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(positionReader, positionWriter, portfolioCopierVisitor);
    
    // close stuff
    positionReader.close();
    positionWriter.close();
  }

  private String getOptionValue(String optionName, String defaultValue) {
    return getCommandLine().getOptionValue(optionName) == null ? defaultValue : getCommandLine().getOptionValue(optionName);
  }

  
  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the OpenGamma portfolio for which to resolve time series");
    portfolioNameOption.setRequired(true);
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
 
    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);
   
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name(s) of the time series data field(s)");
    options.addOption(timeSeriesDataFieldOption);
    
    return options;
  }


}
