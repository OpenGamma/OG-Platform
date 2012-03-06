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
    new OpenGammaComponentServer().run(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server.
   * 
   * @param args  the arguments, not null
   */
  public void run(String[] args) {
    CommandLine cmdLine;
    try {
      cmdLine = (new PosixParser()).parse(OPTIONS, args);
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      usage();
      return;
    }
    
    if (cmdLine.hasOption(HELP_OPTION)) {
      usage();
      return;
    }
    
    int verbosity = 1;
    if (cmdLine.hasOption(VERBOSE_OPTION)) {
      verbosity = 2;
    } else if (cmdLine.hasOption(QUIET_OPTION)) {
      verbosity = 0;
    }
    
    args = cmdLine.getArgs();
    if (args.length == 0) {
      System.out.println("No config file specified");
      usage();
      return;
    }
    if (args.length > 1) {
      System.out.println("Only one config file can be specified");
      usage();
      return;
    }
    String configFile = args[0];
    
    run(verbosity, configFile);
  }

  //-------------------------------------------------------------------------
  /**
   * Runs the server with config file.
   * 
   * @param verbosity  the verbosity (0 quiet, 1 normal, 2 verbose)
   * @param configFile  the config file, not null
   */
  protected void run(int verbosity, String configFile) {
    long start = System.nanoTime();
    if (verbosity > 0) {
      System.out.println("======== STARTING OPENGAMMA ========");
      if (verbosity > 1) {
        System.out.println(" Config file: " + configFile);
      }
    }
    
    // extract the server name from the file name
    String serverName = extractServerName(configFile);
    System.setProperty(OPENGAMMA_SERVER_NAME, serverName);
    
    // create the manager
    ComponentManager manager = createManager(serverName, verbosity);
    
    // start server
    try {
      OpenGammaComponentServerMonitor.create(manager.getRepository());
      manager.start(configFile);
      
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      System.out.println("======== OPENGAMMA STARTUP FAILED ========");
      System.exit(1);
    }
    
    if (verbosity > 0) {
      long end = System.nanoTime();
      System.out.println("======== OPENGAMMA STARTED in " + ((end - start) / 1000000) + "ms ========");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the server name.
   * 
   * @param fileName  the name to extract from, not null
   * @return the server name, not null
   */
  protected String extractServerName(String fileName) {
    if (fileName.contains(":")) {
      fileName = StringUtils.substringAfter(fileName, ":");
    }
    fileName = FilenameUtils.removeExtension(fileName);
    fileName = FilenameUtils.getPathNoEndSeparator(fileName);
    String name = FilenameUtils.getName(fileName);
    if (name.length() == 0) {
      name = StringUtils.substringBefore(fileName, "-");
    }
    return name;
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
  private static class VerboseManager extends ComponentManager {
    public VerboseManager(String serverName) {
      super(serverName);
    }
    public VerboseManager(String serverName, ComponentRepository repo) {
      super(serverName, repo);
    }
    @Override
    public ComponentRepository start(Resource resource) {
      try {
        System.out.println("  Using file: " + resource.getURI());
      } catch (IOException ex) {
        try {
          System.out.println("  Using file: " + resource.getFile());
        } catch (IOException ex2) {
          System.out.println("  Using file: " + resource);
        }
      }
      return super.start(resource);
    }
    @Override
    protected void loadIni(Resource resource) {
      System.out.println("--- Using merged properties ---");
      Map<String, String> properties = new TreeMap<String, String>(getProperties());
      for (String key : properties.keySet()) {
        if (key.contains("password")) {
          System.out.println(" " + key + " = " + StringUtils.repeat("*", properties.get(key).length()));
        } else {
          System.out.println(" " + key + " = " + properties.get(key));
        }
      }
      super.loadIni(resource);
    }
    @Override
    protected void initComponent(String groupName, LinkedHashMap<String, String> groupData) {
      long startInstant = System.nanoTime();
      System.out.println("--- Initializing " + groupName + " ---");
      
      super.initComponent(groupName, groupData);
      
      long endInstant = System.nanoTime();
      System.out.println("--- Initialized " + groupName + " in " + ((endInstant - startInstant) / 1000000L) + "ms ---");
    }
    @Override
    protected void start() {
      System.out.println("--- Starting Lifecycle ---");
      super.start();
      System.out.println("--- Started Lifecycle ---");
    }
  }

  /**
   * Repository that can output more verbose messages.
   */
  private static class VerboseRepository extends ComponentRepository {
    @Override
    protected void registered(Object registeredKey, Object registeredObject) {
      if (registeredKey instanceof ComponentInfo) {
        ComponentInfo info = (ComponentInfo) registeredKey;
        if (info.getAttributes().isEmpty()) {
          System.out.println(" Registered component: " + info.toComponentKey());
        } else {
          System.out.println(" Registered component: " + info.toComponentKey() + " " + info.getAttributes());
        }
      } else if (registeredKey instanceof ComponentKey) {
        System.out.println(" Registered component: " + registeredKey);
      } else {
        System.out.println(" Registered callback: " + registeredObject);
      }
      super.registered(registeredKey, registeredObject);
    }
  }

}
