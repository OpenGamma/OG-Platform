/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.ZippedPortfolioReader;
import com.opengamma.integration.copier.portfolio.writer.MasterPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.util.generate.scripts.Scriptable;

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
  /** Overwrite option flag */
  private static final String OVERWRITE_OPT = "o";
  /** Verbose option flag */
  private static final String VERBOSE_OPT = "v";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";
  /** Ignore versioning flag */
  private static final String IGNORE_VERSION_OPT = "i";

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
    boolean overwrite = getCommandLine().hasOption(OVERWRITE_OPT);

    if (write) {
      if (overwrite) {
        System.out.println("Write and overwrite options specified, will persist to portfolio '" + portfolioName + "'");
      } else {
        System.out.println("Write option specified, will persist to portfolio '" + portfolioName + "'");
      }
    } else {
      System.out.println("Write option not specified, not persisting to OpenGamma masters");
    }

    new PortfolioLoader(getToolContext(), portfolioName,
                        getCommandLine().getOptionValue(SECURITY_TYPE_OPT),
                        getCommandLine().getOptionValue(FILE_NAME_OPT),
                        write, overwrite,
                        getCommandLine().hasOption(VERBOSE_OPT),
                        getCommandLine().hasOption(IGNORE_VERSION_OPT)).execute();
  }


  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV, XLS or ZIP)");
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
    
    Option overwriteOption = new Option(
        OVERWRITE_OPT, "overwrite", false, 
        "Deletes any existing matching securities, positions and portfolios and recreates them from input data");
    options.addOption(overwriteOption);

    Option ignoreVersionOption = new Option(
        IGNORE_VERSION_OPT, "ignoreversion", false,
        "Ignore the versioning hashes in METADATA.INI when reading from a ZIP file");
    options.addOption(ignoreVersionOption);

    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
  }
}
