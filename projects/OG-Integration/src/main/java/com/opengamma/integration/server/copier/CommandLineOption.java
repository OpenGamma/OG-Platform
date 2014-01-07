/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.server.copier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.opengamma.util.ArgumentChecker;

/**
 * Server copier command line options
 */
public class CommandLineOption {
  
  /**
   * Help command line option.
   */
  static final String HELP_OPTION = "h";
  /**
   * tool context config name option.
   */
  static final String TOOLCONTEXT_CONFIG = "config";
  /**
   * server url option.
   */
  static final String SERVER = "server";
  
  private String _configFile;
  
  private String _serverUrl;

  public CommandLineOption(String[] args, Class<?> entryPointClazz) {
    ArgumentChecker.notNull(args, "args");
    ArgumentChecker.notNull(entryPointClazz, "entryPointClazz");
    
    Options options = getCommandLineOption();
    
    final CommandLineParser parser = new PosixParser();
    CommandLine line = null;
    try {
      line = parser.parse(options, args);
    } catch (final ParseException e) {
      usage(options, entryPointClazz);
    }
    if (line.hasOption(HELP_OPTION)) {
      usage(options, entryPointClazz);
    } else {
      _configFile = line.getOptionValue(TOOLCONTEXT_CONFIG);
      _serverUrl = line.getOptionValue(SERVER);
    }
  }

  private Options getCommandLineOption() {
    Options options = new Options();
    Option configOption = new Option("c", TOOLCONTEXT_CONFIG, true, "The tool context config file");
    configOption.setRequired(true);
    options.addOption(configOption);

    Option serverUrlOption = new Option("s", SERVER, true, "The opengamma server url");
    serverUrlOption.setRequired(true);
    options.addOption(serverUrlOption);
    return options;
  }

  public String getConfigFile() {
    return _configFile;
  }

  public String getServerUrl() {
    return _serverUrl;
  }
  
  private void usage(final Options options, Class<?> entryPointClazz) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + entryPointClazz.getName(), options, true);
  }

}
