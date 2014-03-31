/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.position.Portfolio;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.integration.copier.portfolio.reader.ZippedPositionReader;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.tool.portfolio.xml.SchemaRegister;
import com.opengamma.integration.tool.portfolio.xml.XmlFileReader;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.tuple.Pair;

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
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new PortfolioLoaderTool().invokeAndTerminate(args);
  }

  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {

    String suggestedPortfolioName = getOptionValue(PORTFOLIO_NAME_OPT);
    boolean write = getCommandLine().hasOption(WRITE_OPT);

    if (write) {
      System.out.println("Write option specified, will persist to portfolio '" + suggestedPortfolioName + "'");
    } else {
      System.out.println("Write option not specified, not persisting to OpenGamma masters");
    }

    PortfolioWriter persister = new PortfolioWriter(write,
                                                          getToolContext().getPortfolioMaster(),
                                                          getToolContext().getPositionMaster(),
                                                          getToolContext().getSecurityMaster());


    final String filename = getOptionValue(FILE_NAME_OPT);
    final String securityType = getOptionValue(SECURITY_TYPE_OPT);

    for (PositionReader positionReader : constructPortfolioReaders(filename, securityType)) {

      String name = positionReader.getPortfolioName();
      String portfolioName = name != null ? name : suggestedPortfolioName;

      PortfolioReader portfolioReader = new PortfolioReader(positionReader, portfolioName);

      Pair<Portfolio, Set<ManageableSecurity>> pair = portfolioReader.createPortfolio();
      persister.write(pair.getFirst(), pair.getSecond());
    }
  }

  private String getOptionValue(String opt) {
    return getCommandLine().getOptionValue(opt);
  }

  private Iterable<? extends PositionReader> constructPortfolioReaders(String filename, String securityType) {

    switch (SheetFormat.of(filename)) {

      case CSV:
        // Fallthrough to XLS processing
      case XLS:
        // Check that the asset class was specified on the command line
        if (securityType == null) {
          throw new OpenGammaRuntimeException("Could not import as no asset class was specified for file " + filename);
        } else {
//          if (securityType.equalsIgnoreCase("exchangetraded")) {
//            return new SingleSheetSimplePositionReader(filename, new ExchangeTradedRowParser(s_context.getBloombergSecuritySource()));
//          } else {
          return ImmutableList.of(new SingleSheetSimplePositionReader(filename, securityType));
//          }
        }
      case XML:
        // XMl multi-asset portfolio
        try {
          return new XmlFileReader(new FileInputStream(filename), new SchemaRegister());
        } catch (FileNotFoundException e) {
          throw new OpenGammaRuntimeException("Cannot find file: " + filename, e);
        }

      case ZIP:
        // Create zipped multi-asset class loader
        return ImmutableList.of(new ZippedPositionReader(filename, true));

      default:
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS, .XML or .ZIP");
    }
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

    return options;
  }
}
