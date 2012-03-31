/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.loader;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.*;
import com.opengamma.bbg.tool.BloombergToolContext;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergTickerParserEQOption;
import com.opengamma.bloombergexample.tool.AbstractExampleTool;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.portfolio.*;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.util.tuple.Pair;

/**
 * A portfolio loader which generates a sensible portfolio of liquid equities and options on them
 * Also see DemoEquityOptionPortfolioLoader.
 */
public class DemoEquityOptionCollarPortfolioLoader extends AbstractExampleTool {

  public static final String TOOL_NAME = "Demo Equity Option Portfolio Loader";

  public static final String PORTFOLIO_NAME_OPT = "p";
  public static final String OPTION_DEPTH_OPT = "d";
  public static final String NUM_CONTRACTS_OPT = "n";
  public static final String NUM_INDEX_MEMBERS_OPT = "m";

  private static final String BLOOMBERG_EQUITY_TICKER_SUFFIX = " Equity";

  private static final Logger s_logger = LoggerFactory.getLogger(DemoEquityOptionCollarPortfolioLoader.class);

  private static final Map<String, String> INDEXES_TO_EXCHANGE = getIndexToExchangeMap();
  private static final Set<String> EXCLUDED_SECTORS = Sets.newHashSet("Financials");

  private static final Period[] MEMBER_OPTION_PERIODS = new Period[] {Period.ofMonths(3), Period.ofMonths(6)};

  private BigDecimal _numContracts;
  private int _numOptions;
  private int _numMembers;  

  /**
   * In units of currency
   */
  private static final BigDecimal VALUE_OF_UNDERLYING = BigDecimal.valueOf(100000);
  
  public static final String PORTFOLIO_NAME = "ExampleEquityOptionPortfolio";

  private static Map<String, String> getIndexToExchangeMap() {
    Map<String, String> ret = new HashMap<String, String>();
    ret.put("SPX", "US"); //S&P 500 -> combined US
    // ret.put("IBX", "BZ"); //Sao Paulo Stock Exchange IBrX Index -> combined Brazil
    //ret.put("TW50", "TT"); // FTSE TWSE Taiwan 50 Indx -> Taiwan Stock Exchange
    return ret;
  }

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * No arguments are needed.
   * 
   * @param args  the arguments, unused
   */
  public static void main(String[] args) { // CSIGNORE
    boolean success = new DemoEquityOptionCollarPortfolioLoader().initAndRun(args);
    System.exit(success ? 0 : 1);
  }

