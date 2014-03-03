/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to read currency pairs from a text file and store them in the config master. The pairs must be in the format AAA/BBB, one per line in the file.
 */
@Scriptable
public class SingleConfigImportTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(SingleConfigImportTool.class);
  private static final long DEFAULT_MARK_BUFFER = 1000000; // 1MB should do it.

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { // CSIGNORE
    new SingleConfigImportTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();
    ConfigSource configSource = toolContext.getConfigSource();
    ConventionMaster conventionMaster = toolContext.getConventionMaster();
    MarketDataSnapshotMaster marketDataSnapshotMaster = toolContext.getMarketDataSnapshotMaster();
    SecurityMaster secMaster = toolContext.getSecurityMaster();
    CommandLine commandLine = getCommandLine();
    @SuppressWarnings("unchecked")
    List<String> fileList = commandLine.getArgList();
    for (String file : fileList) {
      System.err.println(file);
    }
    boolean verbose = commandLine.hasOption("verbose");
    if (commandLine.hasOption("load")) {
      checkForInvalidOption("type");
      SingleConfigLoader configLoader = new SingleConfigLoader(secMaster, configMaster, configSource, conventionMaster, marketDataSnapshotMaster, commandLine.hasOption("do-not-update"));
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
            FileInputStream inputStream;
            File file = new File(fileName);
            if (file.isFile()) {
              if (file.getName().endsWith(".zip")) {
                ZipFile zipFile = new ZipFile(file);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                  final ZipEntry zipEntry = entries.nextElement();
                  if (!zipEntry.isDirectory()) {
                    final String zipFileName = zipEntry.getName();
                    if (zipFileName.endsWith(".xml")) {
                      if (verbose) {
                        s_logger.info("Processing file {} in zip archive", zipFileName);
                      }
                      long fileSize = zipEntry.getSize();
                      storeObjectFromStream(configLoader, fileSize, zipFile.getInputStream(zipEntry));
                    } else {
                      s_logger.warn("File {} not xml, skipping...", zipFileName);
                    }
                  }
                }
              } else if (file.getName().endsWith(".xml")) {
                inputStream = new FileInputStream(fileName);
                long fileSize = file.length();
                storeObjectFromStream(configLoader, fileSize, inputStream);
              } else {
                s_logger.error("File type not recognised, pass either a zip or xml file");
                System.exit(1);
              }
            } else {
              s_logger.error("Path is not a file");
              System.exit(1);
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

  /**
   * @param commandLine
   * @param configLoader
   * @param fileName
   * @param inputStream
   * @return
   */
  private void storeObjectFromStream(SingleConfigLoader configLoader, long fileSize, InputStream rawInputStream) {
    InputStream inputStream = new BufferedInputStream(rawInputStream, (int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER));
    if (getCommandLine().hasOption("type")) {
      List<String> types = getTypes();
      if (types.size() > 1) {
        s_logger.error("More than one type specified");
        System.exit(1);
      }
      try {
        Class<?> type = Class.forName(types.get(0));
        try {
          inputStream.mark((int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER));
          configLoader.loadConfig(inputStream, type);
          s_logger.info("Config loaded successfully");
        } catch (Exception e) {
          // try loading it as fudge!
          try {
            inputStream.reset();
            configLoader.loadFudgeConfig(inputStream);
            s_logger.info("Config loaded successfully");
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
        inputStream.mark((int) (fileSize > 0 ? fileSize : DEFAULT_MARK_BUFFER)); 
        configLoader.loadConfig(inputStream);
        s_logger.info("Config loaded successfully as JodaXML");
      } catch (Exception e) {
        try {
          // close it - we could use mark/reset, but this is simpler.
          inputStream.reset();               
          configLoader.loadFudgeConfig(inputStream);
          s_logger.info("Config loaded successfully as FudgeXML");
        } catch (Exception fe) {
          s_logger.error("Exception thrown when loading as both JodaXML and as FudgeXML");
          s_logger.error("JodaXML trace", e);
          s_logger.error("Fudge trace", e);                  
        }
      }
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
    options.addOption(createDontUpdateOption());
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
  private Option createDontUpdateOption() {
    return OptionBuilder.isRequired(false).hasArg(false).withDescription("Don't update configs that already exist").withLongOpt("do-not-update").create("n");
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
