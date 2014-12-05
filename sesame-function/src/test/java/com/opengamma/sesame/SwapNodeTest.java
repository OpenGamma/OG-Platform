/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.FXSwapNode;
import com.opengamma.financial.analytics.ircurve.strips.SwapNode;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.FXForwardAndSwapConvention;
import com.opengamma.financial.convention.FXSpotConvention;
import com.opengamma.financial.convention.FinancialConvention;
import com.opengamma.financial.convention.OISLegConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.SwapFixedLegConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.convention.impl.MasterConventionSource;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.FxRateId;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MapMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests that swap nodes can be used in curve construction.
 */
@Test(groups = TestGroup.UNIT)
public class SwapNodeTest {

  private CurveConstructionConfiguration _ccc;

  private DiscountingMulticurveBundleResolverFn _curveFunction;

  private Environment _env;

  private final ConventionMaster _conventionMaster = new InMemoryConventionMaster();
  private final ConfigMaster _configMaster = new InMemoryConfigMaster();
  private final SecurityMaster _securityMaster = new InMemorySecurityMaster();

  @BeforeMethod
  public void setup() {

    Map<ExternalId, Double> swapMarketDataSource = buildDataForFxSwapCurve();
    Map<ExternalId, Double> usdMarketDataSource = buildDataForUsdCurve();

    Double spotRate = swapMarketDataSource.get(ExternalId.of("TICKER", "SPOT"));
    ZonedDateTime valuationDate = ZonedDateTime.of(2014, 1, 10, 11, 0, 0, 0, ZoneId.of("America/Chicago"));

    MarketDataEnvironmentBuilder marketDataBuilder = new MarketDataEnvironmentBuilder();

    for (Map.Entry<ExternalId, Double> entry : swapMarketDataSource.entrySet()) {
      marketDataBuilder.add(RawId.of(entry.getKey().toBundle()), entry.getValue());
    }
    for (Map.Entry<ExternalId, Double> entry : usdMarketDataSource.entrySet()) {
      marketDataBuilder.add(RawId.of(entry.getKey().toBundle()), entry.getValue());
    }
    marketDataBuilder.add(FxRateId.of(Currency.EUR, Currency.USD), spotRate);
    marketDataBuilder.valuationTime(valuationDate);

    MarketDataBundle marketDataBundle = new MapMarketDataBundle(marketDataBuilder.build());

    List<? extends CurveTypeConfiguration> eurCurveTypes = ImmutableList.of(
        new DiscountingCurveTypeConfiguration("EUR"));
    List<? extends CurveTypeConfiguration> usdCurveTypes = ImmutableList.of(
        new DiscountingCurveTypeConfiguration("USD"),
        new OvernightCurveTypeConfiguration(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index")));

    Map<String, List<? extends CurveTypeConfiguration>> curves = new HashMap<>();
    curves.put("FX Swap Curve", eurCurveTypes);
    curves.put("USD Discounting", usdCurveTypes);

    CurveGroupConfiguration groupConfiguration = new CurveGroupConfiguration(0, curves);

    _ccc =  new CurveConstructionConfiguration(
        "FX Swap Curve CCC",
        ImmutableList.of(groupConfiguration),
        ImmutableList.<String>of());

    RootFinderConfiguration rootFinderConfig = new RootFinderConfiguration(1e-9, 1e-9, 1000);

    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    matrix.setFixedConversion(Currency.USD, Currency.EUR, 1.6584);

    RegionSource regionSource = mock(RegionSource.class);
    SimpleRegion region = new SimpleRegion();
    region.setCurrency(Currency.USD);
    when(regionSource.getHighestLevelRegion(ExternalId.of("FINANCIAL_REGION", "US"))).thenReturn(region);

    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.<Class<?>, Object>builder()
        .put(ConventionSource.class, new MasterConventionSource(_conventionMaster))
        .put(ConfigSource.class, new MasterConfigSource(_configMaster))
        .put(HolidaySource.class, new WeekendHolidaySource())
        .put(RegionSource.class, regionSource)
        .put(HistoricalMarketDataFn.class, mock(HistoricalMarketDataFn.class))
        .put(LegalEntitySource.class, mock(LegalEntitySource.class))
        .put(ConventionBundleSource.class, mock(ConventionBundleSource.class))
        .put(CurrencyMatrix.class, matrix)
        .put(SecuritySource.class, new MasterSecuritySource(_securityMaster));

    ComponentMap components = ComponentMap.of(builder.build());

    FunctionModelConfig config =
        config(
            arguments(
                function(
                    DefaultDiscountingMulticurveBundleFn.class,
                    argument("rootFinderConfiguration", rootFinderConfig),
                    argument("impliedCurveNames", StringSet.of())),
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofDays(1))))),
            implementations(
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                DiscountingMulticurveBundleFn.class, DefaultDiscountingMulticurveBundleFn.class,
                DiscountingMulticurveBundleResolverFn.class, DefaultDiscountingMulticurveBundleResolverFn.class,
                MarketDataFn.class, DefaultMarketDataFn.class));

