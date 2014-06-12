/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.portfolio.PortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.QuietPortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.SimplePortfolioCopier;
import com.opengamma.integration.copier.portfolio.VerbosePortfolioCopierVisitor;
import com.opengamma.integration.copier.portfolio.reader.PositionReader;
import com.opengamma.integration.copier.portfolio.reader.SingleSheetSimplePositionReader;
import com.opengamma.integration.copier.portfolio.reader.ZippedPositionReader;
import com.opengamma.integration.copier.portfolio.writer.MasterPositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.PrettyPrintingPositionWriter;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.tool.portfolio.xml.SchemaRegister;
import com.opengamma.integration.tool.portfolio.xml.XmlFileReader;
import com.opengamma.util.ArgumentChecker;

/**
 * Loads a portfolio from a file location. Primarily used by the {@link PortfolioLoaderTool} but split out to allow
 * for reuse in other contexts.
 */
public class PortfolioLoader {

  /**
   * Tool context for executing the load - must not be null.
   */
  private final ToolContext _toolContext;

  /**
   * The suggested name for the portfolio. Some portfolio readers will read the name from
   * the data in the file. If the reader does not do this, or no valid name is supplied,
   * then this suggested name will be used
   */
  private final String _suggestedPortfolioName;

  /**
   * The security type for the portfolio (if not a multi-asset portfolio).
   */
  private final String _securityType;

  /**
   * The filename to read the portfolio from - must not be null.
   */
  private final String _fileName;

  /**
   * Should the data actually be written to the masters.
   */
  private final boolean _write;

  /**
   * Should the output be verbose.
   */
  private final boolean _verbose;

  /**
   * Should the version hashes in the multi-asset zip file be ignored.
   */
  private final boolean _ignoreVersion;

  /**
   * Should positions in the same security and within the same portfolio node be merged into one.
   */
  private final boolean _mergePositions;

  /**
   * Should positions in the previous portfolio version be kept, otherwise start from scratch.
   */
  private final boolean _keepCurrentPositions;

  private final boolean _logToSystemOut;


  private final String[] _structure;

  /**
   * Constructs a new portfolio loader ready to load a portfolio from file.
   *
   * @param toolContext tool context for executing the load - must not be null
   * @param portfolioName the name for the portfolio - must not be null when write is true
   * @param securityType the security type for the portfolio (if not a multi-asset portfolio)
   * @param fileName the filename to read the portfolio from - must not be null
   * @param write should the data actually be written to the masters
   * @param verbose should the output be verbose
   * @param mergePositions should positions in the same security and within the same portfolio node be merged into one
   * @param keepCurrentPositions should positions in the previous portfolio version be kept, otherwise start from scratch
   * @param ignoreVersion should the version hashes in the multi-asset zip file be ignored
   * @param logToSystemOut should logging go to system out or standard logger
   * @param structure the portfolio structure, preserve existing structure if null, flatten if zero-length array,
   */
  public PortfolioLoader(ToolContext toolContext, String portfolioName, String securityType, String fileName,
                         boolean write, boolean verbose, boolean mergePositions,
                         boolean keepCurrentPositions, boolean ignoreVersion, boolean logToSystemOut,
                         String[] structure) {

    ArgumentChecker.notNull(toolContext, "toolContext ");
    ArgumentChecker.isTrue(!write || portfolioName != null, "Portfolio name must be specified if writing to a master");
    ArgumentChecker.notNull(fileName, "fileName ");

    _toolContext = toolContext;
    _fileName = fileName;
    _suggestedPortfolioName = portfolioName;
    _write = write;
    _securityType = securityType;
    _verbose = verbose;
    _mergePositions = mergePositions;
    _keepCurrentPositions = keepCurrentPositions;
    _ignoreVersion = ignoreVersion;
    _logToSystemOut = logToSystemOut;
    _structure = structure;
  }

  /**
   * Execute the portfolio load(s) with the configured options.
   */
  public void execute() {

    for (PositionReader positionReader : constructPortfolioReaders(_fileName, _securityType, _ignoreVersion)) {

      // Get the name of the portfolio from the reader if it supplies one
      String name = positionReader.getPortfolioName();

      String portfolioName = name != null ? name : _suggestedPortfolioName;
      PositionWriter positionWriter =
          constructPortfolioWriter(_toolContext, portfolioName, _write, _mergePositions, _keepCurrentPositions);
      SimplePortfolioCopier portfolioCopier = new SimplePortfolioCopier(_structure);

      // Create visitor for verbose/quiet mode
      PortfolioCopierVisitor portfolioCopierVisitor =
          _verbose ? new VerbosePortfolioCopierVisitor() : new QuietPortfolioCopierVisitor();

      // Call the portfolio loader with the supplied arguments
      portfolioCopier.copy(positionReader, positionWriter, portfolioCopierVisitor);

      // close stuff
      positionReader.close();
      positionWriter.close();
    }
  }

  private PositionWriter constructPortfolioWriter(ToolContext toolContext,
                                                   String portfolioName,
                                                   boolean write,
                                                   boolean mergePositions,
                                                   boolean keepCurrentPositions) {

    if (write) {

      // Check that the portfolio name was specified
      if (portfolioName == null) {
        throw new OpenGammaRuntimeException("Portfolio name omitted, cannot persist to OpenGamma masters");
      }

      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterPositionWriter(portfolioName,
                                       toolContext.getPortfolioMaster(),
                                       toolContext.getPositionMaster(),
                                       toolContext.getSecurityMaster(),
                                       mergePositions,
                                       keepCurrentPositions,
                                       false);

    } else {

      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new PrettyPrintingPositionWriter(true);
    }
  }

  private Iterable<? extends PositionReader> constructPortfolioReaders(String filename, String securityType, boolean ignoreVersion) {

    switch (SheetFormat.of(filename)) {

      case CSV:
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
        return ImmutableList.of(new ZippedPositionReader(filename, ignoreVersion));

      default:
        throw new OpenGammaRuntimeException("Input filename should end in .CSV, .XLS, .XML or .ZIP");
    }
  }
}
