/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.BloombergFields;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergTickerParserEQOption;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * A portfolio loader which generates a sensible portfolio of liquid equities and options on them Also see DemoEquityOptionPortfolioLoader.
 */
@Scriptable
public class DemoEquityOptionCollarPortfolioLoader extends AbstractTool<IntegrationToolContext> {

  private static final String TOOL_NAME = "Demo Equity Option Portfolio Loader";
  private static final String PORTFOLIO_NAME_OPT = "p";
  private static final String OPTION_DEPTH_OPT = "d";
  private static final String NUM_CONTRACTS_OPT = "n";
  private static final String NUM_INDEX_MEMBERS_OPT = "m";

  private static final String BLOOMBERG_EQUITY_TICKER_SUFFIX = " Equity";

  private static final Logger s_logger = LoggerFactory.getLogger(DemoEquityOptionCollarPortfolioLoader.class);

  private static final Map<String, String> INDEXES_TO_EXCHANGE = getIndexToExchangeMap();
  private static final Set<String> EXCLUDED_SECTORS = Sets.newHashSet("Financials");

  private static final Period[] MEMBER_OPTION_PERIODS = new Period[] {Period.ofMonths(3), Period.ofMonths(6) };

  private BigDecimal _numContracts;
  private int _numOptions;
  private int _numMembers;

  /**
   * In units of currency
   */
  private static final BigDecimal VALUE_OF_UNDERLYING = BigDecimal.valueOf(100000);

  /**
   * The default genearted portfolio name.
   */
  public static final String PORTFOLIO_NAME = "Equity Option Portfolio";

  private static Map<String, String> getIndexToExchangeMap() {
    final Map<String, String> ret = new HashMap<String, String>();
    ret.put("SPX", "US"); //S&P 500 -> combined US
    // ret.put("IBX", "BZ"); //Sao Paulo Stock Exchange IBrX Index -> combined Brazil
    //ret.put("TW50", "TT"); // FTSE TWSE Taiwan 50 Indx -> Taiwan Stock Exchange
    return ret;
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new DemoEquityOptionCollarPortfolioLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  protected ManageablePortfolio generatePortfolio(final String portfolioName) {
    final ReferenceDataProvider referenceDataProvider = getToolContext().getBloombergReferenceDataProvider();

    final ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName);

    //Is this a hack?
    final ManageablePortfolioNode rootNode = portfolio.getRootNode();
    portfolio.setRootNode(rootNode);

    //    String indexTickerSuffix = " Index";

    final Set<String> memberEquities = new HashSet<String>();

    for (final Entry<String, String> entry : INDEXES_TO_EXCHANGE.entrySet()) {

      final String indexTickerSuffix = " Index";

      final String underlying = entry.getKey();
      final String ticker = underlying + indexTickerSuffix;

      //don't add index (delete at some point)
      //      addNodes(rootNode, ticker, false, INDEX_OPTION_PERIODS);

      final Set<String> indexMembers = BloombergDataUtils.getIndexMembers(referenceDataProvider, ticker);
      for (final String member : indexMembers) {
        final String symbol = getBloombergEquitySymbol(entry.getValue(), member);
        //time series errors for Walmart
        //Todo: investegate & fix
        if ("WMT US Equity".equals(symbol)) {
          continue;
        }
        memberEquities.add(symbol);
      }
    }

    // Sort the symbols for the current index by market cap (highest to lowest), skipping any in the list of EXCLUDED_SECTORS
    final TreeMap<Double, String> equityByMarketCap = new TreeMap<Double, String>();
    final Map<String, FudgeMsg> refDataMap = referenceDataProvider.getReferenceData(memberEquities,
        Sets.newHashSet(BloombergFields.CURRENT_MARKET_CAP_FIELD, BloombergConstants.FIELD_GICS_SUB_INDUSTRY));
    for (final String equity : memberEquities) {
      final FudgeMsg fieldData = refDataMap.get(equity);
      if (fieldData == null) {
        throw new OpenGammaRuntimeException("Information not found for equity: " + equity);
      }
      final String gicsCodeString = fieldData.getString(BloombergConstants.FIELD_GICS_SUB_INDUSTRY);
      if (gicsCodeString == null) {
        continue;
      }
      final GICSCode gicsCode = GICSCode.of(gicsCodeString);
      if (EXCLUDED_SECTORS.contains(gicsCode.getSectorDescription())) {
        continue;
      }
      final Double marketCap = fieldData.getDouble(BloombergFields.CURRENT_MARKET_CAP_FIELD);
      if (marketCap != null) {
        equityByMarketCap.put(marketCap, equity);
      }
    }

    // Add a given number of symbols (MEMBERS_DEPTH) to the portfolio and store in a List
    // When adding to the portfolio, add a collar of options with PVs distributed equally +/- around 0
    int count = 0;
    final List<String> chosenEquities = new ArrayList<String>();
    for (final Entry<Double, String> entry : equityByMarketCap.descendingMap().entrySet()) {
      try {
        addNodes(rootNode, entry.getValue(), true, MEMBER_OPTION_PERIODS);
        chosenEquities.add(entry.getValue());
        if (++count >= _numMembers) {
          break;
        }
      } catch (final RuntimeException e) {
        s_logger.warn("Caught exception", e);
      }
    }
    s_logger.info("Generated collar portfolio for {}", chosenEquities);
    return portfolio;
  }