  //-------------------------------------------------------------------------
  protected ManageablePortfolio generatePortfolio(String portfolioName) {
    ReferenceDataProvider referenceDataProvider = ((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider();

    ManageablePortfolio portfolio = new ManageablePortfolio(portfolioName);

    //Is this a hack?
    ManageablePortfolioNode rootNode = portfolio.getRootNode();
    portfolio.setRootNode(rootNode);

    //    String indexTickerSuffix = " Index";

    Set<String> memberEquities = new HashSet<String>();

    for (Entry<String, String> entry : INDEXES_TO_EXCHANGE.entrySet()) {

      String indexTickerSuffix = " Index";

      String underlying = entry.getKey();
      String ticker = underlying + indexTickerSuffix;

      //don't add index (delete at some point)
      //      addNodes(rootNode, ticker, false, INDEX_OPTION_PERIODS);

      Set<String> indexMembers = BloombergDataUtils.getIndexMembers(referenceDataProvider, ticker);
      for (String member : indexMembers) {
        String symbol = getBloombergEquitySymbol(entry.getValue(), member);
        //time series errors for Walmart
        //Todo: investegate & fix
        if ("WMT US Equity".equals(symbol)) {
          continue;
        }
        memberEquities.add(symbol);
      }
    }

    // Sort the symbols for the current index by market cap (highest to lowest), skipping any in the list of EXCLUDED_SECTORS
    TreeMap<Double, String> equityByMarketCap = new TreeMap<Double, String>();
    ReferenceDataResult fields = referenceDataProvider.getFields(memberEquities, Sets.newHashSet(BloombergFields.CURRENT_MARKET_CAP_FIELD, BloombergConstants.FIELD_GICS_SUB_INDUSTRY));
    for (String equity : memberEquities) {
      PerSecurityReferenceDataResult result = fields.getResult(equity);
      String gicsCodeString = result.getFieldData().getString(BloombergConstants.FIELD_GICS_SUB_INDUSTRY);
      GICSCode gicsCode = GICSCode.of(gicsCodeString);
      if (EXCLUDED_SECTORS.contains(gicsCode.getSectorDescription())) {
        continue;
      }
      Double marketCap = result.getFieldData().getDouble(BloombergFields.CURRENT_MARKET_CAP_FIELD);
      equityByMarketCap.put(marketCap, equity);
    }

    // Add a given number of symbols (MEMBERS_DEPTH) to the portfolio and store in a List
    // When adding to the portfolio, add a collar of options with PVs distributed equally +/- around 0
    int count = 0;
    List<String> chosenEquities = new ArrayList<String>();
    for (Entry<Double, String> entry : equityByMarketCap.descendingMap().entrySet()) {
      if (count++ >= _numMembers) {
        break;
      }
      addNodes(rootNode, entry.getValue(), true, MEMBER_OPTION_PERIODS);
      chosenEquities.add(entry.getValue());
    }
    s_logger.info("Generated collar portfolio for {}", chosenEquities);
    return portfolio;
  }

  private String getBloombergEquitySymbol(String base, String member) {
    return member.split(" ")[0] + " " + base + BLOOMBERG_EQUITY_TICKER_SUFFIX;
  }

  private void addNodes(ManageablePortfolioNode rootNode, String underlying, boolean includeUnderlying, Period[] expiries) {
    ExternalId ticker = SecurityUtils.bloombergTickerSecurityId(underlying);
    ManageableSecurity underlyingSecurity = null;
    if (includeUnderlying) {
      underlyingSecurity = getOrLoadEquity(ticker);
    }

    ExternalIdBundle bundle = underlyingSecurity == null ? ExternalIdBundle.of(ticker) : underlyingSecurity.getExternalIdBundle();
    HistoricalTimeSeriesInfoDocument timeSeriesInfo = getOrLoadTimeSeries(ticker, bundle);
    double estimatedCurrentStrike = getOrLoadMostRecentPoint(timeSeriesInfo);
    Set<ExternalId> optionChain = getOptionChain(ticker);

    //TODO: reuse positions/nodes?
    String longName = underlyingSecurity == null ? "" : underlyingSecurity.getName();
    String formattedName = MessageFormatter.format("[{}] {}", underlying, longName);
    ManageablePortfolioNode equityNode = new ManageablePortfolioNode(formattedName);

    BigDecimal underlyingAmount = VALUE_OF_UNDERLYING.divide(BigDecimal.valueOf(estimatedCurrentStrike), BigDecimal.ROUND_HALF_EVEN);

    if (includeUnderlying) {
      addPosition(equityNode, underlyingAmount, ticker);
    }

    TreeMap<LocalDate, Set<BloombergTickerParserEQOption>> optionsByExpiry = new TreeMap<LocalDate, Set<BloombergTickerParserEQOption>>();
    for (ExternalId optionTicker : optionChain) {
      s_logger.debug("Got option {}", optionTicker);

      BloombergTickerParserEQOption optionInfo = BloombergTickerParserEQOption.getOptionParser(optionTicker);
      s_logger.debug("Got option info {}", optionInfo);

      LocalDate key = optionInfo.getExpiry();
      Set<BloombergTickerParserEQOption> set = optionsByExpiry.get(key);
      if (set == null) {
        set = new HashSet<BloombergTickerParserEQOption>();
        optionsByExpiry.put(key, set);
      }
      set.add(optionInfo);
    }
    Set<ExternalId> tickersToLoad = new HashSet<ExternalId>();

    BigDecimal expiryCount = BigDecimal.valueOf(expiries.length);
    BigDecimal defaultAmountAtExpiry = underlyingAmount.divide(expiryCount, BigDecimal.ROUND_DOWN);
    BigDecimal spareAmountAtExpiry = defaultAmountAtExpiry.add(BigDecimal.ONE);
    int spareCount = underlyingAmount.subtract(defaultAmountAtExpiry.multiply(expiryCount)).intValue();

    for (int i = 0; i < expiries.length; i++) {
      Period bucketPeriod = expiries[i];

      ManageablePortfolioNode bucketNode = new ManageablePortfolioNode(bucketPeriod.toString().substring(1));

      LocalDate nowish = LocalDate.now().withDayOfMonth(20); //This avoids us picking different options every time this script is run
      LocalDate targetExpiry = nowish.plus(bucketPeriod);
      LocalDate chosenExpiry = optionsByExpiry.floorKey(targetExpiry);
      if (chosenExpiry == null) {
        s_logger.warn("No options for {} on {}", targetExpiry, underlying);
        continue;
      }
      s_logger.info("Using time {} for bucket {} ({})", new Object[] {chosenExpiry, bucketPeriod, targetExpiry});

      Set<BloombergTickerParserEQOption> optionsAtExpiry = optionsByExpiry.get(chosenExpiry);
      TreeMap<Double, Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> optionsByStrike = new TreeMap<Double, Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>>();
      for (BloombergTickerParserEQOption option : optionsAtExpiry) {
        //        s_logger.info("option {}", option);
        double key = option.getStrike();
        Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(key);
        if (pair == null) {
          pair = Pair.of(null, null);
        }
        if (option.getOptionType() == OptionType.CALL) {
          pair = Pair.of(option, pair.getSecond());
        } else {
          pair = Pair.of(pair.getFirst(), option);
        }
        optionsByStrike.put(key, pair);
      }

      //cascading collar?
      BigDecimal amountAtExpiry = spareCount-- > 0 ? spareAmountAtExpiry : defaultAmountAtExpiry;

      s_logger.info(" est strike {}", estimatedCurrentStrike);
      Double[] strikes = optionsByStrike.keySet().toArray(new Double[0]);

      int strikeIndex = Arrays.binarySearch(strikes, estimatedCurrentStrike);
      if (strikeIndex < 0) {
        strikeIndex = -(1 + strikeIndex);
      }
      s_logger.info("strikes length {} index {} strike of index {}", new Object[] {Integer.valueOf(strikes.length), Integer.valueOf(strikeIndex), Double.valueOf(strikes[strikeIndex])});

      int minIndex = strikeIndex - _numOptions;
      minIndex = Math.max(0, minIndex);
      int maxIndex = strikeIndex + _numOptions;
      maxIndex = Math.min(strikes.length - 1, maxIndex);

      s_logger.info("min {} max {}", Integer.valueOf(minIndex), Integer.valueOf(maxIndex));
      StringBuffer sb = new StringBuffer("strikes: [");
      for (int j = minIndex; j <= maxIndex; j++) {
        sb.append(" ");
        sb.append(strikes[j]);
      }
      sb.append(" ]");
      s_logger.info(sb.toString());

      //Short Calls
      ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> calls = new ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>>();
      for (int j = minIndex; j < strikeIndex; j++) {
        Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(strikes[j]);
        if (pair == null) {
          throw new OpenGammaRuntimeException("no pair for strike" + strikes[j]);
        }
        calls.add(pair);
      }
      spreadOptions(bucketNode, calls, OptionType.CALL, -1, tickersToLoad, amountAtExpiry, includeUnderlying, calls.size());

      // Long Puts
      ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> puts = new ArrayList<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>>();
      for (int j = strikeIndex + 1; j <= maxIndex; j++) {
        Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair = optionsByStrike.get(strikes[j]);
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

    for (ExternalId optionTicker : tickersToLoad) {
      ManageableSecurity loaded = getOrLoadSecurity(optionTicker);
      if (loaded == null) {
        throw new OpenGammaRuntimeException("Unexpected option type " + loaded);
      }

      //TODO [LAPANA-29] Should be able to do this for index options too
      if (includeUnderlying) {
        try {
          HistoricalTimeSeriesInfoDocument loadedTs = getOrLoadTimeSeries(optionTicker, loaded.getExternalIdBundle());
          if (loadedTs == null) {
            throw new OpenGammaRuntimeException("Failed to get time series for " + loaded);
          }
        } catch (Exception ex) {
          s_logger.error("Failed to get time series for " + loaded, ex);
        }
      }
    }

    if (equityNode.getPositionIds().size() + equityNode.getChildNodes().size() > 0) {
      rootNode.addChildNode(equityNode);
    }
  }

  private void spreadOptions(ManageablePortfolioNode bucketNode, Collection<Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption>> options, OptionType type, int scale,
      Set<ExternalId> tickersToLoad, BigDecimal underlyingAmount, boolean includeUnderlying, int targetNumber) {

    if (targetNumber == 0) {
      return;
    }

    Collection<BloombergTickerParserEQOption> chosen = new ArrayList<BloombergTickerParserEQOption>();

    int remaining = targetNumber;
    for (Pair<BloombergTickerParserEQOption, BloombergTickerParserEQOption> pair : options) {
      BloombergTickerParserEQOption option;
      if (type == OptionType.PUT) {
        option = pair.getSecond();
      } else {
        option = pair.getFirst();
      }

      //TODO [LAPANA-29] Should be able to do this for index options too
      if (includeUnderlying) {
        try {
          HistoricalTimeSeriesInfoDocument loadedTs = getOrLoadTimeSeries(option.getIdentifier());
          HistoricalTimeSeries ts = getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(loadedTs.getUniqueId(), LocalDate.now().minusWeeks(1), true, LocalDate.now(), true);
          if (ts.getTimeSeries().isEmpty()) {
            s_logger.info("No recent time series points for " + option.getIdentifier());
            //   leave in for now  
            //          continue; //This option is not liquid enough for us
          }
        } catch (Exception ex) {
          s_logger.warn("Failed to get time series for " + option.getIdentifier(), ex);
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
    for (BloombergTickerParserEQOption option : chosen) {
      tickersToLoad.add(option.getIdentifier());
      addPosition(bucketNode, BigDecimal.valueOf(scale).multiply(_numContracts), option.getIdentifier());
    }
  }

  private void addPosition(ManageablePortfolioNode node, BigDecimal amount, ExternalId optionTicker) {
    ManageablePosition position = new ManageablePosition(amount, optionTicker);

    LocalDate tradeDate = getRandomTradeDate(optionTicker);
    ManageableTrade trade = new ManageableTrade(amount, optionTicker, tradeDate, null, ExternalId.of("CPARTY", "BACS"));

    position.addTrade(trade);
    PositionDocument doc = new PositionDocument(position);
    PositionDocument added = getToolContext().getPositionMaster().add(doc);
    node.addPosition(added);
  }

  private LocalDate getRandomTradeDate(ExternalId ticker) {
    int tradeAge = (int) (3 + (Math.random() * 30));
    LocalDate tradeDate = LocalDate.now().minusDays(tradeAge);
    //TODO: pick a date for which PX_LAST is known
    return tradeDate;
  }

  private Set<ExternalId> getOptionChain(ExternalId ticker) {
    if (ticker.getScheme() != SecurityUtils.BLOOMBERG_TICKER) {
      throw new OpenGammaRuntimeException("Not a bloomberg ticker " + ticker);
    }    
    ReferenceDataProvider referenceDataProvider = ((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider();

    Set<ExternalId> optionChain = BloombergDataUtils.getOptionChain(referenceDataProvider, ticker.getValue()); //TODO [BBG-88] this query shouldn't get cached permanently
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
    Double latestValue = timeSeries.getTimeSeries().getLatestValue();
    if (latestValue == null) {
      throw new OpenGammaRuntimeException("Unexpected null latest vaule");
    }
    return latestValue;
  }

  private HistoricalTimeSeries getAllowedRecentPoints(HistoricalTimeSeriesInfoDocument timeSeriesInfo) {
    LocalDate from = oldestTimeSeriesAllowed();
    HistoricalTimeSeries timeSeries = getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(timeSeriesInfo.getUniqueId().toLatest(), from, true, LocalDate.now(), true);
    return timeSeries;
  }

  private HistoricalTimeSeriesInfoDocument getOrLoadTimeSeries(ExternalId ticker) {
    return getOrLoadTimeSeries(ticker, ExternalIdBundle.of(ticker));
  }

  private HistoricalTimeSeriesInfoDocument getOrLoadTimeSeries(ExternalId ticker, ExternalIdBundle idBundle) {

    ExternalIdBundle searchBundle = idBundle.withoutScheme(SecurityUtils.ISIN); //For things which move country, e.g. ISIN(VALE5 BZ Equity) == ISIN(RIODF US Equity)
    HistoricalTimeSeriesInfoSearchRequest htsRequest = new HistoricalTimeSeriesInfoSearchRequest(searchBundle);
    htsRequest.setDataField("PX_LAST");
    HistoricalTimeSeriesInfoSearchResult htsSearch = getToolContext().getHistoricalTimeSeriesMaster().search(htsRequest);
    switch (htsSearch.getDocuments().size()) {
      case 0:
        return loadTimeSeries(idBundle);
      case 1:
        break;
      default:
        throw new OpenGammaRuntimeException("Multiple time series match " + htsSearch);
    }
    HistoricalTimeSeriesInfoDocument timeSeriesInfo = htsSearch.getDocuments().get(0);
    s_logger.debug("Loaded time series info {} for underlying {}", timeSeriesInfo, ticker);
    return timeSeriesInfo;
  }

  private HistoricalTimeSeriesInfoDocument loadTimeSeries(ExternalIdBundle idBundle) {    
    ReferenceDataProvider referenceDataProvider = ((BloombergToolContext) getToolContext()).getBloombergReferenceDataProvider();
    if (idBundle.getExternalId(SecurityUtils.BLOOMBERG_BUID) == null && idBundle.getExternalId(SecurityUtils.BLOOMBERG_TICKER) != null) {
      //For some reason loading some series by TICKER fails, but BUID works 
      BiMap<String, ExternalIdBundle> map = BloombergDataUtils.convertToBloombergBuidKeys(Collections.singleton(idBundle), referenceDataProvider);
      if (map.size() != 1) {
        throw new OpenGammaRuntimeException("Failed to get buid");
      }
      for (String key : map.keySet()) {
        ReferenceDataResult buidResult = referenceDataProvider.getFields(Collections.singleton(key), Collections.singleton(BloombergConstants.FIELD_ID_BBG_UNIQUE));
        String buid = buidResult.getResult(key).getFieldData().getString(BloombergConstants.FIELD_ID_BBG_UNIQUE);
        idBundle = idBundle.withExternalId(SecurityUtils.bloombergTickerSecurityId(buid));
      }
    }
    ExternalIdBundle searchBundle = idBundle.withoutScheme(SecurityUtils.ISIN); // For things which move country, e.g. ISIN(VALE5 BZ Equity) == ISIN(RIODF US Equity)
    Map<ExternalId, UniqueId> timeSeries = getToolContext().getHistoricalTimeSeriesLoader().addTimeSeries(searchBundle.getExternalIds(), "CMPL", "PX_LAST", null, null);
    if (timeSeries.size() != 1) {
      throw new OpenGammaRuntimeException("Failed to load time series " + idBundle + " " + timeSeries);
    }
    for (UniqueId uid : timeSeries.values()) {
      return getToolContext().getHistoricalTimeSeriesMaster().get(uid);
    }
    throw new OpenGammaRuntimeException("Unexpected state");
  }

  private HistoricalTimeSeries updateTimeSeries(HistoricalTimeSeries timeSeries) {
    if (!getToolContext().getHistoricalTimeSeriesLoader().updateTimeSeries(timeSeries.getUniqueId())) {
      throw new OpenGammaRuntimeException("Failed to update time series " + timeSeries);
    }
    //Force a cache miss on the source 
    HistoricalTimeSeriesInfoDocument newUid = getToolContext().getHistoricalTimeSeriesMaster().get(timeSeries.getUniqueId().toLatest());
    return getToolContext().getHistoricalTimeSeriesSource().getHistoricalTimeSeries(newUid.getUniqueId());
  }

  private HistoricalTimeSeriesInfoDocument loadTimeSeries(HistoricalTimeSeriesInfoDocument timeSeriesInfo) {
    ExternalIdBundle idBundle = timeSeriesInfo.getInfo().getExternalIdBundle().toBundle(LocalDate.now());
    return loadTimeSeries(idBundle);
  }

  private LocalDate oldestTimeSeriesAllowed() {
    return LocalDate.now().minusWeeks(1);
  }

  private ManageableSecurity getOrLoadEquity(ExternalId ticker) {
    ManageableSecurity underlyingSecurity = getOrLoadSecurity(ticker);
    if (!EquitySecurity.SECURITY_TYPE.equals(underlyingSecurity.getSecurityType())) {
      throw new OpenGammaRuntimeException("Underlying is not an equity");
    }
    return underlyingSecurity;
  }

  private ManageableSecurity getOrLoadSecurity(ExternalId ticker) {
    SecuritySearchResult underlyingSearch = getToolContext().getSecurityMaster().search(new SecuritySearchRequest(ticker));
    switch (underlyingSearch.getDocuments().size()) {
      case 0:
        s_logger.debug("Loading security for underlying {}", ticker);
        return loadSecurity(ticker);
      case 1:
        return underlyingSearch.getSingleSecurity();
      default:
        // Duplicate securities in the master
        s_logger.warn("Multiple securities matched search for ticker {}. Using the first. {}", ticker, underlyingSearch);
        return underlyingSearch.getFirstSecurity();
    }
  }

  private ManageableSecurity loadSecurity(ExternalId ticker) {
    ExternalIdBundle tickerBundle = ExternalIdBundle.of(ticker);
    Collection<ExternalIdBundle> bundles = Collections.singleton(tickerBundle);
    Map<ExternalIdBundle, UniqueId> loaded = getToolContext().getSecurityLoader().loadSecurity(bundles);
    UniqueId loadedSec = loaded.get(tickerBundle);
    if (loadedSec == null) {
      throw new OpenGammaRuntimeException("Failed to load security for " + ticker);
    }
    return getToolContext().getSecurityMaster().get(loadedSec).getSecurity();
  }

  /**
   * Stores the portfolio.
   * 
   * @param portfolio  the portfolio, not null
   */
  private void storePortfolio(ManageablePortfolio portfolio) {
    PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();

    PortfolioSearchRequest req = new PortfolioSearchRequest();
    req.setName(portfolio.getName());
    PortfolioSearchResult result = portfolioMaster.search(req);
    switch (result.getDocuments().size()) {
      case 0:
        s_logger.info("Creating new portfolio");
        portfolioMaster.add(new PortfolioDocument(portfolio));
        break;
      case 1:
        UniqueId previousId = result.getDocuments().get(0).getUniqueId();
        s_logger.info("Updating portfolio {}", previousId);
        portfolio.setUniqueId(previousId);
        PortfolioDocument document = new PortfolioDocument(portfolio);
        document.setUniqueId(previousId);
        portfolioMaster.update(document);
        break;
      default:
        throw new OpenGammaRuntimeException("Multiple portfolios matching " + req);
    }
  }

  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
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

    if (getToolContext() instanceof BloombergToolContext == false) {
      throw new OpenGammaRuntimeException("The " + DemoEquityOptionCollarPortfolioLoader.class.getSimpleName() + " requires a tool context which implements " + BloombergToolContext.class.getName());
    }

    s_logger.info("Using portfolio \"{}\"", PORTFOLIO_NAME);
    s_logger.info("num index members: " + _numMembers);
    s_logger.info("Option Depth: " + _numOptions);
    s_logger.info("num contracts: {}", _numContracts.toString());
    ManageablePortfolio portfolio = generatePortfolio(PORTFOLIO_NAME);
    storePortfolio(portfolio);

    s_logger.info(TOOL_NAME + " is finished.");
  }

  public void setNumContracts(BigDecimal numContracts) {
    _numContracts = numContracts;
  }

  public void setNumOptions(int numOptions) {
    _numOptions = numOptions;
  }

  public void setNumMembers(int numMembers) {
    _numMembers = numMembers;
  }
}
