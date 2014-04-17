/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergTickerParserBondFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserCommodityFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserEQVanillaOption;
import com.opengamma.bbg.util.BloombergTickerParserFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserIRFutureOption;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergBondFuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergCommodityFuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergEquityFuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergIRFuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveDefinition;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.FuturePriceCurveSpecification;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.master.security.impl.SecuritySearchIterator;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Create future price curve based on the instruments in security master.
 */
@Scriptable
public class FuturePriceCurveCreator extends AbstractTool<IntegrationToolContext> {

  /** Logger */
  private static Logger s_logger = LoggerFactory.getLogger(FuturePriceCurveCreator.class);

  /** bbg surface prefix */
  private static final String BBG_PREFIX = "BBG_";
  /** for ir bonds when using price */
  private static final String PRICE = "PRICE_";
  /** when getting price instead of vol */
  private static final String FIELD_NAME_PRICE = MarketDataRequirementNames.MARKET_VALUE;
  /** wildcard search symbol */
  private static final String WILDCARD_SEARCH = "*";

  //Track surfaces we create so we dont recreate them when multiple securities need them
  /** vol definitions we have created */
  private final Set<String> _curveDefinitionNames = new HashSet<>();
  /** vol specifications we have created */
  private final Set<String> _curveSpecificationNames = new HashSet<>();
  /** regexp to get strike from option ticker */
  private static final String STRIKE_REGEXP = "[CP][ ]*((\\d)+(.\\d+)*)\\b";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new FuturePriceCurveCreator().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    ToolContext toolContext = getToolContext();
    ConfigMaster configMaster = toolContext.getConfigMaster();

    CommandLine commandLine = getCommandLine();
    final String name = commandLine.getOptionValue("name", WILDCARD_SEARCH);
    final boolean dryRun = commandLine.hasOption("do-not-persist");
    final boolean skipExisting = commandLine.hasOption("skip");

    // if skipping existing surfaces get the list now
    if (skipExisting) {
      ConfigSearchRequest<FuturePriceCurveDefinition<?>> curveDefinitionSearchRequest = new ConfigSearchRequest<>();
      curveDefinitionSearchRequest.setType(VolatilitySurfaceDefinition.class);
      // can't use name to restrict search as ticker symbol may not be same as underlying symbol (e.g. RUT vs RUY)
      curveDefinitionSearchRequest.setName(WILDCARD_SEARCH);
      for (ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, curveDefinitionSearchRequest)) {
        _curveDefinitionNames.add(doc.getName());
      }