  private String getBloombergEquitySymbol(final String base, final String member) {
    return member.split(" ")[0] + " " + base + BLOOMBERG_EQUITY_TICKER_SUFFIX;
  }

  private void addNodes(final ManageablePortfolioNode rootNode, final String underlying, final boolean includeUnderlying, final Period[] expiries) {
    final ExternalId ticker = ExternalSchemes.bloombergTickerSecurityId(underlying);
    ManageableSecurity underlyingSecurity = null;
    if (includeUnderlying) {
      underlyingSecurity = getOrLoadEquity(ticker);
    }

    final ExternalIdBundle bundle = underlyingSecurity == null ? ExternalIdBundle.of(ticker) : underlyingSecurity.getExternalIdBundle();
    final HistoricalTimeSeriesInfoDocument timeSeriesInfo = getOrLoadTimeSeries(ticker, bundle);
    final double estimatedCurrentStrike = getOrLoadMostRecentPoint(timeSeriesInfo);
    final Set<ExternalId> optionChain = getOptionChain(ticker);

    //TODO: reuse positions/nodes?
    final String longName = underlyingSecurity == null ? "" : underlyingSecurity.getName();
    final String formattedName = MessageFormatter.format("[{}] {}", underlying, longName).getMessage();
    final ManageablePortfolioNode equityNode = new ManageablePortfolioNode(formattedName);

    final BigDecimal underlyingAmount = VALUE_OF_UNDERLYING.divide(BigDecimal.valueOf(estimatedCurrentStrike), BigDecimal.ROUND_HALF_EVEN);

    if (includeUnderlying) {
      addPosition(equityNode, underlyingAmount, ticker);
    }

    final TreeMap<LocalDate, Set<BloombergTickerParserEQOption>> optionsByExpiry = new TreeMap<LocalDate, Set<BloombergTickerParserEQOption>>();
    for (final ExternalId optionTicker : optionChain) {
      s_logger.debug("Got option {}", optionTicker);

      final BloombergTickerParserEQOption optionInfo = BloombergTickerParserEQOption.getOptionParser(optionTicker);
      s_logger.debug("Got option info {}", optionInfo);

      final LocalDate key = optionInfo.getExpiry();
      Set<BloombergTickerParserEQOption> set = optionsByExpiry.get(key);
      if (set == null) {
        set = new HashSet<BloombergTickerParserEQOption>();
        optionsByExpiry.put(key, set);
      }
      set.add(optionInfo);
    }
    final Set<ExternalId> tickersToLoad = new HashSet<ExternalId>();

    final BigDecimal expiryCount = BigDecimal.valueOf(expiries.length);
    final BigDecimal defaultAmountAtExpiry = underlyingAmount.divide(expiryCount, BigDecimal.ROUND_DOWN);
    final BigDecimal spareAmountAtExpiry = defaultAmountAtExpiry.add(BigDecimal.ONE);
    int spareCount = underlyingAmount.subtract(defaultAmountAtExpiry.multiply(expiryCount)).intValue();

    for (final Period bucketPeriod : expiries) {
      final ManageablePortfolioNode bucketNode = new ManageablePortfolioNode(bucketPeriod.toString().substring(1));

      final LocalDate nowish = LocalDate.now().withDayOfMonth(20); //This avoids us picking different options every time this script is run
      final LocalDate targetExpiry = nowish.plus(bucketPeriod);
      final LocalDate chosenExpiry = optionsByExpiry.floorKey(targetExpiry);
      if (chosenExpiry == null) {
        s_logger.info("No options for {} on {}", targetExpiry, underlying);
        continue;
      }
      s_logger.info("Using time {} for bucket {} ({})", new Object[] {chosenExpiry, bucketPeriod, targetExpiry });

      final Set<BloombergTickerParserEQOption> optionsAtExpiry = optionsByExpiry.get(chosenExpiry);
      final TreeMap<Double, Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> optionsByStrike = new TreeMap<>();
      for (final BloombergTickerParserEQOption option : optionsAtExpiry) {
        //        s_logger.info("option {}", option);
        final double key = option.getStrike();
        Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(key);
        if (pair == null) {
          pair = Pairs.ofNulls();
        }
        if (option.getOptionType() == OptionType.CALL) {
          pair = Pairs.of(option, pair.getSecond());
        } else {
          pair = Pairs.of(pair.getFirst(), option);
        }
        optionsByStrike.put(key, pair);
      }

      //cascading collar?
      final BigDecimal amountAtExpiry = spareCount-- > 0 ? spareAmountAtExpiry : defaultAmountAtExpiry;

      s_logger.info(" est strike {}", estimatedCurrentStrike);
      final Double[] strikes = optionsByStrike.keySet().toArray(new Double[0]);

      int strikeIndex = Arrays.binarySearch(strikes, estimatedCurrentStrike);
      if (strikeIndex < 0) {
        strikeIndex = -(1 + strikeIndex);
      }
      s_logger.info("strikes length {} index {} strike of index {}", new Object[] {Integer.valueOf(strikes.length), Integer.valueOf(strikeIndex), Double.valueOf(strikes[strikeIndex]) });

      int minIndex = strikeIndex - _numOptions;
      minIndex = Math.max(0, minIndex);
      int maxIndex = strikeIndex + _numOptions;
      maxIndex = Math.min(strikes.length - 1, maxIndex);

      s_logger.info("min {} max {}", Integer.valueOf(minIndex), Integer.valueOf(maxIndex));
      final StringBuffer sb = new StringBuffer("strikes: [");
      for (int j = minIndex; j <= maxIndex; j++) {
        sb.append(" ");
        sb.append(strikes[j]);
      }
      sb.append(" ]");
      s_logger.info(sb.toString());

      //Short Calls
      final ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> calls = new ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>>();
      for (int j = minIndex; j < strikeIndex; j++) {
        final Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(strikes[j]);
        if (pair == null) {
          throw new OpenGammaRuntimeException("no pair for strike" + strikes[j]);
        }
        calls.add(pair);
      }
      spreadOptions(bucketNode, calls, OptionType.CALL, -1, tickersToLoad, amountAtExpiry, includeUnderlying, calls.size());

      // Long Puts
      final ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> puts = new ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>>();
      for (int j = strikeIndex + 1; j <= maxIndex; j++) {
        final Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(strikes[j]);
        if (pair == null) {
          throw new OpenGammaRuntimeException("no pair for strike" + strikes[j]);
        }
        puts.add(pair);
      }
      spreadOptions(bucketNode, puts, OptionType.PUT, 1, tickersToLoad, amountAtExpiry, includeUnderlying, puts.size());

      if (bucketNode.getChildNodes().size() + bucketNode.getPositionIds().size() > 0) {
        equityNode.addChildNode(bucketNode); //Avoid generating empty nodes
      }
    }

    for (final ExternalId optionTicker : tickersToLoad) {
      final ManageableSecurity loaded = getOrLoadSecurity(optionTicker);
      if (loaded == null) {
        throw new OpenGammaRuntimeException("Unexpected option type " + loaded);
      }

      //TODO [LAPANA-29] Should be able to do this for index options too
      if (includeUnderlying) {
        try {
          final HistoricalTimeSeriesInfoDocument loadedTs = getOrLoadTimeSeries(optionTicker, loaded.getExternalIdBundle());
          if (loadedTs == null) {
            throw new OpenGammaRuntimeException("Failed to get time series for " + loaded);
          }
        } catch (final Exception ex) {
          s_logger.info("Failed to get time series for " + loaded, ex);
        }
      }
    }

    if (equityNode.getPositionIds().size() + equityNode.getChildNodes().size() > 0) {
      rootNode.addChildNode(equityNode);
    }
  }

