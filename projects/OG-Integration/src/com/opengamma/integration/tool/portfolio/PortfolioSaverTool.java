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
import com.opengamma.integration.copier.portfolio.PortfolioCopier;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.copier.portfolio.reader.PortfolioReader;
import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetSimplePortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.ZippedPortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;

/**
 * The portfolio saver tool
 */
public class PortfolioSaverTool extends AbstractTool {

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

  private static ToolContext s_context;
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioSaverTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {     
    s_context = getToolContext();

    // Construct portfolio reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT)
    );

    // Create portfolio writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValue(SECURITY_TYPE_OPT),
        getCommandLine().hasOption(WRITE_OPT)
    );
    
    // Construct portfolio copier
    PortfolioCopier portfolioCopier = new SimplePortfolioCopier();
        
    // Create visitor for verbose/quiet mode
    PortfolioCopierVisitor portfolioCopierVisitor; 
    if (getCommandLine().hasOption(VERBOSE_OPT)) {
      portfolioCopierVisitor = new VerbosePortfolioCopierVisitor();
    } else {
      portfolioCopierVisitor = new QuietPortfolioCopierVisitor();
    }
    
    // Call the portfolio loader with the supplied arguments
    portfolioCopier.copy(portfolioReader, portfolioWriter, portfolioCopierVisitor);

    // close stuff
    portfolioReader.close();
    portfolioWriter.close();
  }
  
  private static PortfolioWriter constructPortfolioWriter(String filename, String securityType, boolean write) {
    if (write) {  
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }
       
      if (SheetFormat.of(filename) == SheetFormat.CSV || SheetFormat.of(filename) == SheetFormat.XLS) {
//        if (securityType.equalsIgnoreCase("exchangetraded")) {
//          return new SingleSheetSimplePortfolioWriter(filename, new ExchangeTradedRowParser(s_context.getBloombergSecuritySource()));
//        } else {
        return new SingleSheetSimplePortfolioWriter(filename, JodaBeanRowParser.newJodaBeanRowParser(securityType));
//        }
      } else if (SheetFormat.of(filename) == SheetFormat.ZIP) {
        return new ZippedPortfolioWriter(filename);
      } else {
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
      }

    } else {      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }
  }

  private static PortfolioReader constructPortfolioReader(String portfolioName) {
    return new MasterPortfolioReader(
        portfolioName, s_context.getPortfolioMaster(), 
        s_context.getPositionMaster(), 
        s_context.getSecuritySource()
    );
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);

    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file to create and export to (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the source OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the file if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
       
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "securitytype", true, 
        "The security type to export (ignored if ZIP output file is specified)");
    options.addOption(assetClassOption);
    
    Option verboseOption = new Option(
        VERBOSE_OPT, "verbose", false, 
        "Displays progress messages on the terminal");
    options.addOption(verboseOption);

    return options;
  }

}
