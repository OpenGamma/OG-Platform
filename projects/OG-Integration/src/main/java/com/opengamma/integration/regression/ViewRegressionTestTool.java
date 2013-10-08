/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.threeten.bp.Instant;

import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ViewRegressionTestTool {

  private static final Options OPTIONS = createOptions();
  private static final String PROJECT_NAME = "pn";
  private static final String SERVER_CONFIG = "sc";
  private static final String DB_DUMP_DIR = "dd";
  private static final String LOGBACK_CONFIG = "lc";
  private static final String VALUATION_TIME = "vt";
  private static final String BASE_DIR = "bd";
  private static final String NEW_DIR = "nd";
  private static final String BASE_VERSION = "bv";
  private static final String NEW_VERSION = "nv";
  private static final String BASE_PROPS = "bp";
  private static final String NEW_PROPS = "np";
  private static final String HELP = "h";

  /**
   * Main method to run the tool.
   *
   * @param args the arguments, unused
   */
  public static void main(String[] args) throws Exception { // CSIGNORE
    ViewRegressionTestTool.run(args);
  }

  private static void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + ViewRegressionTestTool.class.getName(), OPTIONS, true);
  }

  private static void run(String[] args) throws Exception {
    CommandLineParser parser = new PosixParser();
    CommandLine cl;
    try {
      cl = parser.parse(OPTIONS, args);
    } catch (final ParseException e) {
      printUsage();
      return;
    }
    if (cl.hasOption(HELP)) {
      printUsage();
      return;
    }
    Instant valuationTime;
    if (cl.hasOption(VALUATION_TIME)) {
      valuationTime = Instant.parse(cl.getOptionValue(VALUATION_TIME));
    } else {
      valuationTime = Instant.now();
    }
    ViewRegressionTest test = new ViewRegressionTest(cl.getOptionValue(PROJECT_NAME),
                                                     cl.getOptionValue(SERVER_CONFIG),
                                                     cl.getOptionValue(DB_DUMP_DIR),
                                                     cl.getOptionValue(LOGBACK_CONFIG),
                                                     valuationTime,
                                                     cl.getOptionValue(BASE_DIR),
                                                     cl.getOptionValue(BASE_VERSION),
                                                     cl.getOptionValue(BASE_PROPS),
                                                     cl.getOptionValue(NEW_DIR),
                                                     cl.getOptionValue(NEW_VERSION),
                                                     cl.getOptionValue(NEW_PROPS));
    Collection<CalculationDifference.Result> result = test.run();
    // TODO do something with the results
    System.out.println(result);
  }

  private static Options createOptions() {
    Options options = new Options();

    Option projectNameOption = new Option(PROJECT_NAME, "projectname", true, "Project name (as used in the build artifacts)");
    projectNameOption.setRequired(true);
    options.addOption(projectNameOption);

    Option serverConfigOption = new Option(SERVER_CONFIG, "serverconfig", true, "Configuration file used to run the server");
    serverConfigOption.setRequired(true);
    options.addOption(serverConfigOption);

    Option dbDumpDirOption = new Option(DB_DUMP_DIR, "dbdumpdir", true, "Directory containing the database dump files");
    dbDumpDirOption.setRequired(true);
    options.addOption(dbDumpDirOption);

    Option logbackConfigOption = new Option(LOGBACK_CONFIG, "logbackconfig", true, "Logback config for the servers");
    options.addOption(logbackConfigOption);

    Option valuationTimeOption = new Option(VALUATION_TIME, "valuationtime", true, "Valuation time for the views");
    options.addOption(valuationTimeOption);

    Option baseDirOption = new Option(BASE_DIR, "basedir", true, "Working directory for the base version of the server");
    baseDirOption.setRequired(true);
    options.addOption(baseDirOption);

    Option newDirOption = new Option(NEW_DIR, "newdir", true, "Working directory for the new version of the server");
    newDirOption.setRequired(true);
    options.addOption(newDirOption);

    Option baseVersionOption = new Option(BASE_VERSION, "baseversion", true, "Version of the base server");
    baseVersionOption.setRequired(true);
    options.addOption(baseVersionOption);

    Option newVersionOption = new Option(NEW_VERSION, "newversion", true, "Version of the new server");
    newVersionOption.setRequired(true);
    options.addOption(newVersionOption);

    Option basePropsOption = new Option(BASE_PROPS, "baseprops", true, "The DB properties file for the base server");
    basePropsOption.setRequired(true);
    options.addOption(basePropsOption);

    Option newPropsOption = new Option(NEW_PROPS, "newprops", true, "The DB properties file for the new server");
    newPropsOption.setRequired(true);
    options.addOption(newPropsOption);

    Option helpOption = new Option(HELP, "help", true, "Print usage");
    options.addOption(helpOption);

    return options;
  }
}
