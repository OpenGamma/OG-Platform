/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.LinkedHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * Main entry point for OpenGamma component-based servers.
 * <p>
 * This class starts an OpenGamma JVM process using the specified config file.
 * The standard config file is a simple format based on INI files.
 */
public class OpenGammaComponentServer {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(OpenGammaComponentServer.class);
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
  protected void run(String[] args) {
    CommandLine cmdLine;
    try {
      cmdLine = (new PosixParser()).parse(OPTIONS, args);
    } catch (ParseException ex) {
      System.out.println(ex.getMessage());
      s_logger.warn(ex.getMessage());
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
      System.out.println("======== STARTING OPEN GAMMA ========");
      if (verbosity > 1) {
        System.out.println(" Config file: " + configFile);
      }
    }
    ComponentManager manager = (verbosity == 2 ? new VerboseManager() : new ComponentManager());
    FileSystemResource resource = new FileSystemResource(configFile);
    manager.start(resource);
    
    if (verbosity > 0) {
      long end = System.nanoTime();
      System.out.println("======== OPEN GAMMA STARTED in " + ((end - start) / 1000000) + "ms ========");
    }
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

}
