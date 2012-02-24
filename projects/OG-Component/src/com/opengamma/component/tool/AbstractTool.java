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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;

import com.opengamma.component.factory.tool.ToolContextUtils;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract class for tools that sets up a tool context.
 */
public abstract class AbstractTool {
  
  private static final char HELP_OPTION = 'h';
  private static final char CONFIG_RESOURCE_OPTION = 'c';
  private static final char LOGBACK_RESOURCE_OPTION = 'l';

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
   * @param logbackResource  the logback resource location, not null
   * @return true if successful
   */
  public static final boolean init(String logbackResource) {
    try {
      ArgumentChecker.notNull(logbackResource, "logbackResource");
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      URL logbackResourceUrl = ClassLoader.getSystemResource(logbackResource);
      configurator.doConfigure(logbackResourceUrl);
      return true;
    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Runs the tool.
   * This starts the tool context and calls {@link #run(ToolContext)}.
   * This will catch exceptions and print a stack trace.
   *
   * @param configResource  the config resource location, not null
   * @return true if successful
   */
  public final boolean run(String configResource) {
    try {
      ArgumentChecker.notNull(configResource, "configResourceLocation");
      System.out.println("Starting " + getClass().getSimpleName());
      ToolContext toolContext = ToolContextUtils.getToolContext(configResource);
      System.out.println("Running " + getClass().getSimpleName());
      run(toolContext);
      System.out.println("Finished " + getClass().getSimpleName());
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
   * This will catch not handle exceptions.
   * 
   * @param toolContext  the tool context, not null
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

  /**
   * Override in subclasses to implement the tool.
   * 
   * @throws Exception if an error occurs
   */
  protected abstract void doRun() throws Exception;

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
   * Initializes and runs the tool from standard command-line arguments.
   * 
   * @param args  the command-line arguments, not null
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(String[] args) {
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
    try {
      String logbackResource = line.getOptionValue(LOGBACK_RESOURCE_OPTION);
      if (!AbstractTool.init(logbackResource)) {
        return false;
      }
      String configResource = line.getOptionValue(CONFIG_RESOURCE_OPTION);
      return run(configResource);
    } catch (Exception e) {
      return false;
    }
  }

  protected Options createOptions() {
    Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createConfigOption());
    options.addOption(createLogbackOption());
    return options;
  }
  
  private static Option createHelpOption() {
    OptionBuilder.withLongOpt("help");
    OptionBuilder.withDescription("prints this message");
    return OptionBuilder.create(HELP_OPTION);
  }
  
  private static Option createConfigOption() {
    OptionBuilder.withLongOpt("config");
    OptionBuilder.withDescription("the tool configuration resource");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(CONFIG_RESOURCE_OPTION);
  }
  
  private static Option createLogbackOption() {
    OptionBuilder.withLongOpt("logback");
    OptionBuilder.withDescription("the logback configuration resource");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(LOGBACK_RESOURCE_OPTION);
  }
  
  private void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + getClass().getName(), options, true);
  }

}
