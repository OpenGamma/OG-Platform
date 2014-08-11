/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.component.tool;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.LogUtils;

/**
 * Utilities for setting up the infrastructure around tools.
 */
public final class ToolUtils {

  private static final Logger s_logger = LoggerFactory.getLogger(ToolUtils.class);
  
  /**
   * Default logback file.
   */
  private static final String TOOL_LOGBACK_XML = "tool-logback.xml";
  
  /**
   * Hidden constructor.
   */
  private ToolUtils() {
  }
  
  //-------------------------------------------------------------------------
  public static boolean initLogback(String logbackResource) {
    s_logger.trace("Configuring logging from {}", logbackResource);
    // Don't reconfigure if already configured from the default property or any existing loggers will break
    // and stop reporting anything.
    return logbackResource.equals(getSystemDefaultLogbackConfiguration()) ? true : LogUtils.configureLogger(logbackResource);
  }
  
  /**
   * Returns the name of the default logback configuration file if none is explicitly specified. This will be {@link #TOOL_LOGBACK_XML} unless the global {@code logback.configurationFile property} has
   * been set.
   * 
   * @return the logback configuration file resource address, not null
   */
  public static String getDefaultLogbackConfiguration() {
    final String globalConfiguration = getSystemDefaultLogbackConfiguration();
    if (globalConfiguration != null) {
      return globalConfiguration;
    } else {
      return TOOL_LOGBACK_XML;
    }
  }
  
  //-------------------------------------------------------------------------
  private static String getSystemDefaultLogbackConfiguration() {
    return System.getProperty("logback.configurationFile");
  }
  
  /**
   * Adds an option with a default value.
   * 
   * @param options the options object to add to
   * @param shortOpt the short option name
   * @param longOpt the long option
   * @param description the option's description
   * @param defaultValue the default value
   */
  public static void optionWithDefault(Options options, 
                                       String shortOpt, 
                                       String longOpt, 
                                       String description, 
                                       String defaultValue) {
    String completeDescription = description + " [Default = " + defaultValue + "]";
    Option option = new Option(shortOpt, longOpt, true, completeDescription);
    option.setRequired(false);
    options.addOption(option);
  }
  
  /**
   * Adds a required option.
   * 
   * @param options the options object to add to
   * @param shortOpt the short option name
   * @param longOpt the long option
   * @param hasArg whether the option requires an arg
   * @param description the option's description
   */
  public static void option(Options options, 
                            String shortOpt, 
                            String longOpt, 
                            boolean hasArg, 
                            String description) {
    Option option = new Option(shortOpt, longOpt, hasArg, description);
    option.setRequired(true);
    options.addOption(option);
  }

  /**
   * Adds an option which need not be specified.
   * 
   * @param options the options object to add to
   * @param shortOpt the short option name
   * @param longOpt the long option
   * @param hasArg whether the option requires an arg
   * @param description the option's description
   */
  public static void optionalOption(Options options, 
                                    String shortOpt, 
                                    String longOpt, 
                                    boolean hasArg, 
                                    String description) {
    Option option = new Option(shortOpt, longOpt, hasArg, description);
    option.setRequired(false);
    options.addOption(option);
  }

  
}
