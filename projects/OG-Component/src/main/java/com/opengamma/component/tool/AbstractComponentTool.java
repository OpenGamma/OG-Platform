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

import com.opengamma.component.factory.RemoteComponentFactory;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.LogUtils;
import com.opengamma.util.StartupUtils;

/**
 * Abstract base class for tools which operate on components obtained through an OpenGamma component server.
 * 
 * @deprecated Use {@link AbstractTool}
 */
@Deprecated
public abstract class AbstractComponentTool {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractComponentTool.class);
  /**
   * Default logback file.
   */
  public static final String TOOL_LOGBACK_XML = "tool-logback.xml";

  /** Help command line option. */
  private static final String HELP_OPTION = "h";
  /** Component server URI command line option. */
  private static final String COMPONENT_SERVER_URI_OPTION = "c";
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
   * The remote component factory.
   */
  private RemoteComponentFactory _remoteComponentFactory;

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
    return logbackResource.equals(getSystemDefaultLogbackConfiguration()) ? true : LogUtils.configureLogger(logbackResource);
  }

  /**
   * Creates an instance.
   */
  protected AbstractComponentTool() {
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
   * c/component server URI - the component server URI, mandatory<br />
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
   * c/component server URI - the component server URI, mandatory<br />
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
    String componentServerUri = line.getOptionValue(COMPONENT_SERVER_URI_OPTION);
    return init(logbackResource) && run(componentServerUri);
  }

  /**
   * Runs the tool.
   * <p>
   * This starts the tool context and calls {@link #run(ToolContext)}. This will catch exceptions and print a stack trace.
   * 
   * @param componentServerUri the config resource location, not null
   * @return true if successful
   */
  public final boolean run(String componentServerUri) {
    try {
      ArgumentChecker.notNull(componentServerUri, "componentServerUri");
      s_logger.info("Starting " + getClass().getSimpleName());
      componentServerUri = resolveComponentServerUri(componentServerUri);
      RemoteComponentFactory remoteComponentFactory = new RemoteComponentFactory(componentServerUri);
      s_logger.info("Running " + getClass().getSimpleName());
      run(remoteComponentFactory);
      s_logger.info("Finished " + getClass().getSimpleName());
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }
  
  /**
   * Runs the tool, calling {@code doRun}.
   * <p>
   * This will catch unhandled exceptions, and will convert checked exceptions to unchecked.
   * 
   * @param remoteComponentFactory  the remote component factory, not null
   * @throws RuntimeException if an error occurs
   */
  public final void run(RemoteComponentFactory remoteComponentFactory) {
    _remoteComponentFactory = remoteComponentFactory;
    try {
      doRun();
    } catch (RuntimeException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Override in subclasses to implement the tool.
   * 
   * @throws Exception if an error occurs
   */
  protected abstract void doRun() throws Exception;

  //-------------------------------------------------------------------------
  /**
   * Gets the remote component factory.
   * 
   * @return the remote component factory, not null during {@code doRun}
   */
  protected RemoteComponentFactory getRemoteComponentFactory() {
    return _remoteComponentFactory;
  }

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
   * Subclasses may override this and add their own parameters. The base class defined the options h/help, c/config, l/logback.
   * 
   * @return the set of command line options, not null
   */
  protected Options createOptions() {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createComponentServerOption());
    options.addOption(createLogbackOption());
    return options;
  }

  private static Option createHelpOption() {
    return new Option(HELP_OPTION, "help", false, "prints this message");
  }

  private static Option createComponentServerOption() {
    Option option = new Option(COMPONENT_SERVER_URI_OPTION, "componentServer", true, "the component server, host[:port]");
    option.setArgName("component server");
    option.setRequired(true);
    return option;
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

  private void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + getEntryPointClass().getName(), options, true);
  }

  //-------------------------------------------------------------------------
  private String resolveComponentServerUri(String componentServerUri) {
    componentServerUri = componentServerUri.trim();
    if (componentServerUri.contains("/")) {
      // Assume it's the full URI
      return componentServerUri;
    } else {
      // Assume it's host[:port]
      return "http://" + componentServerUri + "/jax";
    }
  }

}
