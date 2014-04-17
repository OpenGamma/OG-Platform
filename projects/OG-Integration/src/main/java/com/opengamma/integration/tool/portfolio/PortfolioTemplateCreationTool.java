/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.opengamma.integration.copier.portfolio.rowparser.JodaBeanRowParser;
import com.opengamma.integration.copier.portfolio.writer.PositionWriter;
import com.opengamma.integration.copier.portfolio.writer.SingleSheetSimplePositionWriter;
import com.opengamma.scripts.Scriptable;

/**
 * The portfolio saver tool
 */
@Scriptable
public class PortfolioTemplateCreationTool {

  /** Help command line option. */
  private static final String HELP_OPTION = "h";
  /** Asset class flag */
  private static final String SECURITY_TYPE_OPT = "s";
  /** Logback flag (ignored) */
  private static final String LOGBACK_OPTION = "l";

  /** The list of security types - needs to be updated whenever a new sec type is added to the system */
  private static final String[] s_securityTypes = {
    "CorporateBond", "GovernmentBond", "MunicipalBond",
    "CapFloorCMSSpread", "CapFloor",
    "Cash",
    "CDS", "LegacyFixedRecoveryCDS", "LegacyRecoveryLockCDS", "LegacyVanillaCDS", "StandardFixedRecoveryCDS", "StandardRecoveryLockCDS", "StandardVanillaCDS",
    "ContinuousZeroDeposit", "PeriodicZeroDeposit", "SimpleZeroDeposit",
    "Equity", "EquityVarianceSwap",
    "AgricultureForward", "CommodityForward", "EnergyForward", "MetalForward",
    "FRA",
    "AgricultureFuture", "BondFuture", "CommodityFuture", "EnergyFuture", "EquityFuture", "EquityIndexDividendFuture", "Future", "FXFuture",
    "IndexFuture", "InterestRateFuture", "MetalFuture", "StockFuture",
    "FXForward", "NonDeliverableFXForward",
    "BondFutureOption", "CommodityFutureOption", "EquityBarrierOption", "EquityIndexDividendFutureOption", "EquityIndexFutureOption", "EquityIndexOption", "EquityOption", "FXBarrierOption",
    "FXDigitalOption", "FXOption", "IRFutureOption", "NonDeliverableFXDigitalOption", "NonDeliverableFXOption",
    "ForwardSwap", "Swap"
  };

  /**
   * The command line.
   */
  private CommandLine _commandLine;

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE

    new PortfolioTemplateCreationTool().doRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   * @param args  the arguments to run with, not null
   */
  protected void doRun(String[] args) {
    Options options = createOptions();
    CommandLineParser parser = new PosixParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      usage(options);
      return;
    }
    _commandLine = line;
    if (line.hasOption(HELP_OPTION)) {
      usage(options);
      return;
    }

    String[] securityTypes = getCommandLine().getOptionValues(SECURITY_TYPE_OPT)[0].equals("all")
        ? s_securityTypes
        : getCommandLine().getOptionValues(SECURITY_TYPE_OPT);

    // Create portfolio writers to write header rows
    for (String securityType : securityTypes) {
      PositionWriter positionWriter = new SingleSheetSimplePositionWriter(securityType + ".csv",
          JodaBeanRowParser.newJodaBeanRowParser(securityType));
      positionWriter.close();
    }
  }

  protected Options createOptions() {
    
    Options options = new Options();

    String securityTypes = "";
    for (String s : s_securityTypes) {
      securityTypes += " " + s;
    }
    Option assetClassOption = new Option(
        SECURITY_TYPE_OPT, "securitytype", true, 
        "The security type(s) for which to generate a template, or 'all' to create a template for each available security type: "
            + securityTypes);
    assetClassOption.setRequired(true);
    options.addOption(assetClassOption);

    Option helpOption = new Option(
        HELP_OPTION, "help", false,
        "prints this message");
    options.addOption(helpOption);

    Option logbackOption = new Option(
        LOGBACK_OPTION, "logback", true,
        "Logback (ignored)");
    options.addOption(logbackOption);

    return options;
  }

  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + getClass().getName(), options, true);
  }

  protected CommandLine getCommandLine() {
    return _commandLine;
  }

}