      ConfigSearchRequest<FuturePriceCurveSpecification> curveSpecSearchRequest = new ConfigSearchRequest<>();
      curveSpecSearchRequest.setType(FuturePriceCurveSpecification.class);
      // can't use name to restrict search as ticker symbol may not be same as underlying symbol (e.g. RUT vs RUY)
      curveSpecSearchRequest.setName(WILDCARD_SEARCH);
      for (ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, curveSpecSearchRequest)) {
        _curveSpecificationNames.add(doc.getName());
      }
    }

    createSurfaces(name, dryRun);
  }

  /**
   * Create surfaces for all (non-expired) securities
   * 
   * @param name the pattern to match securities
   * @param dryRun set to true to not write to the database
   */
  private void createSurfaces(String name, boolean dryRun) {
    ConfigMaster configMaster = getToolContext().getConfigMaster();
    SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ReferenceDataProvider bbgRefData = getToolContext().getBloombergReferenceDataProvider();

    SecuritySearchRequest securityRequest = new SecuritySearchRequest();
    securityRequest.setName(name);
    securityRequest.setSortOrder(SecuritySearchSortOrder.NAME_ASC);

    for (SecurityDocument doc : SecuritySearchIterator.iterable(securityMaster, securityRequest)) {
      FinancialSecurity security = (FinancialSecurity) doc.getSecurity();
      try {
        security.accept(new FuturePriceCurveCreatorVisitor(configMaster, bbgRefData, _curveSpecificationNames, _curveDefinitionNames, dryRun));
      } catch (Exception ex) {
        s_logger.error("Error processing " + security.getName() + ": " + ex.getLocalizedMessage());
        continue;
      }
    }
  }

  /**
   * Visitor that creates curves for the security it visits
   */
  private class FuturePriceCurveCreatorVisitor extends FinancialSecurityVisitorAdapter<Object> {

    /** the config master */
    private final ConfigMaster _configMaster;
    /** the reference data provider */
    private final ReferenceDataProvider _referenceDataProvider;
    /** known vol specifications */
    private final Set<String> _knownCurveSpecNames;
    /** known vol definitions */
    private final Set<String> _knownCurveDefNames;
    /** skip write to database */
    private final boolean _dryRun;

    /**
     * @param configMaster the config master
     * @param referenceDataProvider the reference data provider
     * @param knownVolSpecNames curve specifications to skip
     * @param knownVolDefNames curve definitions to skip
     * @param dryRun if true skip write to the database
     */
    FuturePriceCurveCreatorVisitor(final ConfigMaster configMaster, final ReferenceDataProvider referenceDataProvider, final Set<String> knownVolSpecNames, final Set<String> knownVolDefNames,
        final boolean dryRun) {
      _configMaster = configMaster;
      _referenceDataProvider = referenceDataProvider;
      _knownCurveSpecNames = knownVolSpecNames;
      _knownCurveDefNames = knownVolDefNames;
      _dryRun = dryRun;
    }

    @Override
    public Object visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final BloombergTickerParserFutureOption tickerParser = new BloombergTickerParserBondFutureOption(ticker);
      //final String postfix = BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      String underlyingOptChainTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), tickerParser.getTypeName());
      final String name = BBG_PREFIX + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.BOND_FUTURE_PRICE;
      if (!_knownCurveSpecNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveSpecification \"{}\"", name);
        final BloombergBondFuturePriceCurveInstrumentProvider curveInstrumentProvider =
            new BloombergBondFuturePriceCurveInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(), FIELD_NAME_PRICE);
        createFuturePriceCurveSpecification(security.getCurrency(), name, curveInstrumentProvider);
      }
      createFuturePriceCurveDefinition(underlyingOptChainTicker, name, security.getCurrency());
      return null;
    }

    @Override
    public Object visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final BloombergTickerParserFutureOption tickerParser = new BloombergTickerParserCommodityFutureOption(ticker);
      //      final String postfix = BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      String underlyingOptChainTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), tickerParser.getTypeName());
      final String name = BBG_PREFIX + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.COMMODITY_FUTURE_PRICE;
      if (!_knownCurveSpecNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveSpecification \"{}\"", name);
        final BloombergCommodityFuturePriceCurveInstrumentProvider curveInstrumentProvider =
            new BloombergCommodityFuturePriceCurveInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(), FIELD_NAME_PRICE, ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName());
        createFuturePriceCurveSpecification(security.getCurrency(), name, curveInstrumentProvider);
      }
      createFuturePriceCurveDefinition(underlyingOptChainTicker, name, security.getCurrency());
      return null;
    }

    @Override
    public Object visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final BloombergTickerParserFutureOption tickerParser = new BloombergTickerParserIRFutureOption(ticker);
      //      final String postfix = BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      String underlyingTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), tickerParser.getTypeName());
      final String name = BBG_PREFIX + PRICE + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.IR_FUTURE_PRICE;
      if (!_knownCurveSpecNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveSpecification \"{}\"", name);
        final BloombergIRFuturePriceCurveInstrumentProvider curveInstrumentProvider = new BloombergIRFuturePriceCurveInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(),
            FIELD_NAME_PRICE);
        createFuturePriceCurveSpecification(security.getCurrency(), name, curveInstrumentProvider);
      }
      createFuturePriceCurveDefinition(underlyingTicker, name, security.getCurrency());
      return null;
    }

    @Override
    public Object visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return null;
    }

    @Override
    public Object visitEquityOptionSecurity(final EquityOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final BloombergTickerParserEQVanillaOption tickerParser = new BloombergTickerParserEQVanillaOption(ticker);
      String underlyingOptChainTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), "Equity");
      final String name = BBG_PREFIX + tickerParser.getSymbol() + "_" + tickerParser.getExchangeCode() + "_" + InstrumentTypeProperties.EQUITY_FUTURE_PRICE;
      if (!_knownCurveSpecNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveSpecification \"{}\"", name);
        // use future chain to get prefix, exchange and postfix.
        final Collection<ExternalId> futChain = BloombergDataUtils.getFuturechain(_referenceDataProvider, underlyingOptChainTicker);
        if (futChain == null || futChain.isEmpty()) {
          throw new OpenGammaRuntimeException("Can't get future chain for " + ticker);
        }
        final String[] tickerParts = futChain.iterator().next().getValue().split("\\s+"); // e.g. [AAPL=G3, OC, Equity]
        if (tickerParts == null || tickerParts.length != 3 || tickerParts[0].length() < 3) {
          throw new OpenGammaRuntimeException("Can't get prefix, exchange and postfix from " + futChain.iterator().next());
        }
        final String prefix = tickerParts[0].substring(0, tickerParts[0].length() - 2); // AAPL=G3 -> AAPL=
        final String exchange = tickerParts[1];
        final String postfix = tickerParts[2];
        final BloombergEquityFuturePriceCurveInstrumentProvider curveInstrumentProvider =
            new BloombergEquityFuturePriceCurveInstrumentProvider(prefix, postfix, FIELD_NAME_PRICE, exchange);
        createFuturePriceCurveSpecification(UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), underlyingOptChainTicker), name, curveInstrumentProvider);
      }
      createFuturePriceCurveDefinition(Lists.newArrayList(1., 2., 3., 4.), name, security.getCurrency()); // hardcoded to 4 currently
      return null;
    }

    // ------ FX securities handled by a different tool ------

    @Override
    public Object visitFXOptionSecurity(final FXOptionSecurity security) {
      return null;
    }

    @Override
    public Object visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
      return null;
    }

    @Override
    public Object visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
      return null;
    }

    @Override
    public Object visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
      return null;
    }

    @Override
    public Object visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
      return null;
    }

    // ------ Non option securities -------

    @Override
    public Object visitEquitySecurity(final EquitySecurity security) {
      return null;
    }

    @Override
    public Object visitEquityFutureSecurity(final EquityFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitBondFutureSecurity(final BondFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitMetalFutureSecurity(final MetalFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      return null;
    }

    @Override
    public Object visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return null;
    }

    @Override
    public Object visitSwapSecurity(final SwapSecurity security) {
      return null;
    }

    @Override
    public Object visitSwaptionSecurity(final SwaptionSecurity security) {
      return null;
    }

    private void createFuturePriceCurveDefinition(final String underlyingTicker, final String name, final UniqueIdentifiable target) {
      if (!_knownCurveDefNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveDefinition \"{}\"", name);
        final Set<ExternalId> options = BloombergDataUtils.getOptionChain(_referenceDataProvider, underlyingTicker);
        final ObjectsPair<ImmutableList<Double>, ImmutableList<Double>> axes = determineAxes(options);
        createFuturePriceCurveDefinition(axes.getFirst(), name, target);
      }
    }

    private void createFuturePriceCurveDefinition(final List<Double> xAxis, final String name, final UniqueIdentifiable target) {
      if (!_knownCurveDefNames.contains(name)) {
        s_logger.info("Creating FuturePriceCurveDefinition \"{}\"", name);
        final FuturePriceCurveDefinition<Double> futureCurveDefinition = FuturePriceCurveDefinition.of(name, target, xAxis);
        final ConfigItem<FuturePriceCurveDefinition<Double>> futureCurveDefinitionConfig = ConfigItem.of(futureCurveDefinition, futureCurveDefinition.getName(), FuturePriceCurveDefinition.class);
        if (!_dryRun) {
          ConfigMasterUtils.storeByName(_configMaster, futureCurveDefinitionConfig);
        }
        _knownCurveDefNames.add(name);
      }
    }

    private void createFuturePriceCurveSpecification(final UniqueIdentifiable target, final String name, final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider) {
      final FuturePriceCurveSpecification priceCurveSpec = new FuturePriceCurveSpecification(name, target, curveInstrumentProvider);
      final ConfigItem<FuturePriceCurveSpecification> volSpecConfig = ConfigItem.of(priceCurveSpec, priceCurveSpec.getName(), FuturePriceCurveSpecification.class);
      if (!_dryRun) {
        ConfigMasterUtils.storeByName(_configMaster, volSpecConfig);
      }
      _knownCurveSpecNames.add(name);
    }

    /**
     * From the available options determine axes for a volatility surface.
     * @param options the available options as given by OPT_CHAIN (must be tickers)
     * @return x and y axes
     */
    private ObjectsPair<ImmutableList<Double>, ImmutableList<Double>> determineAxes(Collection<ExternalId> options) {
      Set<Double> strikes = new TreeSet<>();
      Pattern strikePattern = Pattern.compile(STRIKE_REGEXP);
      for (ExternalId option : options) {
        String name = option.getValue();
        Matcher matcher = strikePattern.matcher(name);
        if (!matcher.find()) {
          s_logger.error("Cant calculate strike for {}", name);
          continue;
        }
        strikes.add(Double.valueOf(matcher.group(1)));
      }
      if (strikes.isEmpty()) {
        throw new OpenGammaRuntimeException("Could not get any strikes");
      }
      // assume all strikes exist for all exercise dates
      int numX = options.size() / strikes.size();
      // Can get quite low numbers (OPT_CHAIN truncated?) so ensure a minimum
      //TODO: Check why numbers can be so low.
      if (numX < 12) {
        numX = 12;
      }
      List<Double> xAxis = new ArrayList<>();
      for (int i = 1; i < numX + 1; i++) {
        xAxis.add(Double.valueOf(i));
      }
      return ObjectsPair.of(ImmutableList.copyOf(xAxis), ImmutableList.copyOf(strikes));
    }

    private String getUnderlyingTicker(final String ticker, final ExternalId underlyingId, final String postfix) {
      if (underlyingId.isScheme(ExternalSchemes.BLOOMBERG_TICKER)) {
        return underlyingId.getValue();
      }
      // underlying id is not a ticker - have to lookup
      //TODO: check if there is a better buid -> ticker lookup function
      String underlyingTicker = _referenceDataProvider.getReferenceData(Collections.singleton(ticker), Collections.singleton(BloombergConstants.FIELD_OPT_UNDL_TICKER))
          .get(ticker)
          .getString(BloombergConstants.FIELD_OPT_UNDL_TICKER) + " " + BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      if (!underlyingTicker.endsWith(postfix)) {
        underlyingTicker = underlyingTicker + " " + postfix;
      }
      return underlyingTicker;
    }

  }

  @Override
  protected Options createOptions(boolean mandatoryConfig) {
    Options options = super.createOptions(mandatoryConfig);
    options.addOption(createSearchOption());
    options.addOption(createDoNotPersistOption());
    options.addOption(createSkipExistingOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createSearchOption() {
    return OptionBuilder.isRequired(false)
        .hasArgs()
        .withArgName("name search string")
        .withDescription("The name(s) you want to search for (globbing available) - default all")
        .withLongOpt("name")
        .create("n");
  }

  @SuppressWarnings("static-access")
  private Option createDoNotPersistOption() {
    return OptionBuilder.isRequired(false)
        .hasArg(false)
        .withDescription("Simulate writing rather than actually writing to DB")
        .withLongOpt("do-not-persist")
        .create("d");
  }

  @SuppressWarnings("static-access")
  private Option createSkipExistingOption() {
    return OptionBuilder.isRequired(false)
        .hasArg(false)
        .withDescription("Skip surfaces that already exist - do not overwrite")
        .withLongOpt("skip")
        .create("s");
  }

  @Override
  protected void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(120);
    formatter.printHelp("future-price-curve-creator.sh", options, true);
  }
}
