/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.LogUtils;
import com.opengamma.util.StartupUtils;

/**
 * Abstract class for command line tools which do not require access to a ToolContext
 */
public abstract class AbstractToolWithoutContext {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractToolWithoutContext.class);
  /**
   * Default logback file.
   */
  public static final String TOOL_LOGBACK_XML = "tool-logback.xml";

  /** Help command line option. */
  private static final String HELP_OPTION = "h";
  /** Logging command line option. */
  private static final String LOGBACK_RESOURCE_OPTION = "l";
  
  static {
    StartupUtils.init();
  }

  /**
   * The command line.
   */
  private CommandLine _commandLine;

  /**
   * Initializes the tool statically.
   * 
   * @param logbackResource the logback resource location, not null
   * @return true if successful
   */
  public static final boolean init(final String logbackResource) {
    s_logger.debug("Configuring logging from {}", logbackResource);
    // Don't reconfigure if already configured from the default property or any existing loggers will break
    // and stop reporting anything.
    return logbackResource.equals(getSystemDefaultLogbackConfiguration()) || LogUtils.configureLogger(logbackResource);
  }

  /**
   * Creates an instance.
   */
  protected AbstractToolWithoutContext() {
  }

  //-------------------------------------------------------------------------

  protected static String getSystemDefaultLogbackConfiguration() {
    return System.getProperty("logback.configurationFile");
  }

  /**
   * Returns the name of the default logback configuration file if none is explicitly specified. This will be {@link #TOOL_LOGBACK_XML} unless the global {@code logback.configurationFile property} has
   * been set.
   * 
   * @return the logback configuration file resource address, not null
   */
  protected String getDefaultLogbackConfiguration() {
    final String globalConfiguration = getSystemDefaultLogbackConfiguration();
    if (globalConfiguration != null) {
      return globalConfiguration;
    } else {
      return TOOL_LOGBACK_XML;
    }
  }

  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * The base class defined three options:<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * 
   * @param args the command-line arguments, not null
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(String[] args) {
    return initAndRun(args, null);
  }

  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * The base class defined three options:<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * 
   * @param args the command-line arguments, not null
   * @param defaultLogbackResource the default logback resource, null to use tool-logback.xml as the default
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(String[] args, String defaultLogbackResource) {
    ArgumentChecker.notNull(args, "args");

    Options options = createOptions();
    CommandLineParser parser = new PosixParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (ParseException e) {
      usage(options);
      return false;
    }
    _commandLine = line;
    if (line.hasOption(HELP_OPTION)) {
      usage(options);
      return true;
    }
    String logbackResource = line.getOptionValue(LOGBACK_RESOURCE_OPTION);
    logbackResource = StringUtils.defaultIfEmpty(logbackResource, getDefaultLogbackConfiguration());

    return init(logbackResource) && run();
  }

  /**
   * Runs the tool, calling {@code doRun}.
   * 
   * @return true if successful
   */
  public final boolean run() {
    try {
      s_logger.info("Running " + getClass().getSimpleName());
      doRun();
      s_logger.info("Finished " + getClass().getSimpleName());
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Override in subclasses to implement the tool.
   * 
   * @throws Exception if an error occurs
   */
  protected abstract void doRun() throws Exception;

  /**
   * Gets the parsed command line.
   * 
   * @return the parsed command line, not null after parsing
   */
  protected CommandLine getCommandLine() {
    return _commandLine;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the command line options.
   * <p>
   * Subclasses may override this and add their own parameters. The base class defined the options h/help, l/logback.
   * 
   * @return the set of command line options, not null
   */
  protected Options createOptions() {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createLogbackOption());
    return options;
  }

  private static Option createHelpOption() {
    return new Option(HELP_OPTION, "help", false, "prints this message");
  }

  private static Option createLogbackOption() {
    Option option = new Option(LOGBACK_RESOURCE_OPTION, "logback", true, "the logback configuration resource");
    option.setArgName("resource");
    option.setRequired(false);
    return option;
  }

  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + getEntryPointClass().getName(), options, true);
  }

}
