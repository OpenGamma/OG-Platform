/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.net.URL;

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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.component.ComponentManager;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for command line tools.
 * <p>
 * The command line tools generally require access to key parts of the infrastructure. These are provided via {@link ToolContext} which is setup and closed by this class using {@link ComponentManager}
 * . Normally the file is named {@code toolcontext.ini}.
 */
public abstract class AbstractTool {
  
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTool.class);
  /**
   * Default logback file.
   */
  public static final String TOOL_LOGBACK_XML = "tool-logback.xml";

  /** Help command line option. */
  private static final String HELP_OPTION = "h";
  /** Configuration command line option. */
  private static final String CONFIG_RESOURCE_OPTION = "c";
  /** Logging command line option. */
  private static final String LOGBACK_RESOURCE_OPTION = "l";

  /**
   * The command line.
   */
  private CommandLine _commandLine;
  /**
   * The tool context.
   */
  private ToolContext _toolContext;

  /**
   * Initializes the tool statically.
   * 
   * @param logbackResource the logback resource location, not null
   * @return true if successful
   */
  public static final boolean init(String logbackResource) {
    try {
      ArgumentChecker.notNull(logbackResource, "logbackResource");
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      URL logbackResourceUrl = AbstractTool.class.getClassLoader().getResource(logbackResource);
      if (logbackResourceUrl == null) {
        throw new IllegalArgumentException("Logback file not found: " + logbackResource);
      }
      configurator.doConfigure(logbackResourceUrl);
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Creates an instance.
   */
  protected AbstractTool() {
  }

  //-------------------------------------------------------------------------
  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * The base class defined three options:<br />
   * c/config - the config file, mandatory<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * 
   * @param args the command-line arguments, not null
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(String[] args) {
    return initAndRun(args, null, null);
  }

  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * The base class defined three options:<br />
   * c/config - the config file, mandatory unless default specified<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * 
   * @param args the command-line arguments, not null
   * @param defaultConfigResource the default configuration resource location, null if mandatory on command line
   * @param defaultLogbackResource the default logback resource, null to use tool-logback.xml as the default
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(String[] args, String defaultConfigResource, String defaultLogbackResource) {
    ArgumentChecker.notNull(args, "args");

    Options options = createOptions(defaultConfigResource == null);
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
    logbackResource = StringUtils.defaultIfEmpty(logbackResource, TOOL_LOGBACK_XML);
    String configResource = line.getOptionValue(CONFIG_RESOURCE_OPTION);
    configResource = StringUtils.defaultString(configResource, defaultConfigResource);
    return init(logbackResource) && run(configResource);
  }

  /**
   * Runs the tool.
   * <p>
   * This starts the tool context and calls {@link #run(ToolContext)}. This will catch exceptions and print a stack trace.
   * 
   * @param configResource the config resource location, not null
   * @return true if successful
   */
  public final boolean run(String configResource) {
    try {
      ArgumentChecker.notNull(configResource, "configResourceLocation");
      s_logger.info("Starting " + getClass().getSimpleName());
      ToolContext toolContext = ToolContextUtils.getToolContext(configResource);
      s_logger.info("Running " + getClass().getSimpleName());
      run(toolContext);
      s_logger.info("Finished " + getClass().getSimpleName());
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    } finally {
      if (_toolContext != null) {
        _toolContext.close();
      }
    }
  }

  /**
   * Runs the tool, calling {@code doRun}.
   * <p>
   * This will catch not handle exceptions, but will convert checked exceptions to unchecked.
   * 
   * @param toolContext the tool context, not null
   * @throws RuntimeException if an error occurs
   */
  public final void run(ToolContext toolContext) {
    _toolContext = toolContext;
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
   * Gets the tool context.
   * 
   * @return the context, not null during {@code doRun}
   */
  protected ToolContext getToolContext() {
    return _toolContext;
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
   * @param mandatoryConfigResource whether the config resource is mandatory
   * @return the set of command line options, not null
   */
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createConfigOption(mandatoryConfigResource));
    options.addOption(createLogbackOption());
    return options;
  }

  private static Option createHelpOption() {
    return new Option(HELP_OPTION, "help", false, "prints this message");
  }

  private static Option createConfigOption(boolean mandatoryConfigResource) {
    Option option = new Option(CONFIG_RESOURCE_OPTION, "config", true, "the toolcontext configuration resource");
    option.setArgName("resource");
    option.setRequired(mandatoryConfigResource);
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

}
