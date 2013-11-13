/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.opengamma.scripts.Scriptable;

/**
 *
 */
@Scriptable
public class ViewRegressionTestSetupTool {

  private static final Options OPTIONS = createOptions();
  private static final String PROJECT_NAME = "pn";
  private static final String SERVER_CONFIG = "sc";
  private static final String DB_DUMP_DIR = "dd";
  private static final String LOGBACK_CONFIG = "l";
  private static final String HELP = "h";
  private static final String WORKING_DIR = "wd";
  private static final String VERSION = "v";
  private static final String DB_PROPS = "dp";

  public static void main(final String[] args) { // CSIGNORE
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
    String logbackConfig = "-Dlogback.configurationFile=" + cl.getOptionValue(LOGBACK_CONFIG);

    new ViewRegressionTestSetup(cl.getOptionValue(DB_DUMP_DIR),
                                cl.getOptionValue(SERVER_CONFIG),
                                cl.getOptionValue(DB_PROPS),
                                logbackConfig,
                                cl.getOptionValue(PROJECT_NAME),
                                cl.getOptionValue(VERSION),
                                cl.getOptionValue(WORKING_DIR)).run();
    System.exit(0);
  }

  private static void printUsage() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + ViewRegressionTestSetupTool.class.getName(), OPTIONS, true);
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

    Option baseDirOption = new Option(WORKING_DIR, "workingdir", true, "Working directory for the base version of the server");
    baseDirOption.setRequired(true);
    options.addOption(baseDirOption);

    Option baseVersionOption = new Option(VERSION, "version", true, "Version of the server");
    baseVersionOption.setRequired(true);
    options.addOption(baseVersionOption);

    Option newPropsOption = new Option(DB_PROPS, "dbprops", true, "The DB properties file for the server");
    newPropsOption.setRequired(true);
    options.addOption(newPropsOption);

    Option helpOption = new Option(HELP, "help", true, "Print usage");
    options.addOption(helpOption);

    return options;
  }
}
