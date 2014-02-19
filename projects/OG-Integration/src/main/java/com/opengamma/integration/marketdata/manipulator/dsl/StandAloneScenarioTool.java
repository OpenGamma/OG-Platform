/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.ToolUtils;
import com.opengamma.scripts.Scriptable;

/**
 * <p>Tool for executing stand alone scenario scripts against a remote server.
 * Invokes {@link StandAloneScenarioRunner#runScenarioScript}.</p>
 *
 * <p>Usage {@code stand-alone-scenario-tool.sh script}</p>
 */
@Scriptable
public class StandAloneScenarioTool {

  private static final Logger s_logger = LoggerFactory.getLogger(StandAloneScenarioTool.class);

  private static final Options OPTIONS = createOptions();

  private static final String LOGBACK_CONFIG = "l";
  private static final String SHORT_FORMAT = "s";
  private static final String VERBOSE_OUTPUT = "v";

  private static final String EXTENSION = ".txt";

  public static void main(String[] args) throws IOException {
    CommandLineParser parser = new PosixParser();
    CommandLine commandLine;

    try {
      commandLine = parser.parse(OPTIONS, args);
    } catch (final ParseException e) {
      printUsage();
      return;
    }
    if (commandLine.getArgList().size() == 0) {
      System.out.println("Please specify a script file to execute");
      printUsage();
      return;
    }
    String logbackResource = commandLine.getOptionValue(LOGBACK_CONFIG, ToolUtils.getDefaultLogbackConfiguration());
    ToolUtils.initLogback(logbackResource);

    boolean verbose = commandLine.hasOption(VERBOSE_OUTPUT);
    List<ScenarioResultModel> results;
    File scriptFile;

    try {
      scriptFile = new File((String) commandLine.getArgList().get(0));
      results = StandAloneScenarioRunner.runScenarioScript(scriptFile);
    } catch (Exception e) {
      if (verbose) {
        s_logger.warn("Failed to run scenario script", e);
      } else {
        System.err.println("Failed to run scenario script. " + e.getMessage());
      }
      System.exit(1);
      return;
    }
    String resultsFileRoot = FilenameUtils.removeExtension(scriptFile.getName()) + ".results";
    File resultsFile = new File(resultsFileRoot + EXTENSION);
    int fileSuffix = 1;

    // generate a file name that doesn't exist
    while (resultsFile.exists()) {
      resultsFile = new File(resultsFileRoot + fileSuffix++ + EXTENSION);
    }
    try (Writer writer = new BufferedWriter(new FileWriter(resultsFile))) {
      // default to long format, user has to specify short format if they want it
      if (commandLine.hasOption(SHORT_FORMAT)) {
        ScenarioResultsWriter.writeShortFormat(results, writer);
      } else {
        ScenarioResultsWriter.writeLongFormat(results, writer);
      }
    }
    System.out.println("Successfully wrote scenario results to " + resultsFile.getAbsolutePath());
    System.exit(0);
  }

  private static Options createOptions() {
    Options options = new Options();

    Option shortFormatOption = new Option(SHORT_FORMAT, "Outputs data in a shorter format");
    options.addOption(shortFormatOption);

    Option verboseOption = new Option(VERBOSE_OUTPUT, "Prints stack traces as well as error messages");
    options.addOption(verboseOption);

    Option logbackConfigOption = new Option(LOGBACK_CONFIG, true, "Logback config for the tool");
    options.addOption(logbackConfigOption);

    return options;
  }

  private static void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("stand-alone-scenario-tool.sh <script>", OPTIONS, true);
  }

  // TODO options:
  // output file?
  // server location / config location?
}
