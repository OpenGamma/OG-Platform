/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.scripts.Scriptable;

/**
 * The portfolio loader tool
 */
@Scriptable
public class PortfolioLoaderTool extends AbstractTool<ToolContext> {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";
  /** Try to merge positions in the same security type within a portfolio node (sum quantities and add trades from both) */
  private static final String MERGE_POSITIONS_OPT = "m";
  /** Keep existing positions in the previous version of the portfolio and add the newly loaded positions */
  private static final String KEEP_CURRENT_POSITIONS_OPT = "k";
  /** Ignore versioning flag */
  private static final String IGNORE_VERSION_OPT = "i";
  /** Structure by attributes option */
  private static final String STRUCTURE_OPT = "t";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioLoaderTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {

    String portfolioName = getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT);
    boolean write = getCommandLine().hasOption(WRITE_OPT);

    if (write) {
      System.out.println("Write option specified, will persist to portfolio '" + portfolioName + "'");
    } else {
      System.out.println("Write option not specified, not persisting to OpenGamma masters");
    }

    new PortfolioLoader(getToolContext(), portfolioName,
                        getCommandLine().getOptionValue(SECURITY_TYPE_OPT),
                        getCommandLine().getOptionValue(FILE_NAME_OPT),
                        write,
                        getCommandLine().hasOption(VERBOSE_OPT),
                        getCommandLine().hasOption(MERGE_POSITIONS_OPT),
                        getCommandLine().hasOption(KEEP_CURRENT_POSITIONS_OPT),
                        getCommandLine().hasOption(IGNORE_VERSION_OPT),
                        true,
                        getCommandLine().getOptionValues(STRUCTURE_OPT)).execute();
  }


  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV, XLS, XML or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
    
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "security", true, 
        "The security type expected in the input CSV/XLS file (ignored if ZIP file is specified)");
    options.addOption(assetClassOption);

    Option mergePositionsOption = new Option(MERGE_POSITIONS_OPT, "merge", false,
        "Try to merge positions in the same security type within a portfolio node, adding trades from all");
    options.addOption(mergePositionsOption);

    Option keepCurrentPositionsOption = new Option(KEEP_CURRENT_POSITIONS_OPT, "keep", false,
        "Keep existing positions in the previous version of the portfolio and add the newly loaded positions");
    options.addOption(keepCurrentPositionsOption);

    Option ignoreVersionOption = new Option(
        IGNORE_VERSION_OPT, "ignoreversion", false,
        "Ignore the versioning hashes in METADATA.INI when reading from a ZIP file");
    options.addOption(ignoreVersionOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    Option structureOption = new Option(
        STRUCTURE_OPT, "structure", true,
        "A /-separated sequence of position attributes used to structure the portfolio(s) (e.g. trade-group/strategy");
    options.addOption(structureOption);

    return options;
  }
}