  private void spreadOptions(final ManageablePortfolioNode bucketNode, final Collection<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> options, final OptionType type,
      final int scale,
      final Set<ExternalId> tickersToLoad, final BigDecimal underlyingAmount, final boolean includeUnderlying, final int targetNumber) {

    if (targetNumber == 0) {
      return;
    }

    final Collection<BloombergTickerParserEQOption> chosen = new ArrayList<BloombergTickerParserEQOption>();

    int remaining = targetNumber;
    for (final Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair : options) {
      BloombergTickerParserEQOption option;
      if (type == OptionType.PUT) {
        option = pair.getSecond();
      } else {
        option = pair.getFirst();
      }

      //TODO [LAPANA-29] Should be able to do this for index options too
      if (includeUnderlying) {
        try {
          final HistoricalTimeSeriesInfoDocument loadedTs = getOrLoadTimeSeries(option.getIdentifier());
          final HistoricalTimeSeries ts = getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(loadedTs.getUniqueId(), LocalDate.now().minusWeeks(1), true, LocalDate.now(), true);
          if (ts.getTimeSeries().isEmpty()) {
            s_logger.info("No recent time series points for " + option.getIdentifier());
            //   leave in for now
            //          continue; //This option is not liquid enough for us
          }
        } catch (final Exception ex) {
          s_logger.info("Failed to get time series for " + option.getIdentifier(), ex);
          //TODO: stop refetching this series each time
          continue; //This option is not liquid enough for us
        }
      }
      chosen.add(option);
      remaining--;
      if (remaining == 0) {
        break;
      }
    }

    if (chosen.size() == 0) {
      s_logger.warn("Couldn't find any liquid " + type + " options from " + options);
      return; //TODO: should we try another expiry?
    }
    for (final BloombergTickerParserEQOption option : chosen) {
      tickersToLoad.add(option.getIdentifier());
      addPosition(bucketNode, BigDecimal.valueOf(scale).multiply(_numContracts), option.getIdentifier());
    }
  }

