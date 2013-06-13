/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EnumUtils;

/**
 * View status reporter options
 */
public final class ViewStatusReporterOption {
  
  private static final String DEFAULT_TARGET_TYPE = "position";
  private static final String DEFAULT_FORMAT = "html";
  
  private static final List<String> SUPPORTED_TARGET_TYPES = Lists.newArrayList("position");
  private static final List<String> SUPPORTED_FORMAT = Lists.newArrayList("html");
  /** 
   * Portfolio name option flag 
   */
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** 
   * Computation target type 
   */
  private static final String TARGET_TYPE_OPT = "t";
  /**
   * Result format type
   */
  private static final String FORMAT_TYPE_OPT = "fm";
  
  
  private final String _portfolioName;
  
  private final String _computationTargetType;
  
  private final String _format;
  
  private ViewStatusReporterOption(final String portfolioName, String computationTargetType, String format) {
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    _portfolioName = portfolioName;
    
    computationTargetType = computationTargetType.toLowerCase();
    if (!SUPPORTED_TARGET_TYPES.contains(computationTargetType)) {
      throw new OpenGammaRuntimeException("Unsupported target type: " + computationTargetType);
    }
    _computationTargetType = computationTargetType;
    
    format = format.toLowerCase();
    if (!SUPPORTED_FORMAT.contains(format)) {
      throw new OpenGammaRuntimeException("Unsupported format type: " + format);
    }
    _format = format;
  }
  
  public static Options createOptions() {
    
    Options options = new Options();
    
    Option portfolioNameOption = new Option(PORTFOLIO_NAME_OPT, "name", true, "the name of the source OpenGamma portfolio");
    portfolioNameOption.setArgName("portfolioName");
    portfolioNameOption.setRequired(true);
   
    Option targetTypeOption = new Option(TARGET_TYPE_OPT, "target", true, "the computation target type");
    targetTypeOption.setArgName("position, portfolio_node");
    
    Option formatTypeOption = new Option(FORMAT_TYPE_OPT, "format", true, "the format of status result, default is html");
    formatTypeOption.setArgName("csv, xml, html");
    
    options.addOption(portfolioNameOption);
    options.addOption(targetTypeOption);
    options.addOption(formatTypeOption);
    
    return options;
  }
  
  public static ViewStatusReporterOption getViewStatusReporterOption(final CommandLine commandLine) {
    ArgumentChecker.notNull(commandLine, "commandLine");
    
    String portfolioName = StringUtils.trimToNull(commandLine.getOptionValue(PORTFOLIO_NAME_OPT));
    String computationTargetType = StringUtils.trimToNull(commandLine.getOptionValue(TARGET_TYPE_OPT));
    computationTargetType = StringUtils.defaultString(computationTargetType, DEFAULT_TARGET_TYPE);
    
    String format = StringUtils.trimToNull(commandLine.getOptionValue(FORMAT_TYPE_OPT));
    format = StringUtils.defaultString(format, DEFAULT_FORMAT);
    
    return new ViewStatusReporterOption(portfolioName, computationTargetType, format);
  }

  /**
   * Gets the portfolioName.
   * @return the portfolioName
   */
  public String getPortfolioName() {
    return _portfolioName;
  }


  /**
   * Gets the computationTargetType.
   * @return the computationTargetType
   */
  public String getComputationTargetType() {
    return _computationTargetType;
  }
  
  /**
   * Gets the format.
   * @return the format
   */
  public String getFormat() {
    return _format;
  }
  
  static enum TargetType {
    /**
     * Position
     */
    POSITION,
    /**
     * Portfolio Node
     */
    PORTFOLIO_NODE;
    
    private static Map<TargetType, ComputationTargetType> s_typeMapping = Maps.newHashMap();
    static {
      s_typeMapping.put(POSITION, ComputationTargetType.POSITION);
      s_typeMapping.put(PORTFOLIO_NODE, ComputationTargetType.PORTFOLIO_NODE);
    }

    public static TargetType of(String targetTypeStr) {
      targetTypeStr = StringUtils.trimToNull(targetTypeStr);
      if (targetTypeStr != null) {
        return EnumUtils.safeValueOf(TargetType.class, targetTypeStr.toUpperCase());
      }
      return null;
    }
    
    public static ComputationTargetType toEngineType(TargetType type) {
      ComputationTargetType targetType = s_typeMapping.get(type);
      if (targetType == null) {
        throw new OpenGammaRuntimeException("Unsupported target type: " + type.name());
      }
      return targetType;
    }
  }
  
  static enum ResultFormat {
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
