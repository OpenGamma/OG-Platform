/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.inflation;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.util.money.Currency.USD;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.mockito.Mockito.mock;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.model.interestrate.curve.PriceIndexCurveSimple;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.IdentityMatrix;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.ircurve.strips.ContinuouslyCompoundedRateNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurveDefinitionCurveLabellingFn;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.InflationProviderBundle;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.sesame.cache.NoOpFunctionCache;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.InflationMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeriesBuilder;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

@Test(groups = TestGroup.UNIT)
public class ZeroCouponInflationSwapTest {

  /**
   * PV matching SwapZeroCouponInflationDiscountingUsdE2ETest
   */
  private static final double EXPECTED_PV1 = 0d;
  private static final double EXPECTED_PV2 = 557423.3817;
  private static final double STD_TOLERANCE_PV = 1.0E-3;

  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 10, 9);
  private static final String CURVE_BUNDLE = "Curve Bundle";
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();

  private FunctionRunner _functionRunner;
  private ZeroCouponInflationSwapFn _function;
  private ZeroCouponInflationSwapTrade _trade1 = createZCInflationTrade(DateUtils.getUTCDate(2014, 10, 9),
                                                                        DateUtils.getUTCDate(2016, 10, 11),
                                                                        0.02);

  private ZeroCouponInflationSwapTrade _trade2 = createZCInflationTrade(DateUtils.getUTCDate(2014, 1, 8),
                                                                        DateUtils.getUTCDate(2019, 1, 10),
                                                                        0.01);

  private static final ExternalId CONVENTION_ID = ExternalId.of("CONVENTION", "TEST");
  private static final ExternalId REGION_ID =  ExternalSchemes.financialRegionId("US");

  private static final Interpolator1D LINEAR_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  // Discounting Curve
  private static final String USD_DSC_NAME = "USD Dsc";
  private static final double[] USD_DSC_TIME = new double[] {0.0027397260273972603, 0.0136986301369863, 0.1095890410958904,
                                                             0.18904109589041096, 0.27123287671232876, 0.5178082191780822,
                                                             0.7671232876712328, 1.0191780821917809, 2.025218953514485,
                                                             3.0246575342465754, 4.021917808219178, 5.019178082191781,
                                                             6.019754472640168, 7.024657534246575, 8.024657534246575,
                                                             9.024657534246575, 10.019754472640168};
  private static final double[] USD_DSC_RATE = new double[] {0.0016222186172986138, 0.001622209965572477, 7.547616096755544E-4,
                                                             9.003947315389025E-4, 9.833562990057003E-4, 9.300905368344651E-4,
                                                             0.0010774349342544426, 0.001209299356175582, 0.003243498783874946,
                                                             0.007148138535707508, 0.011417234937364525, 0.015484713638367467,
                                                             0.01894872475170524, 0.02177798040124286, 0.024146976832379798,
                                                             0.02610320121432829, 0.027814843351943817 };
  private static final YieldAndDiscountCurve USD_DSC_CURVE =
      new YieldCurve(USD_DSC_NAME, new InterpolatedDoublesCurve(USD_DSC_TIME,
                                                                USD_DSC_RATE,
                                                                LINEAR_FLAT,
                                                                true,
                                                                USD_DSC_NAME));

  // Index Curve
  private static final String USD_INDEX_NAME = "US CPI-U";
  private static ExternalId PRICE_INDEX_ID = ExternalId.of("SEC", USD_INDEX_NAME);
  private static final double[] USD_INDEX_VALUE = new double[]  {242.88404516129032, 248.03712245417105, 252.98128118335094,
                                                                 258.0416354687366, 263.20242369585515, 268.4653023378886,
                                                                 273.83617795725064, 279.3124974961296, 284.8987721100803,
                                                                 290.5954768446179, 302.3336095056465, 320.8351638061777,
                                                                 354.2203489141063, 391.08797576744865, 431.7913437911175};
  private static final double[] USD_INDEX_TIME = new double[] {0.893150684931507, 1.894071412530878, 2.893150684931507,
                                                               3.893150684931507, 4.893150684931507, 5.894071412530878,
                                                               6.893150684931507, 7.893150684931507, 8.893150684931507,
                                                               9.894071412530877, 11.893150684931507, 14.893150684931507,
                                                               19.893150684931506, 24.893150684931506, 29.894071412530877 };

  private static final PriceIndexCurveSimple USD_PRICE_INDEX_CURVE =
      new PriceIndexCurveSimple(InterpolatedDoublesCurve.from(USD_INDEX_TIME,
                                                              USD_INDEX_VALUE,
                                                              LINEAR_FLAT,
                                                              USD_INDEX_NAME));


  private static InflationProviderBundle buildCurveBundle() {
    IndexPrice PRICE_INDEX_USD = new IndexPrice(USD_INDEX_NAME, USD);
    InflationIssuerProviderDiscount provider = new InflationIssuerProviderDiscount();
    provider.setCurve(USD, USD_DSC_CURVE);
    provider.setCurve(PRICE_INDEX_USD, USD_PRICE_INDEX_CURVE);

    CurveBuildingBlockBundle blockBundle = new CurveBuildingBlockBundle();
    blockBundle.add(USD_DSC_NAME,
                    buildCurveBlock(USD_DSC_NAME, USD_DSC_CURVE.getNumberOfParameters()),
                    buildJacobian(USD_DSC_CURVE.getNumberOfParameters()));
    blockBundle.add(USD_INDEX_NAME,
                    buildCurveBlock(USD_INDEX_NAME, USD_PRICE_INDEX_CURVE.getNumberOfParameters()),
                    buildJacobian(USD_PRICE_INDEX_CURVE.getNumberOfParameters()));

    return InflationProviderBundle.builder()
            .curveBuildingBlockBundle(blockBundle)
            .parameterInflationProvider(provider)
            .build();
  }

  private static DoubleMatrix2D buildJacobian(int params) {
    // have pre-calibrated curve so jacobian is the identity matrix
    return new IdentityMatrix(params);
  }

  private static CurveBuildingBlock buildCurveBlock(String configCurveName, int params) {
    LinkedHashMap<String, Pair<Integer, Integer>> curveBlock = Maps.newLinkedHashMap();
    curveBlock.put(configCurveName, Pairs.of(0, params));
    return new CurveBuildingBlock(curveBlock);
  }

  private static MarketDataEnvironment getMarketDataEnvironment() {
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    builder.add(InflationMulticurveId.of(CURVE_BUNDLE), buildCurveBundle());
    builder.add(RawId.of(PRICE_INDEX_ID.toBundle()), createFlatTimeSeries());
    builder.valuationTime(VALUATION_TIME);
    return builder.build();
  }

  private ZeroCouponInflationSwapTrade createZCInflationTrade(ZonedDateTime tradeDate,
                                                              ZonedDateTime maturity,
                                                              double rate) {

    ZonedDateTime effectiveDate = tradeDate.plusDays(2);
    String counterpartyName = "TEST";
    Frequency frequency = SimpleFrequency.SEMI_ANNUAL;
    BusinessDayConvention convention = BusinessDayConventions.MODIFIED_FOLLOWING;
    Notional notional = new InterestRateNotional(USD, 10_000_000);
    boolean eom = true;
    SimpleCounterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, counterpartyName));

    FixedInflationSwapLeg payLeg = new FixedInflationSwapLeg(DayCounts.ACT_360,
                                                             frequency,
                                                             REGION_ID,
                                                             convention,
                                                             notional,
                                                             eom,
                                                             rate);

    InflationIndexSwapLeg receiveLeg = new InflationIndexSwapLeg(DayCounts.ACT_360,
                                                                 frequency,
                                                                 REGION_ID,
                                                                 convention,
                                                                 notional,
                                                                 eom,
                                                                 PRICE_INDEX_ID,
                                                                 3,
                                                                 3,
                                                                 InterpolationMethod.MONTH_START_LINEAR);

    ZeroCouponInflationSwapSecurity security = new ZeroCouponInflationSwapSecurity(tradeDate,
                                                                                   effectiveDate,
                                                                                   maturity,
                                                                                   counterpartyName,
                                                                                   payLeg,
                                                                                   receiveLeg);

    SimpleTrade trade = new SimpleTrade(security,
                                        BigDecimal.valueOf(1),
                                        counterparty,
                                        LocalDate.of(2000, 1, 1),
                                        OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC));
    trade.setPremium(0.0);
    trade.setPremiumCurrency(USD);
    return new ZeroCouponInflationSwapTrade(trade);
  }

  private static ImmutableMap<Class<?>, Object> generateBaseComponents() {
    return generateComponentMap(holidaySource(),
                                regionSource(),
                                mock(ConventionSource.class),
                                mock(ConventionBundleSource.class),
                                mock(LegalEntitySource.class),
                                configSource(),
                                securitySource(),
                                mock(HistoricalTimeSeriesSource.class),
                                mock(HistoricalTimeSeriesResolver.class),
                                mock(CurrencyMatrix.class));
  }

  private static HolidaySource holidaySource() {
    return new WeekendHolidaySource();
  }

  private static LocalDateDoubleTimeSeries createFlatTimeSeries() {
    LocalDateDoubleTimeSeriesBuilder seriesBuilder = ImmutableLocalDateDoubleTimeSeries.builder();
    //see StandardTimeSeriesInflationDataSets for full set
    seriesBuilder.put(LocalDate.of(2013, 10, 31), 233.546);
    seriesBuilder.put(LocalDate.of(2013, 11, 30), 233.069);
    seriesBuilder.put(LocalDate.of(2014, 7, 31), 238.25);
    seriesBuilder.put(LocalDate.of(2014, 8, 31), 237.852);
    return seriesBuilder.build();
  }

  private static RegionSource regionSource() {
    RegionMaster regionMaster = new InMemoryRegionMaster();
    SimpleRegion regionUs = new SimpleRegion();
    regionUs.addExternalId(REGION_ID);
    regionUs.addExternalId(ExternalSchemes.currencyRegionId(USD));
    regionUs.setUniqueId(UniqueId.of("REGION", "1"));
    regionMaster.add(new RegionDocument(regionUs));
    return new MasterRegionSource(regionMaster);
  }

  private static CurveNode buildCurveNode(Tenor tenor) {
    //Not the correct node to use for inflation, but in this case it is only used for labeling
    return new ContinuouslyCompoundedRateNode("Mapper", tenor, tenor.toFormattedString().substring(1));
  }

  private static ConfigSource configSource() {
    ConfigMaster configMaster = new InMemoryConfigMaster();

    Set<CurveNode> indexNodes = new LinkedHashSet<>();
    for (double years : USD_INDEX_TIME) {
      Tenor tenor = Tenor.parse("P" + Math.round(years * 365) + "D");
      indexNodes.add(buildCurveNode(tenor));
    }

    Set<CurveNode> discountingNodes = new LinkedHashSet<>();
    for (double years : USD_DSC_TIME) {
      Tenor tenor = Tenor.parse("P" + Math.round(years * 365) + "D");
      discountingNodes.add(buildCurveNode(tenor));
    }

    CurveDefinition indexDefinition = new CurveDefinition(USD_INDEX_NAME, indexNodes);
    CurveDefinition discountingDefinition = new CurveDefinition(USD_DSC_NAME, discountingNodes);

    configMaster.add(new ConfigDocument(ConfigItem.of(indexDefinition)));
    configMaster.add(new ConfigDocument(ConfigItem.of(discountingDefinition)));
    return new MasterConfigSource(configMaster);
  }

  private static SecuritySource securitySource() {
    SecurityMaster securityMaster = new InMemorySecurityMaster();
    PriceIndex priceIndex = new PriceIndex(USD_INDEX_NAME, CONVENTION_ID);
    priceIndex.setUniqueId(UniqueId.of("SEC", "1"));
    priceIndex.setExternalIdBundle(PRICE_INDEX_ID.toBundle());
    securityMaster.add(new SecurityDocument(priceIndex));
    return new MasterSecuritySource(securityMaster);
  }

  private static ImmutableMap<Class<?>, Object> generateComponentMap(Object... components) {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Object component : components) {
      builder.put(component.getClass().getInterfaces()[0], component);
    }
    return builder.build();
  }

  public static ExposureFunctions exposureFunction() {
    List<String> exposureFunctions =  ImmutableList.of(CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of("CurrencyISO", "USD"), CURVE_BUNDLE);
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }

  @BeforeClass
  public void setUpClass() throws IOException {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(exposureFunction())))),
            implementations(
                InflationSwapConverterFn.class, DefaultInflationSwapConverterFn.class,
                ZeroCouponInflationSwapFn.class, DiscountingZeroCouponInflationSwapFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                CurveLabellingFn.class, CurveDefinitionCurveLabellingFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                FunctionCache.class, NoOpFunctionCache.class
            ));

    Map<Class<?>, Object> components = generateBaseComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    ComponentMap componentMap = ComponentMap.of(components);
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolved(componentMap.getComponent(CurrencyMatrix.class));
    List<MarketDataBuilder> builders = MarketDataBuilders.standard(componentMap, "dataSource", currencyMatrixLink);
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(new EmptyMarketDataFactory(), builders);
    _functionRunner = new FunctionRunner(environmentFactory);
    _function = FunctionModel.build(ZeroCouponInflationSwapFn.class, config, ComponentMap.of(components));
  }

  @Test
  public void testCalculatePVTrade1() throws Exception {

    MarketDataEnvironment ENV = getMarketDataEnvironment();
    Result<MultipleCurrencyAmount> resultPV = _functionRunner.runFunction(
        ARGS, ENV, new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _function.calculatePV(env, _trade1);
          }
        });
    assertSuccess(resultPV);

    MultipleCurrencyAmount mca = resultPV.getValue();
    assertThat(mca.getCurrencyAmount(USD).getAmount(), is(closeTo(EXPECTED_PV1, STD_TOLERANCE_PV)));

  }

  @Test
  public void testCalculateBucketedPV01Trade1() throws Exception {

    MarketDataEnvironment ENV = getMarketDataEnvironment();
    Result<BucketedCurveSensitivities> result = _functionRunner.runFunction(
        ARGS, ENV, new Function<Environment, Result<BucketedCurveSensitivities>>() {
          @Override
          public Result<BucketedCurveSensitivities> apply(Environment env) {
            return _function.calculateBucketedPV01(env, _trade1);
          }
        });
    assertSuccess(result);

    BucketedCurveSensitivities mcps = result.getValue();

    double[] expectedDsc = new double[] {0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d};
    double[] expectedIndex = new double[] {0.2391924970613193, 3.9334293371931075, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d };

    assertArrayRelative("BucketedPV01Trade1 Index",
                        expectedIndex,
                        mcps.getSensitivities().get(Pairs.of(USD_INDEX_NAME, USD)).getValues(),
                        STD_TOLERANCE_PV);

    assertArrayRelative("BucketedPV01Trade1 Discounting",
                        expectedDsc,
                        mcps.getSensitivities().get(Pairs.of(USD_DSC_NAME, USD)).getValues(),
                        STD_TOLERANCE_PV);
  }

  @Test
  public void testCalculatePVTrade2() throws Exception {

    MarketDataEnvironment ENV = getMarketDataEnvironment();
    Result<MultipleCurrencyAmount> resultPV = _functionRunner.runFunction(
        ARGS, ENV, new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _function.calculatePV(env, _trade2);
          }
        });
    assertSuccess(resultPV);

    MultipleCurrencyAmount mca = resultPV.getValue();
    assertThat(mca.getCurrencyAmount(USD).getAmount(), is(closeTo(EXPECTED_PV2, STD_TOLERANCE_PV)));

  }

  @Test
  public void testCalculateBucketedPV01Trade2() throws Exception {

    MarketDataEnvironment ENV = getMarketDataEnvironment();
    Result<BucketedCurveSensitivities> result = _functionRunner.runFunction(
        ARGS, ENV, new Function<Environment, Result<BucketedCurveSensitivities>>() {
          @Override
          public Result<BucketedCurveSensitivities> apply(Environment env) {
            return _function.calculateBucketedPV01(env, _trade2);
          }
        });
    assertSuccess(result);

    BucketedCurveSensitivities mcps = result.getValue();

    double[] expectedDsc = new double[] {0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, -181.78857803005081, -55.38361696256043, 0d, 0d, 0d, 0d, 0d };
    double[] expectedIndex = new double[] {0d, 0d, 0d, 3.2884348703648847, 0.7763062873998817, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d };

    assertArrayRelative("BucketedPV01Trade2 Index",
                        expectedIndex,
                        mcps.getSensitivities().get(Pairs.of(USD_INDEX_NAME, USD)).getValues(),
                        STD_TOLERANCE_PV);

    assertArrayRelative("BucketedPV01Trade2 Discounting",
                        expectedDsc,
                        mcps.getSensitivities().get(Pairs.of(USD_DSC_NAME, USD)).getValues(),
                        STD_TOLERANCE_PV);

  }

  private static void assertArrayRelative(String message, double[] expected, double[] obtained, double relativeTol) {
    int nData = expected.length;
    assertEquals(message, nData, obtained.length);
    for (int i = 0; i < nData; ++i) {
      double ref = Math.max(Math.abs(expected[i]), 1.0);
      assertThat(message, obtained[i], is(closeTo(expected[i], ref * relativeTol)));
    }
  }

}