/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.ComponentManager;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ShutdownUtils;
import com.opengamma.util.StartupUtils;

/**
 * Abstract class for command line tools.
 * <p>
 * The command line tools generally require access to key parts of the infrastructure.
 * These are provided via {@link ToolContext} which is setup and closed by this class
 * using {@link ComponentManager}. Normally the file is named {@code toolcontext.ini}.
 *
 * @param <T> the tool context type
 */
public abstract class AbstractTool<T extends ToolContext> {

  /**
   * Logger.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractTool.class);

  /**
   * Help command line option.
   */
  private static final String HELP_OPTION = "h";
  /**
   * Configuration command line option.
   */
  protected static final String CONFIG_RESOURCE_OPTION = "c";
  /**
   * Logging command line option.
   */
  private static final String LOGBACK_RESOURCE_OPTION = "l";
  
  static {
    StartupUtils.init();
  }

  /**
   * The command line.
   */
  private volatile CommandLine _commandLine;
  /**
   * The tool contexts.
   */
  private volatile T[] _toolContexts;

  /**
   * Initializes the tool statically.
   *
   * @param logbackResource the logback resource location, not null
   * @return true if successful
   */
  public static final boolean init(final String logbackResource) {
    return ToolUtils.initLogback(logbackResource);
  }

  /**
   * Creates an instance.
   */
  protected AbstractTool() {
  }

  //-------------------------------------------------------------------------
  /**
   * Main entry point to initialize and run the tool from standard command-line
   * arguments, terminating the JVM once complete.
   * <p>
   * This base class defines three options:<br />
   * c/config - the config file, mandatory<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * <p>
   * This method is intended for use from a standalone main method.
   * It will print exceptions to system err and terminate the JVM.
   * This method never returns.
   * <p>
   * This method calculates the {@code ToolContext} type by reflection of generics.
   *
   * @param args  the command-line arguments, not null
   */
  public void invokeAndTerminate(final String[] args) {
    invokeAndTerminate(args, null, null);
  }

  /**
   * Main entry point to initialize and run the tool from standard command-line
   * arguments, terminating the JVM once complete.
   * <p>
   * This base class defines three options:<br />
   * c/config - the config file, mandatory<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   * <p>
   * This method is intended for use from a standalone main method.
   * It will print exceptions to system err and terminate the JVM.
   * This method never returns.
   * <p>
   * This method calculates the {@code ToolContext} type by reflection of generics.
   *
   * @param args  the command-line arguments, not null
   * @param defaultConfigResource  the default configuration resource location, null if mandatory on command line
   * @param defaultLogbackResource  the default logback resource, null to use tool-logback.xml as the default
   */
  public void invokeAndTerminate(final String[] args, final String defaultConfigResource, final String defaultLogbackResource) {
    try {
      // reflection to find the tool context type via reflection
      Class<?> cls = getClass();
      ParameterizedType type = null;
      while (cls != AbstractTool.class) {
        Type loop = cls.getGenericSuperclass();
        if (loop instanceof ParameterizedType) {
          type = (ParameterizedType) loop;
          break;
        }
        cls = cls.getSuperclass();
      }
      if (type == null || type.getActualTypeArguments().length != 1 ||
          type.getActualTypeArguments()[0] instanceof Class == false ||
          ToolContext.class.isAssignableFrom((Class<?>) type.getActualTypeArguments()[0]) == false) {
        System.err.println("Subclass must declare tool context type");
        ShutdownUtils.exit(-2);
      }
      @SuppressWarnings("unchecked")
      Class<T> toolContextClass = (Class<T>) type.getActualTypeArguments()[0];
      // invoke and terminate the tool
      boolean success = initAndRun(args, defaultConfigResource, defaultLogbackResource, toolContextClass);
      ShutdownUtils.exit(success ? 0 : -1);
      
    } catch (Throwable ex) {
      ex.printStackTrace();
      ShutdownUtils.exit(-2);
    }
  }

  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * This base class defines three options:<br />
   * c/config - the config file, mandatory<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   *
   * @param args  the command-line arguments, not null
   * @param toolContextClass  the type of tool context to create, should match the generic type argument
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(final String[] args, final Class<? extends T> toolContextClass) {
    return initAndRun(args, null, null, toolContextClass);
  }

  /**
   * Initializes and runs the tool from standard command-line arguments.
   * <p>
   * This base class defines three options:<br />
   * c/config - the config file, mandatory unless default specified<br />
   * l/logback - the logback configuration, default tool-logback.xml<br />
   * h/help - prints the help tool<br />
   *
   * @param args  the command-line arguments, not null
   * @param defaultConfigResource  the default configuration resource location, null if mandatory on command line
   * @param defaultLogbackResource  the default logback resource, null to use tool-logback.xml as the default
   * @param toolContextClass  the type of tool context to create, should match the generic type argument
   * @return true if successful, false otherwise
   */
  public boolean initAndRun(final String[] args, final String defaultConfigResource, final String defaultLogbackResource,
                            final Class<? extends T> toolContextClass) {
    ArgumentChecker.notNull(args, "args");

    final Options options = createOptions(defaultConfigResource == null);
    final CommandLineParser parser = new PosixParser();
    CommandLine line;
    try {
      line = parser.parse(options, args);
    } catch (final ParseException e) {
      System.err.println(e.getMessage());
      usage(options);
      return false;
    }
    _commandLine = line;
    if (line.hasOption(HELP_OPTION)) {
      usage(options);
      return true;
    }
    String logbackResource = line.getOptionValue(LOGBACK_RESOURCE_OPTION);
    logbackResource = StringUtils.defaultIfEmpty(logbackResource, ToolUtils.getDefaultLogbackConfiguration());
    String[] configResources = line.getOptionValues(CONFIG_RESOURCE_OPTION);
    if (configResources == null || configResources.length == 0) {
      configResources = new String[] {defaultConfigResource};
    }
    return init(logbackResource) && run(configResources, toolContextClass);
  }

