/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Market Data Source command line interface 
 * <pre>
 * There are 3 supported types, live, historical and snapshot.
 * input formats are
 *  live:dataSourceName dataSourceName is option, if missing MarketData.live() is used
 *  historical:resolverkey:date optional resolverkey and date(format is yyyymmdd), if missing LatestHistoricalMarketDataSpecification is used
 *  snapshot:snapshotName
 * </pre>
 * <p>
 * Order is based on order of options from the command line e.g --dataSource live --dataSource snapshot:test --dataSource historical
 * will build a layered data source of 
 * <p>
 * marketdata live then user snapshot with name = test and then latest historical
 */
public class MarketDataSourceCli {
  
  private static final Logger s_logger = LoggerFactory.getLogger(MarketDataSourceCli.class);
  
  /** Market data source option */
  private static final String MARKET_DATA_SOURCE_OPTION = "dataSource";
 
  private static final Pattern s_dsOptionPattern = Pattern.compile("(live|historical|snapshot)(:(.*))?");
  private static final Pattern s_historicalPattern = Pattern.compile("([^:]+)((:)([0-9]{8}))?");
  /**
   * Market data source command line option definition
   */
  private final Option _option;
  
  public MarketDataSourceCli() {
    final Option option = new Option("ds", MARKET_DATA_SOURCE_OPTION, true, "the market data source name " +
        "format is \nlive:<dataSourceName> or \nhistorical:<resolverkey>~<date> date in yyyymmdd or \nsnapshot:snapshotName");
    option.setArgName("data source");
    _option = option;
  }
  
  /**
   * Gets the option.
   * @return the option
   */
  public Option getOption() {
    return _option;
  }
  
  public List<MarketDataSpecification> getMarketDataSpecs(final CommandLine commandLine, final MarketDataSnapshotMaster mktDataSnapshotMaster) {
    ArgumentChecker.notNull(commandLine, "commandLine");
    ArgumentChecker.notNull(mktDataSnapshotMaster, "mktDataSnapshotMaster");
    
    List<MarketDataSpecification> marketDataSpecs = new ArrayList<>();
    String[] optionValues = commandLine.getOptionValues(MARKET_DATA_SOURCE_OPTION);
    if (optionValues == null) {
      s_logger.info("Missing {} option from command line", MARKET_DATA_SOURCE_OPTION);
      return marketDataSpecs;
    }
    
    for (String optionValue : optionValues) {
      optionValue = StringUtils.trimToNull(optionValue);
      if (optionValue == null) {
        throw new OpenGammaRuntimeException("Empty market data source option not allowed");
      }
      Matcher optionMatcher = s_dsOptionPattern.matcher(optionValue);
      if (!optionMatcher.matches()) {
        throw new OpenGammaRuntimeException(String.format("Invalid data source option value [%s] in command line option", optionValue));
      }
      String type = optionMatcher.group(1);
      String dataSourceStr = StringUtils.trimToNull(optionMatcher.group(2));
            
      switch (type) {
        case "live":
          if (dataSourceStr == null) {
            marketDataSpecs.add(MarketData.live());
          } else {
            marketDataSpecs.add(LiveMarketDataSpecification.of(optionMatcher.group(3)));
          }
          break;
        case "historical":
          if (dataSourceStr == null) {
            marketDataSpecs.add(new LatestHistoricalMarketDataSpecification());
          } else {
            Matcher historicalMatcher = s_historicalPattern.matcher(optionMatcher.group(3));
            if (!historicalMatcher.matches()) {
              throw new OpenGammaRuntimeException(String.format("Invalid historical data source option value [%s] in command line option", optionValue));
            }
            String resolverKey = historicalMatcher.group(1);
            if (StringUtils.trimToNull(historicalMatcher.group(2)) == null) {
              marketDataSpecs.add(new LatestHistoricalMarketDataSpecification(resolverKey));
            } else {
              String dateStr = StringUtils.trimToNull(historicalMatcher.group(4));
              LocalDate snapshotDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
              marketDataSpecs.add(new FixedHistoricalMarketDataSpecification(resolverKey, snapshotDate));
            }
          }
          break;
        case "snapshot":
          
          if (dataSourceStr == null || StringUtils.trimToNull(optionMatcher.group(3)) == null) {
            throw new OpenGammaRuntimeException(String.format("Invalid historical data source option value [%s] in command line option", optionValue));
          }
          String snapshotName = optionMatcher.group(3);
          UniqueId uniqueId = getSnapshotUniqueId(snapshotName, mktDataSnapshotMaster);
          if (uniqueId == null) {
            s_logger.warn("Snapshot with name {} can not be found", snapshotName);
          } else {
            marketDataSpecs.add(UserMarketDataSpecification.of(uniqueId));
          }
          
          break;
        default:
          throw new OpenGammaRuntimeException(String.format("Unsupported market data source type [%s] in command line option", type));
      }
    }
    return marketDataSpecs;
  }

  private UniqueId getSnapshotUniqueId(String snapshotName, MarketDataSnapshotMaster mktDataSnapshotMaster) {
    MarketDataSnapshotSearchRequest request = new MarketDataSnapshotSearchRequest();
    request.setName(snapshotName);
    MarketDataSnapshotSearchResult snapshotSearchResult = mktDataSnapshotMaster.search(request);
    MarketDataSnapshotDocument snapshotDoc = Iterables.getFirst(snapshotSearchResult.getDocuments(), null);
    if (snapshotDoc != null) {
      return snapshotDoc.getUniqueId();
    }
    return null;
  }

}
