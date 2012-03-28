/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.integration.loadsave.portfolio.PortfolioCopier;
import com.opengamma.integration.loadsave.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.loadsave.portfolio.reader.MasterPortfolioReader;
import com.opengamma.integration.loadsave.portfolio.reader.PortfolioReader;
import com.opengamma.integration.loadsave.portfolio.writer.DummyPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.SingleSheetPortfolioWriter;
import com.opengamma.integration.loadsave.portfolio.writer.ZippedPortfolioWriter;
import com.opengamma.integration.tool.AbstractIntegrationTool;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;

/**
 * The portfolio saver tool
 */
public class PortfolioSaverTool extends AbstractIntegrationTool {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";

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
    IntegrationToolContext context = getToolContext();

    // Create portfolio writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValues(SECURITY_TYPE_OPT),
        getCommandLine().hasOption(WRITE_OPT)
    );
    
    // Construct portfolio reader
    PortfolioReader portfolioReader = constructPortfolioReader(
        getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT),
        context.getPortfolioMaster(), 
        context.getPositionMaster(), 
        context.getSecuritySource()
    );

    // Construct portfolio copier
    PortfolioCopier portfolioCopier = new SimplePortfolioCopier();
        
    // Copy portfolio
    portfolioCopier.copy(portfolioReader, portfolioWriter);

    // close stuff
    portfolioReader.close();
    portfolioWriter.close();
  }
  
  private static PortfolioWriter constructPortfolioWriter(String filename, String[] securityTypes, boolean write) {
    if (write) {  
      // Check that the file name was specified on the command line
      if (filename == null) {
        throw new OpenGammaRuntimeException("File name omitted, cannot export to file");
      }
       
      String extension = filename.substring(filename.lastIndexOf('.'));

      if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
        
        return new SingleSheetPortfolioWriter(filename, securityTypes);
            
      // Multi-asset ZIP file extension
      } else if (extension.equalsIgnoreCase(".zip")) {
        // Create zipped multi-asset class loader
        return new ZippedPortfolioWriter(filename);
      } else {
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS or .ZIP");
      }

    } else {      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyPortfolioWriter();
    }
  }

  private static PortfolioReader constructPortfolioReader(String portfolioName, PortfolioMaster portfolioMaster,
      PositionMaster positionMaster, SecuritySource securitySource) {
    return new MasterPortfolioReader(portfolioName, portfolioMaster, positionMaster, securitySource);
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
        "The security type(s) to export (ignored if ZIP output file is specified)");
    options.addOption(assetClassOption);
    
    return options;
  }

}
