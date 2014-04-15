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
import org.apache.shiro.UnavailableSecurityManagerException;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.StartupUtils;
import com.opengamma.util.auth.AuthUtils;

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
 * <p>
 * This class is not thread-safe.
 * A new instance should be created for each thread.
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
   * Load-only command line option.
   */
  private static final String LOAD_ONLY_OPTION = "load-only";
  /**
   * Property-display command line option.
   */
  private static final String PROPERTY_DISPLAY_OPTION = "property-display";
  /**
   * Command line options.
   */
  private static final Options OPTIONS = getOptions();
  /**
   * Message logged when startup begins.
   */
  public static final String STARTING_MESSAGE = "======== STARTING OPENGAMMA ========";
  /**
   * Message logged if startup fails.
   */
  public static final String STARTUP_FAILED_MESSAGE = "======== OPENGAMMA STARTUP FAILED ========";
  /**
   * Prefix of the message logged when startup completes.
   */
  public static final String STARTUP_COMPLETE_MESSAGE = "======== OPENGAMMA STARTED in ";

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
      ShutdownUtils.exit(-1);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server.
   * <p>
   * This takes the same arguments as the standard main method command line.
   * 
   * @param args  the arguments, not null
   * @return true if the server is started, false if there was a problem
   * @throws RuntimeException if an error occurs
   */
  public boolean run(String[] args) {
    // parse command line
    CommandLine cmdLine;
    try {
      cmdLine = (new PosixParser()).parse(OPTIONS, args);
    } catch (ParseException ex) {
      _logger.logError(ex.getMessage());
      usage();
      return false;
    }
    // help option
    if (cmdLine.hasOption(HELP_OPTION)) {
      usage();
      return false;
    }
    // logger option
    int verbosity = 2;
    if (cmdLine.hasOption(VERBOSE_OPTION)) {
      verbosity = 3;
    } else if (cmdLine.hasOption(QUIET_OPTION)) {
      verbosity = 0;
    }
    _logger = createLogger(verbosity);
    // config file
    args = cmdLine.getArgs();
    if (args.length == 0) {
      _logger.logError("No config file specified");
      usage();
      return false;
    }
    String configFile = args[0];
    // properties
    Map<String, String> properties = new HashMap<>();
    if (args.length > 1) {
      for (int i = 1; i < args.length; i++) {
        String arg = args[i];
        int equalsPosition = arg.indexOf('=');
        if (equalsPosition < 0) {
          throw new ComponentConfigException("Invalid property format, must be key=value (no spaces)");
        }
        String key = arg.substring(0, equalsPosition).trim();
        String value = arg.substring(equalsPosition + 1).trim();
        if (key.length() == 0) {
          throw new ComponentConfigException("Invalid empty property key");
        }
        if (properties.containsKey(key)) {
          throw new ComponentConfigException("Invalid property, key '" + key + "' specified twice");
        }
        properties.put(key, value);
      }
    }
    // run
    if (cmdLine.hasOption(PROPERTY_DISPLAY_OPTION)) {
      return displayProperty(configFile, properties, cmdLine.getOptionValue(PROPERTY_DISPLAY_OPTION));
    } else if (cmdLine.hasOption(LOAD_ONLY_OPTION)) {
      return loadOnly(configFile, properties);
    } else {
      return (run(configFile, properties) != null);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server from application code.
   * <p>
   * This is intended for use by applications that wrap the server startup
   * in another class with its own main method. This would typically be
   * done to control the set of override properties using code.
   * <p>
   * A variety of loggers are provided in {@link ComponentLogger} nested classes.
   * The {@code ComponentLogger.Console.VERBOSE} logger is used if null is passed in.
   * 
   * @param configFile  the configuration file to use, not null
   * @param properties  the set of override properties to use, not null
   * @param logger  the logger to use, null uses verbose
   * @return the component repository, null if there was an error
   * @throws RuntimeException if an error occurs
   */
  public ComponentRepository run(String configFile, Map<String, String> properties, ComponentLogger logger) {
    ArgumentChecker.notNull(configFile, "configFile");
    ArgumentChecker.notNull(properties, "properties");
    _logger = (logger != null ? logger : ComponentLogger.Console.VERBOSE);
    return run(configFile, properties);
  }

  /**
   * Initializes a {@code ComponentManager} using a configuration file and properties.
   * <p>
   * This is intended for use by applications that wrap this class.
   * It allows the caller to initialize a {@link ComponentManager} in a manner
   * identical to that of a standard system startup.
   * <p>
   * The manager will have had the {@code load(String)} method called.
   * It can be queried to see the configuration loaded. It can also be used
   * to actually start the system using {@code init()} and {@code start()}.
   * <p>
   * This method will throw an exception on errors rather than using a logger.
   * 
   * @param configFile  the configuration file to use, not null
   * @param properties  the set of override properties to use, not null
   * @return the property value, null if no such property
   * @throws RuntimeException if an error occurs
   */
  public ComponentManager createManager(String configFile, Map<String, String> properties) {
    ArgumentChecker.notNull(configFile, "configFile");
    ArgumentChecker.notNull(properties, "properties");
    _logger = ComponentLogger.Throws.INSTANCE;
    ComponentManager manager = buildManager(configFile, properties);
    manager.load(configFile);
    return manager;
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the config files without starting the server.
   * 
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @return false always
   */
  protected boolean loadOnly(String configFile, Map<String, String> properties) {
    _logger.logDebug(" Config locator: " + configFile);
    
    try {
      ComponentManager manager = buildManager(configFile, properties);
      manager.load(configFile);
      
    } catch (Exception ex) {
      _logger.logError(ex);
      return false;
    }
    return false;
  }

  //-------------------------------------------------------------------------
  /**
   * Displays the value of the property from the config files without starting the server.
   * 
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @param property  the property to display, not null
   * @return false always
   */
  protected boolean displayProperty(String configFile, Map<String, String> properties, String property) {
    try {
      String value = queryProperty(configFile, properties, property);
      if (value == null) {
        System.out.println("NO-SUCH-PROPERTY");
      } else {
        System.out.println(value);
      }
      
    } catch (Exception ex) {
      _logger.logError(ex);
      return false;
    }
    return false;
  }

  /**
   * Queries the value of the property from the config files without starting the server.
   * 
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @param property  the property to display, not null
   * @return the property value, null if no such property
   * @throws RuntimeException if an error occurs
   */
  protected String queryProperty(String configFile, Map<String, String> properties, String property) {
    ComponentManager manager = buildManager(configFile, properties);
    manager.load(configFile);
    return manager.getProperties().get(property);
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server with config file.
   * 
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @return true if the server was started, false if there was a problem
   */
  protected ComponentRepository run(String configFile, Map<String, String> properties) {
    long start = System.nanoTime();
    _logger.logInfo(STARTING_MESSAGE);
    _logger.logDebug(" Config locator: " + configFile);
    
    ComponentRepository repo;
    try {
      ComponentManager manager = buildManager(configFile, properties);
      serverStarting(manager);
      repo = manager.start(configFile);
      checkSecurityManager();
      
    } catch (Exception ex) {
      _logger.logError(ex);
      _logger.logError(STARTUP_FAILED_MESSAGE);
      return null;
    }
    
    long end = System.nanoTime();
    _logger.logInfo(STARTUP_COMPLETE_MESSAGE + ((end - start) / 1000000) + "ms ========");
    return repo;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds the component manager.
   *
   * @param configFile  the config file, not null
   * @param properties  the properties read from the command line, not null
   * @return the manager, not null
   */
  protected ComponentManager buildManager(String configFile, Map<String, String> properties) {
    String serverName = extractServerName(configFile);
    System.setProperty(OPENGAMMA_SERVER_NAME, serverName);
    ComponentManager manager = createManager(serverName);
    manager.getProperties().putAll(properties);
    return manager;
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
   * Called once the server has started to check the security manager.
   */
  protected void checkSecurityManager() {
    try {
      if (AuthUtils.isPermissive()) {
        _logger.logWarn("*********************************************************");
        _logger.logWarn(" Warning: Server running with permissive SecurityManager ");
        _logger.logWarn("*********************************************************");
      }
    } catch (UnavailableSecurityManagerException ex) {
      _logger.logError("***************************************************");
      _logger.logError(" Error: Server running without any SecurityManager ");
      _logger.logError("***************************************************");
    }
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
        .addOption(new Option("l", LOAD_ONLY_OPTION, false, "load the config, but do not start the server"))
        .addOption(new Option("p", PROPERTY_DISPLAY_OPTION, true, "displays the calculated value of a property")));
    options.addOptionGroup(new OptionGroup()
        .addOption(new Option("q", QUIET_OPTION, false, "be quiet during startup"))
        .addOption(new Option("v", VERBOSE_OPTION, false, "be verbose during startup")));
    return options;
  }

}