  private void addPosition(final ManageablePortfolioNode node, final BigDecimal amount, final ExternalId optionTicker) {
    final ManageablePosition position = new ManageablePosition(amount, optionTicker);

    final LocalDate tradeDate = getRandomTradeDate(optionTicker);
    final ManageableTrade trade = new ManageableTrade(amount, optionTicker, tradeDate, null, ExternalId.of("CPARTY", "BACS"));

    position.addTrade(trade);
    final PositionDocument doc = new PositionDocument(position);
    final PositionDocument added = getToolContext().getPositionMaster().add(doc);
    node.addPosition(added);
  }

  private LocalDate getRandomTradeDate(final ExternalId ticker) {
    final int tradeAge = (int) (3 + (Math.random() * 30));
    final LocalDate tradeDate = LocalDate.now().minusDays(tradeAge);
    //TODO: pick a date for which PX_LAST is known
    return tradeDate;
  }

  private Set<ExternalId> getOptionChain(final ExternalId ticker) {
    if (ticker.getScheme() != ExternalSchemes.BLOOMBERG_TICKER) {
      throw new OpenGammaRuntimeException("Not a bloomberg ticker " + ticker);
    }
    final ReferenceDataProvider referenceDataProvider = getToolContext().getBloombergReferenceDataProvider();

    final Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(referenceDataProvider, ticker.getValue()); //TODO [BBG-88] this query shouldn't get cached permanently
    if (optionChain == null) {
      throw new OpenGammaRuntimeException("Failed to get option chain for " + ticker);
    }
    s_logger.info("Got option chain {}", optionChain);
    return optionChain;
  }

