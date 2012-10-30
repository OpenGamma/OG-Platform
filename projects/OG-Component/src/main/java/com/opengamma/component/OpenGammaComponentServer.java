/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.Resource;

/**
 * Main entry point for OpenGamma component-based servers.
 * <p>
 * This class starts an OpenGamma JVM process using the specified config file.
 * A {@link OpenGammaComponentServerMonitor monitor} thread will also be started.
 * <p>
 * Two types of config file format are recognized - properties and INI.
 * A properties file must be in the standard Java format and contain a key "component.ini"
 * which is the resource location of the main INI file.
 * The INI file is described in {@link ComponentConfigLoader}.
 */
public class OpenGammaComponentServer {
  
  /**
   * The server name property.
   * DO NOT deduplicate with the same value in ComponentManager.
   * This constant is used to set a system property before ComponentManager is class loaded.
   */
  private static final String OPENGAMMA_SERVER_NAME = "opengamma.server.name";
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
   * Main method to start an OpenGamma JVM process.
   * 
   * @param args  the arguments
   */
  public static void main(String[] args) { // CSIGNORE
    if (!new OpenGammaComponentServer().run(args)) {
      System.exit(0);
    }
  }
  
  protected void log(final String msg) {
    System.out.println(msg);
  }
  
  protected void log(final Throwable t) {
    t.printStackTrace(System.err);
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
      log(ex.getMessage());
      usage();
      return false;
    }
    
    if (cmdLine.hasOption(HELP_OPTION)) {
      usage();
      return false;
    }
    
    int verbosity = 1;
    if (cmdLine.hasOption(VERBOSE_OPTION)) {
      verbosity = 2;
    } else if (cmdLine.hasOption(QUIET_OPTION)) {
      verbosity = 0;
    }
    
    args = cmdLine.getArgs();
    if (args.length == 0) {
      log("No config file specified");
      usage();
      return false;
    }
    if (args.length > 1) {
      log("Only one config file can be specified");
      usage();
      return false;
    }
    String configFile = args[0];
    
    return run(verbosity, configFile);
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
   * @param verbosity  the verbosity (0 quiet, 1 normal, 2 verbose)
   * @param configFile  the config file, not null
   * @return true if the server was started, false if there was a problem
   */
  protected boolean run(int verbosity, String configFile) {
    long start = System.nanoTime();
    if (verbosity > 0) {
      log("======== STARTING OPENGAMMA ========");
      if (verbosity > 1) {
        log(" Config file: " + configFile);
      }
    }
    
    // extract the server name from the file name
    String serverName = extractServerName(configFile);
    System.setProperty(OPENGAMMA_SERVER_NAME, serverName);
    
    // create the manager
    ComponentManager manager = createManager(serverName, verbosity);
    
    // start server
    try {
      serverStarting(manager);
      manager.start(configFile);
      
    } catch (Exception ex) {
      log(ex);
      log("======== OPENGAMMA STARTUP FAILED ========");
      return false;
    }
    
    if (verbosity > 0) {
      long end = System.nanoTime();
      log("======== OPENGAMMA STARTED in " + ((end - start) / 1000000) + "ms ========");
    }
    return true;
  }

  //-------------------------------------------------------------------------
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
   * Creates the component manager.
   *
   * @param serverName  the server name, not null
   * @param verbosity  the verbosity level, 0 to 2
   * @return the manager, not null
   */
  protected ComponentManager createManager(String serverName, int verbosity) {
    ComponentManager manager;
    if (verbosity == 2) {
      manager = new VerboseManager(serverName, new VerboseRepository());
    } else if (verbosity == 1) {
      manager = new VerboseManager(serverName);
    } else {
      manager = new ComponentManager(serverName);
    }
    return manager;
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

  //-------------------------------------------------------------------------  
  /**
   * Manager that can output more verbose messages.
   */
  private class VerboseManager extends ComponentManager {
    public VerboseManager(String serverName) {
      super(serverName);
    }
    public VerboseManager(String serverName, ComponentRepository repo) {
      super(serverName, repo);
    }
    @Override
    public ComponentRepository start(Resource resource) {
      try {
        log("  Using file: " + resource.getURI());
      } catch (IOException ex) {
        try {
          log("  Using file: " + resource.getFile());
        } catch (IOException ex2) {
          log("  Using file: " + resource);
        }
      }
      return super.start(resource);
    }
    @Override
    protected void loadIni(Resource resource) {
      log("--- Using merged properties ---");
      Map<String, String> properties = new TreeMap<String, String>(getProperties());
      for (String key : properties.keySet()) {
        if (key.contains("password")) {
          log(" " + key + " = " + StringUtils.repeat("*", properties.get(key).length()));
        } else {
          log(" " + key + " = " + properties.get(key));
        }
      }
      super.loadIni(resource);
    }
    @Override
    protected void initComponent(String groupName, LinkedHashMap<String, String> remainingConfig) {
      String typeStr = remainingConfig.get("factory");
      log("--- Initializing " + groupName + " ---");
      if (typeStr != null) {
        log(" Using factory " + typeStr);
      }
      
      long startInstant = System.nanoTime();
      super.initComponent(groupName, remainingConfig);
      long endInstant = System.nanoTime();
      
      log("--- Initialized " + groupName + " in " + ((endInstant - startInstant) / 1000000L) + "ms ---");
    }
    @Override
    protected void start() {
      log("--- Starting Lifecycle ---");
      super.start();
      log("--- Started Lifecycle ---");
    }
  }

  /**
   * Repository that can output more verbose messages.
   */
  private class VerboseRepository extends ComponentRepository {
    @Override
    protected void registered(Object registeredKey, Object registeredObject) {
      if (registeredKey instanceof ComponentInfo) {
        ComponentInfo info = (ComponentInfo) registeredKey;
        if (info.getAttributes().isEmpty()) {
          log(" Registered component: " + info.toComponentKey());
        } else {
          log(" Registered component: " + info.toComponentKey() + " " + info.getAttributes());
        }
      } else if (registeredKey instanceof ComponentKey) {
        log(" Registered component: " + registeredKey);
      } else {
        log(" Registered callback: " + registeredObject);
      }
      super.registered(registeredKey, registeredObject);
    }
  }

}
