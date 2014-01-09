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
public class SingleConfigImportTool extends AbstractTool<ToolContext> {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleConfigImportTool.class);

  /**
   * Main method to run the tool.
   */
  public static void main(String[] args) { // CSIGNORE
    new SingleConfigImportTool().initAndRun(args, ToolContext.class);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    CommandLine commandLine = getCommandLine();
    @SuppressWarnings("unchecked")
    List<String> fileList = commandLine.getArgList();
    for (String file : fileList) {
      System.err.println(file);
    }
    boolean verbose = commandLine.hasOption("verbose");
    if (commandLine.hasOption("load")) {
      checkForInvalidOption("type");
      SingleConfigLoader configLoader = new SingleConfigLoader(configMaster);
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
            if (commandLine.hasOption("type")) {
              List<String> types = getTypes();
              if (types.size() > 1) {
                s_logger.error("More than one type specified");
                System.exit(1);
              }
              try {
                Class<?> type = Class.forName(types.get(0));
                try {
                  configLoader.loadConfig(inputStream, type);
                } catch (Exception e) {
                  // try loading it as fudge!
                  try {
                    // close it - we could use mark/reset, but this is simpler.
                    inputStream.close();
                    inputStream = new FileInputStream(fileName);
                    configLoader.loadFudgeConfig(inputStream);
                  } catch (Exception fe) {
                    s_logger.error("Exception thrown when loading as both JodaXML and as FudgeXML");
                    s_logger.error("JodaXML trace", e);
                    s_logger.error("Fudge trace", e);
                  }
                }
              } catch (ClassNotFoundException ex) {
                s_logger.error("Class {} not found", types.get(0));
                System.exit(1);
              }

            } else {
              try {
                configLoader.loadConfig(inputStream);
              } catch (Exception e) {
                try {
                  // close it - we could use mark/reset, but this is simpler.
                  inputStream.close();
                  inputStream = new FileInputStream(fileName);                  
                  configLoader.loadFudgeConfig(inputStream);
                } catch (Exception fe) {
                  s_logger.error("Exception thrown when loading as both JodaXML and as FudgeXML");
                  s_logger.error("JodaXML trace", e);
                  s_logger.error("Fudge trace", e);                  
                }
              }
            }
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
    } else {
      s_logger.info("Specify -load to load a config");
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
    options.addOption(createLoadOption());
    options.addOption(createVerboseOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createTypeOption() {
    return OptionBuilder.isRequired(false).hasArgs().withArgName("full class name").withDescription("The type(s) you want to export").withLongOpt("type").create("t");
  }

  @SuppressWarnings("static-access")
  private Option createLoadOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Load from file to config database").withLongOpt("load").create("load");
  }

  @SuppressWarnings("static-access")
  private Option createVerboseOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Display extra error messages").withLongOpt("verbose").create("v");
  }

  @Override
  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("single-config-import-tool.sh [file...]", options, true);
  }

}