  private double getOrLoadMostRecentPoint(HistoricalTimeSeriesInfoDocument timeSeriesInfo) {
    HistoricalTimeSeries timeSeries = getAllowedRecentPoints(timeSeriesInfo);
    if (timeSeries == null || timeSeries.getTimeSeries().isEmpty()) {
      if (timeSeries == null) {
        timeSeriesInfo = loadTimeSeries(timeSeriesInfo);
      } else if (timeSeries.getTimeSeries().isEmpty()) {
        timeSeries = updateTimeSeries(timeSeries);
      }
      timeSeries = getAllowedRecentPoints(timeSeriesInfo);
      if (timeSeries == null || timeSeries.getTimeSeries().isEmpty()) {
        throw new OpenGammaRuntimeException("Couldn't load recent points for " + timeSeriesInfo);
      }
    }
    final Double latestValue = timeSeries.getTimeSeries().getLatestValue();
    if (latestValue == null) {
      throw new OpenGammaRuntimeException("Unexpected null latest vaule");
    }
    return latestValue;
  }

  private HistoricalTimeSeries getAllowedRecentPoints(final HistoricalTimeSeriesInfoDocument timeSeriesInfo) {
    final LocalDate from = oldestTimeSeriesAllowed();
    final HistoricalTimeSeries timeSeries = getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(timeSeriesInfo.getUniqueId().toLatest(), from, true, LocalDate.now(), true);
    return timeSeries;
  }

  private HistoricalTimeSeriesInfoDocument getOrLoadTimeSeries(final ExternalId ticker) {
    return getOrLoadTimeSeries(ticker, ExternalIdBundle.of(ticker));
  }

  private HistoricalTimeSeriesInfoDocument getOrLoadTimeSeries(final ExternalId ticker, final ExternalIdBundle idBundle) {

    final ExternalIdBundle searchBundle = idBundle.withoutScheme(ExternalSchemes.ISIN); //For things which move country, e.g. ISIN(VALE5 BZ Equity) == ISIN(RIODF US Equity)
    final HistoricalTimeSeriesInfoSearchRequest htsRequest = new HistoricalTimeSeriesInfoSearchRequest(searchBundle);
    htsRequest.setDataField("PX_LAST");
    final HistoricalTimeSeriesInfoSearchResult htsSearch = getToolContext().getHistoricalTimeSeriesMaster().search(htsRequest);
    switch (htsSearch.getDocuments().size()) {
      case 0:
        return loadTimeSeries(idBundle);
      case 1:
        break;
      default:
        throw new OpenGammaRuntimeException("Multiple time series match " + htsSearch);
    }
    final HistoricalTimeSeriesInfoDocument timeSeriesInfo = htsSearch.getDocuments().get(0);
    s_logger.debug("Loaded time series info {} for underlying {}", timeSeriesInfo, ticker);
    return timeSeriesInfo;
  }