  /**
   * Runs the tool.
   * <p>
   * This starts the tool context and calls {@link #run(ToolContext)}. This will catch exceptions and print a stack trace.
   *
   * @param configResource  the config resource location, not null
   * @param toolContextClass  the type of tool context to create, should match the generic type argument
   * @return true if successful
   */
  public final boolean run(final String configResource, final Class<? extends T> toolContextClass) {
    return run(new String[] {configResource}, toolContextClass);
  }

  /**
   * Runs the tool.
   * <p>
   * This starts the tool contexts and calls {@link #run(ToolContexts)}. This will catch exceptions and print a stack trace.
   *
   * @param configResources  the config resource locations for multiple tool contexts, not null
   * @param toolContextClass  the type of tool context to create, should match the generic type argument
   * @return true if successful
   */
  @SuppressWarnings("unchecked")
  public final boolean run(final String[] configResources, final Class<? extends T> toolContextClass) {
    ToolContext[] toolContexts = null;
    try {
      ArgumentChecker.notEmpty(configResources, "configResources");
      s_logger.info("Starting " + getClass().getSimpleName());
      toolContexts = new ToolContext[configResources.length];
      for (int i = 0; i < configResources.length; i++) {
        s_logger.info("Populating tool context " + (i + 1) + " of " + configResources.length + "...");
        toolContexts[i] = ToolContextUtils.getToolContext(configResources[i], toolContextClass);
      }
      s_logger.info("Running " + getClass().getSimpleName());
      run((T[]) toolContexts);
      s_logger.info("Finished " + getClass().getSimpleName());
      return true;
    } catch (final Exception ex) {
      s_logger.error("Caught exception", ex);
      ex.printStackTrace();
      return false;
    } finally {
      if (toolContexts != null) {
        for (final ToolContext toolContext : toolContexts) {
          if (toolContext != null) {
            try {
              toolContext.close();
            } catch (final Exception e) {
              s_logger.error("Caught exception", e);
            }
          }
        }
      }
    }
  }

  /**
   * Runs the tool, calling {@code doRun}.
   * <p>
   * This will catch not handle exceptions, but will convert checked exceptions to unchecked.
   *
   * @param toolContext  the tool context, not null
   * @throws RuntimeException if an error occurs
   */
  @SuppressWarnings("unchecked")
  public final void run(final T toolContext) {
    run((T[]) new ToolContext[] {toolContext});
  }

  /**
   * Runs the tool, calling {@code doRun}.
   * <p>
   * This will catch not handle exceptions, but will convert checked exceptions to unchecked.
   *
   * @param toolContexts  the tool contexts, not null or empty
   * @throws RuntimeException if an error occurs
   */
  public final void run(final T[] toolContexts) {
    _toolContexts = toolContexts;
    try {
      doRun();
    } catch (final RuntimeException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    } finally {
      _toolContexts = null;
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
   * Gets the (first) tool context.
   *
   * @return the context, not null during {@code doRun}
   */
  protected T getToolContext() {
    return getToolContext(0);
  }

   //-------------------------------------------------------------------------
  /**
   * Gets the i-th tool context.
   *
   * @param i  the index of the tool context to retrieve
   * @return the i-th context, not null during {@code doRun}
   */
  protected T getToolContext(final int i) {
    ArgumentChecker.notNegative(i, "ToolContext index");
    if (getToolContexts().length > i) {
      return getToolContexts()[i];
    } else {
      throw new OpenGammaRuntimeException("ToolContext " + i + " does not exist");
    }
  }

   //-------------------------------------------------------------------------
  /**
   * Gets all tool contexts.
   *
   * @return the array of contexts, not null or empty during {@code doRun}
   */
  protected T[] getToolContexts() {
    return _toolContexts;
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
   * @param mandatoryConfigResource  whether the config resource is mandatory
   * @return the set of command line options, not null
   */
  protected Options createOptions(final boolean mandatoryConfigResource) {
    final Options options = new Options();
    options.addOption(createHelpOption());
    options.addOption(createConfigOption(mandatoryConfigResource));
    options.addOption(createLogbackOption());
    return options;
  }

  private static Option createHelpOption() {
    return new Option(HELP_OPTION, "help", false, "prints this message");
  }

  private static Option createConfigOption(final boolean mandatoryConfigResource) {
    final Option option = new Option(CONFIG_RESOURCE_OPTION, "config", true, "the toolcontext configuration resource");
    option.setArgName("resource");
    option.setRequired(mandatoryConfigResource);
    return option;
  }

  private static Option createLogbackOption() {
    final Option option = new Option(LOGBACK_RESOURCE_OPTION, "logback", true, "the logback configuration resource");
    option.setArgName("resource");
    option.setRequired(false);
    return option;
  }

  protected Class<?> getEntryPointClass() {
    return getClass();
  }

  protected void usage(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("java " + getEntryPointClass().getName(), options, true);
  }

}
