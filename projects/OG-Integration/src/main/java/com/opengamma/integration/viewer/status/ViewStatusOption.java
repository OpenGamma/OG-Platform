/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import static org.apache.commons.lang.StringUtils.defaultString;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.io.File;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EnumUtils;

/**
 * View status command line options
 */
public final class ViewStatusOption {
  
  private static final String DEFAULT_FORMAT = "html";
  
  private static final List<String> SUPPORTED_FORMAT = Lists.newArrayList("html", "csv");
  /** 
   * Portfolio name option flag 
   */
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** 
   * Username option flag
   */
  private static final String USERNAME_OPT = "u";
  /**
   * User ip address flag
   */
  private static final String USER_IP_ADDRESS_OPT = "ip";
  /**
   * Result format type flag
   */
  private static final String FORMAT_TYPE_OPT = "fm";
  /**
   * Aggregation type flag
   */
  private static final String AGGREGATION_TYPE_OPT = "a";
  /**
   * Output filename flag
   */
  private static final String OUTPUT_OPT = "o";
  /**
   * Default output name
   */
  private static final String DEFAULT_OUTPUT_NAME = "view-status";
  /**
   * Default user
   */
  private static final UserPrincipal DEFAULT_USER = UserPrincipal.getLocalUser();
  
  private final String _portfolioName;
  
  private final ResultFormat _format;
  
  private final UserPrincipal _user;
  
  private final AggregateType _aggregateType;
  
  private final File _outputFile;
    
  private ViewStatusOption(final String portfolioName, final String formatOption, final UserPrincipal user, 
      final AggregateType aggregateType, final File outputFile) {
    
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(formatOption, "formatOption");
    ArgumentChecker.notNull(aggregateType, "aggregateType");
    ArgumentChecker.notNull(outputFile, "outputFile");
    
    _portfolioName = portfolioName;
    validateFormat(formatOption);
    _format = ResultFormat.of(formatOption);
    _user = user;
    _aggregateType = aggregateType;
    _outputFile = outputFile;
  }

  private void validateFormat(String formatOption) {
    formatOption = formatOption.toLowerCase();
    if (!SUPPORTED_FORMAT.contains(formatOption)) {
      throw new OpenGammaRuntimeException("Unsupported format type: " + formatOption);
    }
  }
  
  /**
   * Creates command line options
   * 
   * @return the command line options, not-null.
   */
  public static Options createOptions() {
    
    Options options = new Options();
    
    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPT, "name", true, "the name of the source OpenGamma portfolio");
    portfolioNameOption.setArgName("portfolioName");
    portfolioNameOption.setRequired(true);
   
    Option usernameOption = new Option(USERNAME_OPT, "username", true, "the username for computing views");
    usernameOption.setArgName("username");
    
    Option ipaddressOption = new Option(USER_IP_ADDRESS_OPT, "ipaddress", true, "the ip address of user for computing views");
    ipaddressOption.setArgName("ipaddress");
    
    Option formatTypeOption = new Option(FORMAT_TYPE_OPT, "format", true, "the format of status result, default is html");
    formatTypeOption.setArgName("csv, xml, html");
    
    Option aggregationTypeOption = new Option(AGGREGATION_TYPE_OPT, "aggregate", true, "the aggregation type of result, default is no-aggregation");
    aggregationTypeOption.setArgName("TSVC, CSVT");
    
    Option outputOption = new Option(OUTPUT_OPT, "output", true, "the output filename");
    outputOption.setArgName("filePath");
    
    options.addOption(portfolioNameOption);
    options.addOption(usernameOption);
    options.addOption(formatTypeOption);
    options.addOption(ipaddressOption);
    options.addOption(aggregationTypeOption);
    options.addOption(outputOption);
    
    return options;
  }
  
  /**
   * Creates a View status option instance from the options supplied from the command line
   * 
   * @param commandLine the command line, not-null
   * @return the view status option, not-null
   */
  public static ViewStatusOption getViewStatusReporterOption(final CommandLine commandLine) {
    ArgumentChecker.notNull(commandLine, "commandLine");
    
    String portfolioName = trimToNull(commandLine.getOptionValue(PORTFOLIO_NAME_OPT));
    String username = trimToNull(commandLine.getOptionValue(USERNAME_OPT));
    UserPrincipal user = null;
    if (username == null) {
      user = DEFAULT_USER;
    } else {
      String ipaddress = trimToNull(commandLine.getOptionValue(USER_IP_ADDRESS_OPT));
      if (ipaddress == null) {
        user = DEFAULT_USER;
      } else {
        user = new UserPrincipal(username, ipaddress);
      }
    }
    
    String format = defaultString(trimToNull(commandLine.getOptionValue(FORMAT_TYPE_OPT)), DEFAULT_FORMAT);
    
    String aggregationOption = trimToNull(commandLine.getOptionValue(AGGREGATION_TYPE_OPT));
    AggregateType aggregateType = null;
    if (aggregationOption != null) {
      aggregateType = AggregateType.of(aggregationOption);
    } else {
      aggregateType = AggregateType.NO_AGGREGATION;
    }
    
    String outputOption = trimToNull(commandLine.getOptionValue(OUTPUT_OPT));
    File outputFile = null;
    if (outputOption != null) {
      outputFile = new File(outputOption);
    } else {
      outputFile = new File(DEFAULT_OUTPUT_NAME + "." + ResultFormat.of(format).getExtension());
    }
    return new ViewStatusOption(portfolioName, format, user, aggregateType, outputFile);
  }

  /**
   * Gets the portfolioName.
   * @return the portfolioName
   */
  public String getPortfolioName() {
    return _portfolioName;
  }
  
  /**
   * Gets the user.
   * @return the user
   */
  public UserPrincipal getUser() {
    return _user;
  }
    
  /**
   * Gets the format.
   * @return the format
   */
  public ResultFormat getFormat() {
    return _format;
  }
  
  /**
   * Gets the outputFile.
   * @return the outputFile
   */
  public File getOutputFile() {
    return _outputFile;
  }

  /**
   * Gets the aggregate type.
   * @return the aggregation type
   */
  public AggregateType getAggregateType() {
    return _aggregateType;
  }

  /**
   * View result format
   */
  public static enum ResultFormat {
    /**
     * CSV
     */
    CSV("csv"),
    /**
     * XML
     */
    XML("xml"),
    /**
     * HTML
     */
    HTML("html");
    
    private String _extension;
    
    /**
     * Creates an instance.
     * 
     * @param extension  the file suffix, not null
     */
    private ResultFormat(String extension) {
      _extension = extension;
    }
    
    /**
     * Gets the file extension for a format
     * 
     * @return the file extension, not null
     */
    public String getExtension() {
      return _extension;
    }
    
    public static ResultFormat of(String resultFormatStr) {
      resultFormatStr = StringUtils.trimToNull(resultFormatStr);
      if (resultFormatStr != null) {
        return EnumUtils.safeValueOf(ResultFormat.class, resultFormatStr.toUpperCase());
      }
      return null;
    }
  }
  
}
