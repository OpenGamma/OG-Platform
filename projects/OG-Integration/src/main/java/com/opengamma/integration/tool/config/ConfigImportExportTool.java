/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchSortOrder;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master. The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class ConfigImportExportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ConfigImportExportTool.class);

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { // CSIGNORE
    new ConfigImportExportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    PortfolioMaster portfolioMaster = toolContext.getPortfolioMaster();
    CommandLine commandLine = getCommandLine();
    @SuppressWarnings("unchecked")
    List<String> fileList = commandLine.getArgList();
    for (String file : fileList) {
      System.err.println(file);
    }
    boolean portPortfolioRefs = commandLine.hasOption("portable-portfolios");
    boolean verbose = commandLine.hasOption("verbose");
    if (commandLine.hasOption("load")) {
      checkForInvalidOption("type");
      checkForInvalidOption("name");
      checkForInvalidOption("save");
      checkForInvalidOption("sort-by-name");
      boolean persist = !commandLine.hasOption("do-not-persist"); // NOTE: inverted logic here
      ConfigLoader configLoader = new ConfigLoader(configMaster, portfolioMaster, portPortfolioRefs, persist, verbose);
      if (fileList.size() > 0) {
        boolean problems = false;
        for (String fileName : fileList) {
          File file = new File(fileName);
          if (!file.exists()) {
            s_logger.error("Could not find file:" + fileName);
            problems = true;
          }
          if (!file.canRead()) {
            s_logger.error("Not able to read file (permissions?):" + fileName);
            problems = true;
          }
        }
        if (problems) {
          s_logger.error("Problems with one or more files, aborting.");
          System.exit(1);
        }
        try {
          for (String fileName : fileList) {
            if (verbose) {
              s_logger.info("Processing " + fileName);
            }
            FileInputStream inputStream = new FileInputStream(fileName);
            configLoader.loadConfig(inputStream);
          }
        } catch (IOException ioe) {
          if (verbose) {
            s_logger.error("An I/O error occurred while processing a file (run with -v to see stack trace)");
          } else {
            s_logger.error("An I/O error occurred while processing a file", ioe);
          }
        }
      } else {
        if (verbose) {
          s_logger.info("No file name given, assuming STDIN");
        }
        configLoader.loadConfig(System.in);
      }
    } else if (commandLine.hasOption("save")) {
      if (verbose) {
        s_logger.info("Save option active");
      }
      checkForInvalidOption("do-not-persist");
      List<String> types = getTypes();
      List<String> names = getNames();
      PrintStream outputStream;
      if (fileList.size() == 1) {
        try {
          outputStream = new PrintStream(new FileOutputStream(fileList.get(0)));
        } catch (FileNotFoundException ex) {
          s_logger.error("Couldn't find file " + fileList.get(0));
          System.exit(1);
          return;
        }
      } else {
        outputStream = System.out;
      }
      ConfigSearchSortOrder order = ConfigSearchSortOrder.VERSION_FROM_INSTANT_DESC;
      if (commandLine.hasOption("sort-by-name")) {
        order = ConfigSearchSortOrder.NAME_ASC;
      }
      ConfigSaver configSaver = new ConfigSaver(configMaster, portfolioMaster, names, types, portPortfolioRefs, verbose, order);
      configSaver.saveConfigs(outputStream);
      System.out.println("Warning: file may have been created in installation base directory");
    }
  }

  private List<String> getTypes() {
    if (getCommandLine().hasOption("type")) {
      String[] typeValues = getCommandLine().getOptionValues("type");
      return Arrays.asList(typeValues);
    } else {
      return Collections.emptyList();
    }
  }

  private List<String> getNames() {
    if (getCommandLine().hasOption("name")) {
      String[] nameValues = getCommandLine().getOptionValues("name");
      return Arrays.asList(nameValues);
    } else {
      return Collections.emptyList();
    }
  }

  private void checkForInvalidOption(String longOpt) {
    if (getCommandLine().hasOption(longOpt)) {
      System.err.println("Option " + longOpt + " is invalid in this context");
      System.exit(1);
    }
  }

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createTypeOption());
    options.addOption(createSearchOption());
    options.addOption(createLoadOption());
    options.addOption(createSaveOption());
    options.addOption(createPortablePortfolioReferencesOption());
    options.addOption(createDoNotPersistOption());
    options.addOption(createVerboseOption());
    options.addOption(createSortOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createTypeOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("full class name").withDescription("The type(s) you want to export").withLongOpt("type").create("t");
  }

  @SuppressWarnings("static-access")
  private Option createSearchOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("name search string").withDescription("The name(s) you want to search for (globbing available)").withLongOpt("name")
        .create("n");
  }

  @SuppressWarnings("static-access")
  private Option createLoadOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Load from file to config database").withLongOpt("load").create("load");
  }

  @SuppressWarnings("static-access")
  private Option createSaveOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Save to file from config database").withLongOpt("save").create("save");
  }

  @SuppressWarnings("static-access")
  private Option createPortablePortfolioReferencesOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Attempt to port portfolio reference ids").withLongOpt("portable-portfolios").create("p");
  }

  @SuppressWarnings("static-access")
  private Option createDoNotPersistOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Simulate writing rather than actually writing to DB").withLongOpt("do-not-persist").create("d");
  }

  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Display extra error messages").withLongOpt("verbose").create("v");
  }

  @SuppressWarnings("static-access")
  private Option createSortOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Sort output by config name (default=most recent first)").withLongOpt("sort-by-name").create("s");
  }

  @Override
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("config-import-export-tool.sh [file...]", options, true);
  }

}
