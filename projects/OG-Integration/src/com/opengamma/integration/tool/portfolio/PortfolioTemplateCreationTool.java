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
import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.writer.PortfolioWriter;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetSimplePortfolioWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;

/**
 * The portfolio saver tool
 */
public class PortfolioTemplateCreationTool extends AbstractTool {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioTemplateCreationTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {     

    // Create portfolio writer
    PortfolioWriter portfolioWriter = constructPortfolioWriter(
        getCommandLine().getOptionValue(FILE_NAME_OPT), 
        getCommandLine().getOptionValue(SECURITY_TYPE_OPT)
    );
    
    portfolioWriter.close();
  }
  
  private static PortfolioWriter constructPortfolioWriter(String filename, String securityType) {
    // Check that the file name was specified on the command line
    if (filename == null) {
      throw new OpenGammaRuntimeException("File name omitted, cannot create file");
    }
     
    if (SheetFormat.of(filename) == SheetFormat.CSV || SheetFormat.of(filename) == SheetFormat.XLS) {
      return new SingleSheetSimplePortfolioWriter(filename, JodaBeanRowParser.newJodaBeanRowParser(securityType));
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .XLS");
    }
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);

    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file to create and export to (CSV or XLS)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "securitytype", true, 
        "The security type for which to generate a template");
    options.addOption(assetClassOption);
    
    return options;
  }

}
