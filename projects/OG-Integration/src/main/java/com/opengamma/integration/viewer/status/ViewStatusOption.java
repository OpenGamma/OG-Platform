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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.EnumUtils;

/**
 * View status command line options
 */
public final class ViewStatusOption {
  
  private static final String DEFAULT_FORMAT = "html";
  
  private static final List<String> SUPPORTED_FORMAT = Lists.newArrayList("html", "csv");
  /**  Portfolio name option flag */
  private static final String PORTFOLIO_NAME_OPT = "n";
  /**  User option flag */
  private static final String USER_OPT = "u";
  /** Result format type flag */
  private static final String FORMAT_TYPE_OPT = "fm";
  /** Aggregation type flag */
  private static final String AGGREGATION_TYPE_OPT = "a";
  /** Output filename flag */
  private static final String OUTPUT_OPT = "o";
  /** Live MarketData flag */
  private static final String LIVE_MARKET_DATA_OPT = "ld";
  /** User snapshot market data flag */
  private static final String USER_MARKET_DATA_OPT = "ud";
  /** Historical market data flag */
  private static final String HISTORICAL_MARKET_DATA_OPT = "hd";
  /**
   * Default output name
   */
  public static final String DEFAULT_OUTPUT_NAME = "view-status";
  /**
   * Default user
   */
  private static final UserPrincipal DEFAULT_USER = UserPrincipal.getLocalUser();
  
  private static final Pattern USER_OR_HISTORICAL_PATTERN = Pattern.compile("^(.+)/(.+)$");
  
  private final String _portfolioName;
  
  private final ResultFormat _format;
  
  private final UserPrincipal _user;
  
  private final AggregateType _aggregateType;
  
  private final File _outputFile;
  
  private final MarketDataSpecification _marketDataSpecification;
    
  private ViewStatusOption(final String portfolioName, final String formatOption, final UserPrincipal user, 
      final AggregateType aggregateType, final File outputFile, final MarketDataSpecification marketDataSpecification) {
    
    ArgumentChecker.notNull(portfolioName, "portfolioName");
    ArgumentChecker.notNull(user, "user");
    ArgumentChecker.notNull(formatOption, "formatOption");
    ArgumentChecker.notNull(aggregateType, "aggregateType");
    ArgumentChecker.notNull(outputFile, "outputFile");
    ArgumentChecker.notNull(marketDataSpecification, "marketDataSpecification");
    
    _portfolioName = portfolioName;
    validateFormat(formatOption);
    _format = ResultFormat.of(formatOption);
    _user = user;
    _aggregateType = aggregateType;
    _outputFile = outputFile;
    _marketDataSpecification = marketDataSpecification;
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
   
    Option userOption = new Option(USER_OPT, "user", true, "the username/ipaddress for computing views");
    userOption.setArgName("username/ipaddress");
        
    Option formatTypeOption = new Option(FORMAT_TYPE_OPT, "format", true, "the format of status result, default is html");
    formatTypeOption.setArgName("csv, xml, html");
    
    Option aggregationTypeOption = new Option(AGGREGATION_TYPE_OPT, "aggregate", true, "the aggregation type of result, default is no-aggregation");
    aggregationTypeOption.setArgName("TSVC, CSVT");
    
    Option outputOption = new Option(OUTPUT_OPT, "output", true, "the output filename");
    outputOption.setArgName("filePath");
    
    Option liveMarketDataOption = new Option(LIVE_MARKET_DATA_OPT, "live", true, "the live marketdata datasource");
    liveMarketDataOption.setArgName("datasource");
    
    Option userMarketDataOption = new Option(USER_MARKET_DATA_OPT, "snapshot", true, "the user marketdata snapshot name");
    userMarketDataOption.setArgName("snapshot name");
    
    Option historicalMarketDataOption = new Option(HISTORICAL_MARKET_DATA_OPT, "historical", true, "the historical marketdata specification");
    historicalMarketDataOption.setArgName("localdate/htsKey");
    
    options.addOption(portfolioNameOption);
    options.addOption(userOption);
    options.addOption(formatTypeOption);
    options.addOption(aggregationTypeOption);
    options.addOption(outputOption);
    options.addOption(liveMarketDataOption);
    options.addOption(userMarketDataOption);
    options.addOption(historicalMarketDataOption);
    
    return options;
  }
  