  private HistoricalTimeSeriesInfoDocument loadTimeSeries(ExternalIdBundle idBundle) {
    final ReferenceDataProvider referenceDataProvider = getToolContext().getBloombergReferenceDataProvider();
    if (idBundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID) == null && idBundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER) != null) {
      //For some reason loading some series by TICKER fails, but BUID works
      final BiMap<String, ExternalIdBundle> map = BloombergDataUtils.convertToBloombergBuidKeys(Collections.singleton(idBundle), referenceDataProvider);
      if (map.size() != 1) {
        throw new OpenGammaRuntimeException("Failed to get buid");
      }
      for (final String key : map.keySet()) {
        final String buid = referenceDataProvider.getReferenceDataValue(key, BloombergConstants.FIELD_ID_BBG_UNIQUE);
        idBundle = idBundle.withExternalId(ExternalSchemes.bloombergBuidSecurityId(buid));
      }
    }
    final ExternalIdBundle searchBundle = idBundle.withoutScheme(ExternalSchemes.ISIN); // For things which move country, e.g. ISIN(VALE5 BZ Equity) == ISIN(RIODF US Equity)
    final Map<ExternalId, UniqueId> timeSeries = getToolContext().getHistoricalTimeSeriesLoader()
        .loadTimeSeries(searchBundle.getExternalIds(), "UNKNOWN", "PX_LAST", LocalDate.now().minusYears(1), null);
    if (timeSeries.size() != 1) {
      throw new OpenGammaRuntimeException("Failed to load time series " + idBundle + " " + timeSeries);
    }
    for (final UniqueId uid : timeSeries.values()) {
      return getToolContext().getHistoricalTimeSeriesMaster().get(uid);
    }
    throw new OpenGammaRuntimeException("Unexpected state");
  }

  private HistoricalTimeSeries updateTimeSeries(final HistoricalTimeSeries timeSeries) {
    if (!getToolContext().getHistoricalTimeSeriesLoader().updateTimeSeries(timeSeries.getUniqueId())) {
      throw new OpenGammaRuntimeException("Failed to update time series " + timeSeries);
    }
    //Force a cache miss on the source
    final HistoricalTimeSeriesInfoDocument newUid = getToolContext().getHistoricalTimeSeriesMaster().get(timeSeries.getUniqueId().toLatest());
    return getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(newUid.getUniqueId());
  }

  private HistoricalTimeSeriesInfoDocument loadTimeSeries(final HistoricalTimeSeriesInfoDocument timeSeriesInfo) {
    final ExternalIdBundle idBundle = timeSeriesInfo.getInfo().getExternalIdBundle().toBundle(LocalDate.now());
    return loadTimeSeries(idBundle);
  }

  private LocalDate oldestTimeSeriesAllowed() {
    return LocalDate.now().minusWeeks(1);
  }

  private ManageableSecurity getOrLoadEquity(final ExternalId ticker) {
    final ManageableSecurity underlyingSecurity = getOrLoadSecurity(ticker);
    if (!EquitySecurity.SECURITY_TYPE.equals(underlyingSecurity.getSecurityType())) {
      throw new OpenGammaRuntimeException("Underlying is not an equity");
    }
    return underlyingSecurity;
  }

  private ManageableSecurity getOrLoadSecurity(final ExternalId ticker) {
    final SecuritySearchResult underlyingSearch = getToolContext().getSecurityMaster().search(new SecuritySearchRequest(ticker));
    switch (underlyingSearch.getDocuments().size()) {
      case 0:
        s_logger.debug("Loading security for underlying {}", ticker);
        return loadSecurity(ticker);
      case 1:
        return underlyingSearch.getSingleSecurity();
      default:
        // Duplicate securities in the master
        s_logger.info("Multiple securities matched search for ticker {}. Using the first. {}", ticker, underlyingSearch);
        return underlyingSearch.getFirstSecurity();
    }
  }

  private ManageableSecurity loadSecurity(final ExternalId ticker) {
    final ExternalIdBundle tickerBundle = ExternalIdBundle.of(ticker);
    final Collection<ExternalIdBundle> bundles = Collections.singleton(tickerBundle);
    final Map<ExternalIdBundle, UniqueId> loaded = getToolContext().getSecurityLoader().loadSecurities(bundles);
    final UniqueId loadedSec = loaded.get(tickerBundle);
    if (loadedSec == null) {
      throw new OpenGammaRuntimeException("Failed to load security for " + ticker);
    }
    return getToolContext().getSecurityMaster().get(loadedSec).getSecurity();
  }

  /**
   * Stores the portfolio.
   *
   * @param portfolio the portfolio, not null
   */
  private void storePortfolio(final ManageablePortfolio portfolio) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();

    final PortfolioSearchRequest req = new PortfolioSearchRequest();
    req.setName(portfolio.getName());
    final PortfolioSearchResult result = portfolioMaster.search(req);
    switch (result.getDocuments().size()) {
      case 0:
        s_logger.info("Creating new portfolio");
        portfolioMaster.add(new PortfolioDocument(portfolio));
        break;
      case 1:
        final UniqueId previousId = result.getDocuments().get(0).getUniqueId();
        s_logger.info("Updating portfolio {}", previousId);
        portfolio.setUniqueId(previousId);
        final PortfolioDocument document = new PortfolioDocument(portfolio);
        document.setUniqueId(previousId);
        portfolioMaster.update(document);
        break;
      default:
        throw new OpenGammaRuntimeException("Multiple portfolios matching " + req);
    }
  }

  @Override
  protected Options createOptions(final boolean mandatoryConfigResource) {
    final Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createPortfolioNameOption());
    options.addOption(createOptionDepthOption());
    options.addOption(createNumContractsOption());
    options.addOption(createNumMembersOption());
    return options;
  }

  private static Option createPortfolioNameOption() {
    OptionBuilder.withLongOpt("portfolio");
    OptionBuilder.withDescription("The name of the portfolio to create/update");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(PORTFOLIO_NAME_OPT);
  }

  private static Option createOptionDepthOption() {
    OptionBuilder.withLongOpt("depth");
    OptionBuilder.withDescription("Number of options on either side of the strike price");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(OPTION_DEPTH_OPT);
  }

  private static Option createNumContractsOption() {
    OptionBuilder.withLongOpt("contracts");
    OptionBuilder.withDescription("Number of contracts for each option");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(NUM_CONTRACTS_OPT);
  }

  private static Option createNumMembersOption() {
    OptionBuilder.withLongOpt("members");
    OptionBuilder.withDescription("Number underlyers from index to include");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("resource");
    OptionBuilder.isRequired();
    return OptionBuilder.create(NUM_INDEX_MEMBERS_OPT);
  }

  @Override
  protected void doRun() throws Exception {
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));

    s_logger.info("Using portfolio \"{}\"", PORTFOLIO_NAME);
    s_logger.info("num index members: " + _numMembers);
    s_logger.info("Option Depth: " + _numOptions);
    s_logger.info("num contracts: {}", _numContracts.toString());
    final ManageablePortfolio portfolio = generatePortfolio(PORTFOLIO_NAME);
    storePortfolio(portfolio);

    s_logger.info(TOOL_NAME + " is finished.");
  }

  public void setNumContracts(final BigDecimal numContracts) {
    _numContracts = numContracts;
  }

  public void setNumOptions(final int numOptions) {
    _numOptions = numOptions;
  }

  public void setNumMembers(final int numMembers) {
    _numMembers = numMembers;
  }
}
