/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.bbg.util.BloombergTickerParserBondFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserCommodityFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserEQIndexOption;
import com.opengamma.bbg.util.BloombergTickerParserEQVanillaOption;
import com.opengamma.bbg.util.BloombergTickerParserFutureOption;
import com.opengamma.bbg.util.BloombergTickerParserIRFutureOption;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.volatility.surface.BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.SurfaceAndCubeQuoteType;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceDefinition;
import com.opengamma.financial.analytics.volatility.surface.VolatilitySurfaceSpecification;
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
 * Create volatility surfaces based on the instruments in security master.
 */
@Scriptable
public class VolatilitySurfaceCreator extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static Logger s_logger = LoggerFactory.getLogger(VolatilitySurfaceCreator.class);

  /** bbg surface prefix */
  private static final String BBG_SURFACE_PREFIX = "BBG_";
  /** for ir bonds when using price */
  private static final String PRICE = "PRICE_";
  /** implied vol */
  private static final String FIELD_NAME_VOL = MarketDataRequirementNames.IMPLIED_VOLATILITY;
  /** when getting price instead of vol */
  private static final String FIELD_NAME_PRICE = MarketDataRequirementNames.MARKET_VALUE;
  /** wildcard search symbol */
  private static final String WILDCARD_SEARCH = "*";
  /** regexp to get strike from option ticker */
  private static final String STRIKE_REGEXP = "[CP][ ]*((\\d)+(.\\d+)*)\\b";

  //Track surfaces we create so we dont recreate them when multiple securities need them
  /** vol definitions we have created */
  private final Set<String> _volDefinitionNames = new HashSet<String>();
  /** vol specifications we have created */
  private final Set<String> _volSpecificationNames = new HashSet<String>();

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) {  // CSIGNORE
    new VolatilitySurfaceCreator().invokeAndTerminate(args);
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
      ConfigSearchRequest<VolatilitySurfaceDefinition<?, ?>> volDefinitionSearchRequest = new ConfigSearchRequest<VolatilitySurfaceDefinition<?, ?>>();
      volDefinitionSearchRequest.setType(VolatilitySurfaceDefinition.class);
      // can't use name to restrict search as ticker symbol may not be same as underlying symbol (e.g. RUT vs RUY)
      volDefinitionSearchRequest.setName(WILDCARD_SEARCH);
      for (ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, volDefinitionSearchRequest)) {
        _volDefinitionNames.add(doc.getName());
      }

      ConfigSearchRequest<VolatilitySurfaceSpecification> volSpecSearchRequest = new ConfigSearchRequest<VolatilitySurfaceSpecification>();
      volSpecSearchRequest.setType(VolatilitySurfaceSpecification.class);
      // can't use name to restrict search as ticker symbol may not be same as underlying symbol (e.g. RUT vs RUY)
      volSpecSearchRequest.setName(WILDCARD_SEARCH);
      for (ConfigDocument doc : ConfigSearchIterator.iterable(configMaster, volSpecSearchRequest)) {
        _volSpecificationNames.add(doc.getName());
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
        security.accept(new VolSurfaceCreatorVisitor(configMaster, bbgRefData, _volSpecificationNames, _volDefinitionNames, dryRun));
      } catch (Exception ex) {
        s_logger.error("Error processing " + security.getName() + ": " + ex.getLocalizedMessage());
        continue;
      }
    }
  }

  /**
   * Visitor that creates surfaces for the security it visits
   */
  private class VolSurfaceCreatorVisitor extends FinancialSecurityVisitorAdapter<Object> {

    /** the config master */
    private final ConfigMaster _configMaster;
    /** the reference data provider */
    private final ReferenceDataProvider _referenceDataProvider;
    /** known vol specifications */
    private final Set<String> _knownVolSpecNames;
    /** known vol definitions */
    private final Set<String> _knownVolDefNames;
    /** skip write to database */
    private final boolean _dryRun;

    /**
     * @param configMaster the config master
     * @param referenceDataProvider the reference data provider
     * @param knownVolSpecNames surface specifications to skip
     * @param knownVolDefNames surface definitions to skip
     * @param dryRun if true skip write to the database
     */
    VolSurfaceCreatorVisitor(final ConfigMaster configMaster, final ReferenceDataProvider referenceDataProvider, final Set<String> knownVolSpecNames, final Set<String> knownVolDefNames,
        final boolean dryRun) {
      _configMaster = configMaster;
      _referenceDataProvider = referenceDataProvider;
      _knownVolSpecNames = knownVolSpecNames;
      _knownVolDefNames = knownVolDefNames;
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
      final String name = BBG_SURFACE_PREFIX + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.BOND_FUTURE_OPTION;
      if (!_knownVolSpecNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceSpecification \"{}\"", name);
        final BloombergFutureOptionVolatilitySurfaceInstrumentProvider surfaceInstrumentProvider =
            new BloombergBondFutureOptionVolatilitySurfaceInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(), FIELD_NAME_VOL, getSpot(underlyingOptChainTicker),
                security.getTradingExchange());
        createVolatilitySpecification(security.getCurrency().getUniqueId(), name, surfaceInstrumentProvider);
      }
      createvolatilityDefinition(underlyingOptChainTicker, name, security.getCurrency().getUniqueId());
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
      final String name = BBG_SURFACE_PREFIX + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.COMMODITY_FUTURE_OPTION;
      if (!_knownVolSpecNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceSpecification \"{}\"", name);
        final BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider surfaceInstrumentProvider =
            new BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(), FIELD_NAME_VOL, getSpot(underlyingOptChainTicker),
                security.getTradingExchange());
        createVolatilitySpecification(security.getCurrency().getUniqueId(), name, surfaceInstrumentProvider);
      }
      createvolatilityDefinition(underlyingOptChainTicker, name, security.getCurrency().getUniqueId());
      return null;
    }

    @Override
    public Object visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final BloombergTickerParserEQIndexOption tickerParser = new BloombergTickerParserEQIndexOption(ticker);
      final String postfix = BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      String underlyingTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), postfix);
      final String name = BBG_SURFACE_PREFIX + underlyingTicker + "_" + InstrumentTypeProperties.EQUITY_OPTION;
      if (!_knownVolSpecNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceSpecification \"{}\"", name);
        final BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider surfaceInstrumentProvider =
            new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(tickerParser.getSymbol(), postfix, FIELD_NAME_VOL, getSpot(underlyingTicker), security.getExchange());
        createVolatilitySpecification(UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), underlyingTicker), name, surfaceInstrumentProvider);
      }
      createvolatilityDefinition(underlyingTicker, name, UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), underlyingTicker));
      return null;
    }

    @Override
    public Object visitEquityOptionSecurity(final EquityOptionSecurity security) {
      if (TimeCalculator.getTimeBetween(ZonedDateTime.now(OpenGammaClock.getInstance()), security.getExpiry().getExpiry()) < 0) {
        return null;
      }
      final String ticker = security.getExternalIdBundle().getValue(ExternalSchemes.BLOOMBERG_TICKER);
      final String postfix = BloombergDataUtils.splitTickerAtMarketSector(ticker).getSecond();
      final String prefix = new BloombergTickerParserEQVanillaOption(ticker).getSymbol() + " " + security.getExchange();
      String underlyingTicker = getUnderlyingTicker(ticker, security.getUnderlyingId(), postfix);
      final String name = BBG_SURFACE_PREFIX + prefix + "_" + InstrumentTypeProperties.EQUITY_OPTION;
      if (!_knownVolSpecNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceSpecification \"{}\"", name);
        final BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider surfaceInstrumentProvider =
            new BloombergEquityFutureOptionVolatilitySurfaceInstrumentProvider(prefix, postfix, FIELD_NAME_VOL, getSpot(underlyingTicker), security.getExchange());
        createVolatilitySpecification(UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), underlyingTicker), name, surfaceInstrumentProvider);
      }
      createvolatilityDefinition(underlyingTicker, name, UniqueId.of(ExternalSchemes.BLOOMBERG_TICKER_WEAK.getName(), underlyingTicker));
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
      final String name = BBG_SURFACE_PREFIX + PRICE + tickerParser.getSymbol() + "_" + security.getCurrency().getCode() + "_" + InstrumentTypeProperties.IR_FUTURE_OPTION;
      if (!_knownVolSpecNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceSpecification \"{}\"", name);
        final BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider surfaceInstrumentProvider =
            new BloombergIRFutureOptionVolatilitySurfaceInstrumentProvider(tickerParser.getSymbol(), tickerParser.getTypeName(), FIELD_NAME_PRICE, getSpot(underlyingTicker), security.getExchange());
        createVolatilitySpecification(security.getCurrency().getUniqueId(), name, surfaceInstrumentProvider, SurfaceAndCubePropertyNames.PRICE_QUOTE);
      }
      createvolatilityDefinition(underlyingTicker, name, security.getCurrency().getUniqueId());
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

    private void createvolatilityDefinition(final String underlyingTicker, final String name, final UniqueId target) {
      if (!_knownVolDefNames.contains(name)) {
        s_logger.info("Creating VolatilitySurfaceDefinition \"{}\"", name);
        final Set<ExternalId> options = BloombergDataUtils.getOptionChain(_referenceDataProvider, underlyingTicker);
        final ObjectsPair<ImmutableList<Double>, ImmutableList<Double>> axes = determineAxes(options);
        final VolatilitySurfaceDefinition<Double, Double> volSurfaceDefinition =
            new VolatilitySurfaceDefinition<Double, Double>(name, target,
                axes.getFirst().toArray(new Double[0]), axes.getSecond().toArray(new Double[0]));
        final ConfigItem<VolatilitySurfaceDefinition<Double, Double>> volDefinition = ConfigItem.of(volSurfaceDefinition, volSurfaceDefinition.getName(), VolatilitySurfaceDefinition.class);
        if (!_dryRun) {
          ConfigMasterUtils.storeByName(_configMaster, volDefinition);
        }
        _knownVolDefNames.add(name);
      }
    }

    private void createVolatilitySpecification(final UniqueIdentifiable target, final String name, final SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider, String quoteUnits) {
      final VolatilitySurfaceSpecification volSurfaceSpec = new VolatilitySurfaceSpecification(name, target,
          SurfaceAndCubeQuoteType.CALL_AND_PUT_STRIKE, quoteUnits,
          surfaceInstrumentProvider);
      final ConfigItem<VolatilitySurfaceSpecification> volSpecConfig = ConfigItem.of(volSurfaceSpec, volSurfaceSpec.getName(), VolatilitySurfaceSpecification.class);
      if (!_dryRun) {
        ConfigMasterUtils.storeByName(_configMaster, volSpecConfig);
      }
      _knownVolSpecNames.add(name);
    }

    private void createVolatilitySpecification(final UniqueIdentifiable target, final String name, final SurfaceInstrumentProvider<?, ?> surfaceInstrumentProvider) {
      createVolatilitySpecification(target, name, surfaceInstrumentProvider, SurfaceAndCubePropertyNames.VOLATILITY_QUOTE);
    }

    /**
     * From the available options determine axes for a volatility surface.
     * @param options the available options as given by OPT_CHAIN (must be tickers)
     * @return x and y axes
     */
    private ObjectsPair<ImmutableList<Double>, ImmutableList<Double>> determineAxes(Collection<ExternalId> options) {
      Set<Double> strikes = new TreeSet<Double>();
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
      // Could call FUT_CHAIN to get list if OPT_CHAIN list is insufficient but just default to a reasonable value
      if (numX < 17) {
        numX = 17;
      }
      List<Double> xAxis = new ArrayList<Double>();
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

    private double getSpot(final String ticker) {
      //TODO: check if this is the correct field
      return _referenceDataProvider.getReferenceData(Collections.singleton(ticker), Collections.singleton(BloombergConstants.BBG_FIELD_LAST_PRICE))
          .get(ticker)
          .getDouble(BloombergConstants.BBG_FIELD_LAST_PRICE).doubleValue();
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
    formatter.printHelp("volatility-surface-creator.sh", options, true);
  }
}
