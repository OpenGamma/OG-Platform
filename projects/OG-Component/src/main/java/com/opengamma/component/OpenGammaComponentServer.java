/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.StartupUtils;

/**
 * Main entry point for OpenGamma component-based servers.
 * <p>
 * This class starts an OpenGamma JVM process using the specified config file.
 * A {@link OpenGammaComponentServerMonitor monitor} thread will also be started.
 * <p>
 * Two types of config file format are recognized - properties and INI.
 * A properties file must be in the standard Java format and contain a key "MANAGER.NEXT.FILE"
 * which is the resource location of the main INI file.
 * The INI file is described in {@link ComponentConfigIniLoader}.
 */
public class OpenGammaComponentServer {

  /**
   * The server name property.
   * DO NOT deduplicate with the same value in ComponentManager.
   * This constant is used to set a system property before ComponentManager is class loaded.
   */
  private static final String OPENGAMMA_SERVER_NAME = "og.server.name";
  /**
   * Help command line option.
   */
  private static final String HELP_OPTION = "help";
  /**
   * Verbose command line option.
   */
  private static final String VERBOSE_OPTION = "verbose";
  /**
   * Quiet command line option.
   */
  private static final String QUIET_OPTION = "quiet";
  /**
   * Command line options.
   */
  private static final Options OPTIONS = getOptions();

  /**
   * The logger in use.
   */
  private ComponentLogger _logger = ComponentLogger.Console.VERBOSE;

  static {
    StartupUtils.init();
  }

  /**
   * Main method to start an OpenGamma JVM process.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (!new OpenGammaComponentServer().run(args)) {
      System.exit(0);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server.
   * 
   * @param args  the arguments, not null
   * @return true if the server is started, false if there was a problem
   */
  public boolean run(String[] args) {
    CommandLine cmdLine;
    try {
      cmdLine = (new PosixParser()).parse(OPTIONS, args);
    } catch (ParseException ex) {
      _logger.logError(ex.getMessage());
      usage();
      return false;
    }
    
    if (cmdLine.hasOption(HELP_OPTION)) {
      usage();
      return false;
    }
    
    int verbosity = 2;
    if (cmdLine.hasOption(VERBOSE_OPTION)) {
      verbosity = 3;
    } else if (cmdLine.hasOption(QUIET_OPTION)) {
      verbosity = 0;
    }
    _logger = createLogger(verbosity);
    
    args = cmdLine.getArgs();
    if (args.length == 0) {
      _logger.logError("No config file specified");
      usage();
      return false;
    }
    Map<String, String> properties = new HashMap<String, String>();
    if (args.length > 1) {
      for (int i = 1; i < args.length; i++) {
        String arg = args[i];
        int equalsPosition = arg.indexOf('=');
        if (equalsPosition < 0) {
          throw new OpenGammaRuntimeException("Invalid property format, must be key=value (no spaces)");
        }
        String key = arg.substring(0, equalsPosition).trim();
        String value = arg.substring(equalsPosition + 1).trim();
        if (key.length() == 0) {
          throw new IllegalArgumentException("Invalid empty property key");
        }
        if (properties.containsKey(key)) {
          throw new IllegalArgumentException("Invalid property, key '" + key + "' specified twice");
        }
        properties.put(key, value);
      }
    }
    String configFile = args[0];
    
    return run(configFile, properties);
  }

  //-------------------------------------------------------------------------
  /**
   * Called just before the server is started. The default implementation here
   * creates a monitor thread that allows the server to be stopped remotely.
   * 
   * @param manager the component manager
   */
  protected void serverStarting(final ComponentManager manager) {
    OpenGammaComponentServerMonitor.create(manager.getRepository());
  }
  
  /**
   * Runs the server with config file.
   * 
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @return true if the server was started, false if there was a problem
   */
  protected boolean run(String configFile, Map<String, String> properties) {
    long start = System.nanoTime();
    _logger.logInfo("======== STARTING OPENGAMMA ========");
    _logger.logDebug(" Config file: " + configFile);
    
    // extract the server name from the file name
    String serverName = extractServerName(configFile);
    System.setProperty(OPENGAMMA_SERVER_NAME, serverName);
    
    // create the manager
    ComponentManager manager = createManager(serverName);
    manager.getProperties().putAll(properties);
    
    // start server
    try {
      serverStarting(manager);
      manager.start(configFile);
      
    } catch (Exception ex) {
      _logger.logError(ex);
      _logger.logError("======== OPENGAMMA STARTUP FAILED ========");
      return false;
    }
    
    long end = System.nanoTime();
    _logger.logInfo("======== OPENGAMMA STARTED in " + ((end - start) / 1000000) + "ms ========");
    return true;
  }

  /**
   * Extracts the server name.
   * <p>
   * This examines the first part of the file name and the last directory,
   * merging these with a dash.
   * 
   * @param fileName  the name to extract from, not null
   * @return the server name, not null
   */
  protected String extractServerName(String fileName) {
    if (fileName.contains(":")) {
      fileName = StringUtils.substringAfter(fileName, ":");
    }
    fileName = FilenameUtils.removeExtension(fileName);
    String first = FilenameUtils.getName(FilenameUtils.getPathNoEndSeparator(fileName));
    String second = FilenameUtils.getName(fileName);
    if (StringUtils.isEmpty(first) || first.equals(second) || second.startsWith(first + "-")) {
      return second;
    }
    return first + "-" + second;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the logger.
   * 
   * @param verbosity  the verbosity required, 0=errors, 3=debug
   * @return the logger, not null
   */
  protected ComponentLogger createLogger(int verbosity) {
    return new ComponentLogger.Console(verbosity);
  }

  /**
   * Creates the component manager.
   *
   * @param serverName  the server name, not null
   * @return the manager, not null
   */
  protected ComponentManager createManager(String serverName) {
    return new ComponentManager(serverName, _logger);
  }

  //-------------------------------------------------------------------------
  private void usage() {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.setWidth(100);
    helpFormatter.printHelp(getClass().getSimpleName() + " [options] configFile", OPTIONS);
  }

  private static Options getOptions() {
    Options options = new Options();
    options.addOption(new Option("h", HELP_OPTION, false, "print this help message"));
    options.addOptionGroup(new OptionGroup()
        .addOption(new Option("q", QUIET_OPTION, false, "be quiet during startup"))
        .addOption(new Option("v", VERBOSE_OPTION, false, "be verbose during startup")));
    return options;
  }

}