  /**
   * Creates a View status option instance from the options supplied from the command line
   * 
   * @param commandLine the command line, not-null
   * @param toolContext the toolcontext to use for resolving userSnapshot name to UniqueId
   * @return the view status option, not-null
   */
  public static ViewStatusOption getViewStatusReporterOption(final CommandLine commandLine, final ToolContext toolContext) {
    ArgumentChecker.notNull(commandLine, "commandLine");
    ArgumentChecker.notNull(toolContext, "toolContext");
    
    String portfolioName = trimToNull(commandLine.getOptionValue(PORTFOLIO_NAME_OPT));
    String userOption = trimToNull(commandLine.getOptionValue(USER_OPT));
    UserPrincipal user = null;
    if (userOption == null) {
      user = DEFAULT_USER;
    } else {
      Matcher matcher = USER_OR_HISTORICAL_PATTERN.matcher(userOption);
      if (matcher.matches()) {
        String username = matcher.group(1);
        String ipaddress = matcher.group(2);
        user = new UserPrincipal(username, ipaddress);
      } else {
        throw new OpenGammaRuntimeException("Given user option [" + userOption + "] does not match expected format username/ipaddress");
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
    
    MarketDataSpecification marketDataSpec = getMarketDataSpecification(commandLine, toolContext);
    return new ViewStatusOption(portfolioName, format, user, aggregateType, outputFile, marketDataSpec);
  }

  private static MarketDataSpecification getMarketDataSpecification(final CommandLine commandLine, final ToolContext toolContext) {
    String marketDataOption = trimToNull(commandLine.getOptionValue(LIVE_MARKET_DATA_OPT));
    if (marketDataOption != null) {
      return LiveMarketDataSpecification.of(marketDataOption);
    }
    String snapshotOption = trimToNull(commandLine.getOptionValue(USER_MARKET_DATA_OPT));
    if (snapshotOption != null) {
      MarketDataSnapshotMaster snapshotMaster = toolContext.getMarketDataSnapshotMaster();
      if (snapshotMaster == null) {
        throw new OpenGammaRuntimeException("MarketDataSnapshotMaster is missing from given Toolcontext");
      }
      MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
      request.setName(snapshotOption);
      MarketDataSnapshotSearchResult searchResult = snapshotMaster.search(request);
      if (searchResult.getDocuments().isEmpty()) {
        throw new OpenGammaRuntimeException("No matching snapshot for given name [" + marketDataOption + "]");
      }
      return UserMarketDataSpecification.of(searchResult.getFirstDocument().getUniqueId());
    }
    String historicalOption = trimToNull(commandLine.getOptionValue(HISTORICAL_MARKET_DATA_OPT));
    if (historicalOption != null) {
      Matcher matcher = USER_OR_HISTORICAL_PATTERN.matcher(historicalOption);
      if (matcher.matches()) {
        String localDateStr = matcher.group(1);
        String htsKey = matcher.group(2);
        LocalDate snapshotDate = null;
        try {
          snapshotDate = LocalDate.parse(localDateStr);
        } catch (DateTimeParseException ex) {
          throw new OpenGammaRuntimeException("Error parsing given snapshot date [" + snapshotDate + "]", ex.getCause());
        }
        return new FixedHistoricalMarketDataSpecification(htsKey, snapshotDate);
      } else {
        throw new OpenGammaRuntimeException("Given historical option [" + historicalOption + "] does not match expected format localdate/htskey");
      }
    }
    return MarketData.live();
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
   * Gets the marketDataSpecification.
   * @return the marketDataSpecification
   */
  public MarketDataSpecification getMarketDataSpecification() {
    return _marketDataSpecification;
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