    _curveFunction = FunctionModel.build(DiscountingMulticurveBundleResolverFn.class, config, components);

    _env = new SimpleEnvironment(valuationDate, marketDataBundle);

    VersionCorrectionProvider vcProvider = new VersionCorrectionProvider() {
      @Override
      public VersionCorrection getPortfolioVersionCorrection() {
        return VersionCorrection.LATEST;
      }

      @Override
      public VersionCorrection getConfigVersionCorrection() {
        return VersionCorrection.LATEST;
      }
    };

    ThreadLocalServiceContext.init(ServiceContext.of(components.getComponents())
        .with(VersionCorrectionProvider.class, vcProvider));
  }

  @Test
  public void curveBuildsSuccessfully() {
    Result<MulticurveBundle> result = _curveFunction.generateBundle(_env, _ccc);
    if (!result.isSuccess()) {
      fail(failureMessage(result));
    }
  }

  private Map<ExternalId, Double> buildDataForUsdCurve() {

    ExternalId payLegConvention = ExternalId.of("CONVENTION", "USD OIS Fixed Leg");
    ExternalId receiveLegConvention = ExternalId.of("CONVENTION", "USD OIS Overnight Leg");

    ExternalId onConventionId = ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index");

    addConvention(new SwapFixedLegConvention(
        "USD OIS Fixed Leg", payLegConvention.toBundle(),
        Tenor.ONE_YEAR, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
        Currency.USD, ExternalId.of("FINANCIAL_REGION", "US"),
        2, false, StubType.SHORT_START, false, 2));

    addConvention(new OISLegConvention(
        "USD OIS Overnight Leg", receiveLegConvention.toBundle(), onConventionId,
        Tenor.ONE_YEAR, BusinessDayConventions.MODIFIED_FOLLOWING, 2, false, StubType.NONE, false, 2));

    addSecurity(new OvernightIndex("USD OIS Overnight Leg", "description", onConventionId, onConventionId.toBundle()));

    addConvention(new OvernightIndexConvention(
        "FEDL01 Index", onConventionId.toBundle(), DayCounts.ACT_360, 1, Currency.USD,
        ExternalId.of("FINANCIAL_REGION", "US")
    ));


    String mapperName = "USD Discounting Mapper";

    Set<CurveNode> nodes = ImmutableSet.<CurveNode>of(
    new SwapNode(Tenor.ofDays(0), Tenor.ONE_MONTH, payLegConvention, receiveLegConvention, mapperName),
    new SwapNode(Tenor.ofDays(0), Tenor.THREE_MONTHS, payLegConvention, receiveLegConvention, mapperName),
    new SwapNode(Tenor.ofDays(0), Tenor.SIX_MONTHS, payLegConvention, receiveLegConvention, mapperName),
    new SwapNode(Tenor.ofDays(0), Tenor.NINE_MONTHS, payLegConvention, receiveLegConvention, mapperName),
    new SwapNode(Tenor.ofDays(0), Tenor.ONE_YEAR, payLegConvention, receiveLegConvention, mapperName),
    new SwapNode(Tenor.ofDays(0), Tenor.TWO_YEARS, payLegConvention, receiveLegConvention, mapperName));


    addConfig("USD Discounting",
              new InterpolatedCurveDefinition("USD Discounting", nodes, "Linear", "FlatExtrapolator", "FlatExtrapolator"));

    Map<Tenor, CurveInstrumentProvider> swapNodeIds = ImmutableMap.<Tenor, CurveInstrumentProvider>builder()
        .put(Tenor.ONE_MONTH, createProviderForSwap(Tenor.ONE_MONTH))
        .put(Tenor.THREE_MONTHS, createProviderForSwap(Tenor.THREE_MONTHS))
        .put(Tenor.SIX_MONTHS, createProviderForSwap(Tenor.SIX_MONTHS))
        .put(Tenor.NINE_MONTHS, createProviderForSwap(Tenor.NINE_MONTHS))
        .put(Tenor.ONE_YEAR, createProviderForSwap(Tenor.ONE_YEAR))
        .put(Tenor.TWO_YEARS, createProviderForSwap(Tenor.TWO_YEARS))
        .build();

    addConfig(mapperName,
              CurveNodeIdMapper.builder()
                  .name(mapperName)
                  .swapNodeIds(swapNodeIds)
                  .build());

    return ImmutableMap.<ExternalId, Double>builder()
        .put(ExternalId.of("TICKER", "SWP_P1M"), 0.0008)
        .put(ExternalId.of("TICKER", "SWP_P3M"), 0.000875)
        .put(ExternalId.of("TICKER", "SWP_P6M"), 0.001)
        .put(ExternalId.of("TICKER", "SWP_P9M"), 0.00115)
        .put(ExternalId.of("TICKER", "SWP_P1Y"), 0.00129)
        .put(ExternalId.of("TICKER", "SWP_P2Y"), 0.0015)
        .build();
  }

  private Map<ExternalId, Double> buildDataForFxSwapCurve() {

    ExternalId swapConventionId = ExternalId.of("CONVENTION", "FX Swap Convention");
    ExternalId spotConventionId = ExternalId.of("CONVENTION", "FX Spot Convention");

    addConvention(new FXForwardAndSwapConvention(
        "FX Swap Convention", swapConventionId.toBundle(),
        spotConventionId, BusinessDayConventions.FOLLOWING, false,
        ExternalId.of("FINANCIAL_REGION", "US")));

    addConvention(new FXSpotConvention(
        "FX Spot Convention", spotConventionId.toBundle(), 2,
        ExternalId.of("FINANCIAL_REGION", "US")));

    Set<CurveNode> nodes = ImmutableSet.of(
        createFxSwapNode(swapConventionId, Tenor.ONE_WEEK),
        createFxSwapNode(swapConventionId, Tenor.ONE_MONTH),
        createFxSwapNode(swapConventionId, Tenor.THREE_MONTHS),
        createFxSwapNode(swapConventionId, Tenor.SIX_MONTHS),
        createFxSwapNode(swapConventionId, Tenor.ONE_YEAR));

    addConfig("FX Swap Curve",
              new InterpolatedCurveDefinition("FX Swap Curve", nodes, "DoubleQuadratic", "FlatExtrapolator"));

    Map<Tenor, CurveInstrumentProvider> fxSwapNodeIds = ImmutableMap.of(
        Tenor.ONE_WEEK, createProviderForFxSwap(Tenor.ONE_WEEK),
        Tenor.ONE_MONTH, createProviderForFxSwap(Tenor.ONE_MONTH),
        Tenor.THREE_MONTHS, createProviderForFxSwap(Tenor.THREE_MONTHS),
        Tenor.SIX_MONTHS, createProviderForFxSwap(Tenor.SIX_MONTHS),
        Tenor.ONE_YEAR, createProviderForFxSwap(Tenor.ONE_YEAR)
    );

    addConfig("FX Swap Mapper",
              CurveNodeIdMapper.builder()
                .name("FX Swap Mapper")
                .fxSwapNodeIds(fxSwapNodeIds)
                .build());

    return ImmutableMap.<ExternalId, Double>builder()
        .put(ExternalId.of("TICKER", "FXSWP_P7D"), -0.00082)
        .put(ExternalId.of("TICKER", "FXSWP_P1M"), -0.00085)
        .put(ExternalId.of("TICKER", "FXSWP_P3M"), -0.00093)
        .put(ExternalId.of("TICKER", "FXSWP_P6M"), -0.000925)
        .put(ExternalId.of("TICKER", "FXSWP_P1Y"), -0.00095)
        .put(ExternalId.of("TICKER", "SPOT"), 1.6584)
        .build();
  }

  private void addConfig(String name, Object configObject) {
    _configMaster.add(new ConfigDocument(ConfigItem.of(configObject, name, configObject.getClass())));
  }

  private void addSecurity(ManageableSecurity security) {
    _securityMaster.add(new SecurityDocument(security));
  }

  private void addConvention(FinancialConvention convention) {
    _conventionMaster.add(new ConventionDocument(convention));
  }

  private String failureMessage(Result<MulticurveBundle> result) {
    return result.getFailureMessage();
  }

  private CurveInstrumentProvider createProviderForFxSwap(Tenor identifier) {
    return new StaticCurvePointsInstrumentProvider(ExternalId.of("TICKER", "FXSWP_" + identifier.getPeriod().toString()), "Market_Value",
                                                   DataFieldType.POINTS, ExternalId.of("TICKER", "SPOT"),
                                                   "Market_Value");
  }

  private CurveInstrumentProvider createProviderForSwap(Tenor identifier) {
    return new StaticCurveInstrumentProvider(ExternalId.of("TICKER", "SWP_" + identifier.getPeriod().toString()),
                                             "Market_Value", DataFieldType.OUTRIGHT);
  }

  private CurveNode createFxSwapNode(ExternalId swapConventionId, Tenor maturityTenor) {
    return new FXSwapNode(Tenor.ofDays(0), maturityTenor, swapConventionId, Currency.EUR, Currency.USD,
                                    "FX Swap Mapper");
  }


}
