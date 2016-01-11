/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.equityindexoptions;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.DoublesSurface;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.analytics.math.surface.NodalDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.DefaultForwardCurveFn;
import com.opengamma.sesame.DefaultGridInterpolator2DFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ForwardCurveFn;
import com.opengamma.sesame.GridInterpolator2DFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.UnderlyingForwardCurveFn;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.equity.StaticReplicationDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataBundleFn;
import com.opengamma.sesame.equity.StrikeDataFromPriceBundleFn;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.ForwardCurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MulticurveId;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.SurfaceId;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.EquityIndexOptionTrade;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test for equity index options.
 */
@Test(groups = TestGroup.UNIT)
public class EquityIndexOptionFnTest {

  /** Tolerances */
  private static final double TOLERANCE_GREEKS = 1.0E-6;
  private static final double TOLERANCE_PV = 1.0E-2;

  /** Expected values validated Bloomberg */
  public static final double EXPECTED_PV_PRICE_SURFACE = 9150;
  public static final double EXPECTED_PV = 9159.56570;
  public static final double EXPECTED_DELTA = 0.982654738;
  public static final double EXPECTED_GAMMA = 0.001294049921;
  public static final double EXPECTED_VEGA = 3.23429443418;
  public static final double EXPECTED_PV01 = 0.1637725628;

  private EquityIndexOptionFn _functionPriceSurface;
  private EquityIndexOptionFn _functionFlatForward;
  private EquityIndexOptionFn _functionForward;
  private FunctionRunner _functionRunner;
  static final String JPY_DISCOUNTING = "JPY Discounting";
  public static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 7, 22);
  private static ExternalId ID = ExternalId.of(ExternalSchemes.BLOOMBERG_TICKER, "NK225");

  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();

  private static MarketDataEnvironment ENV = createMarketDataEnvironment();

  private static final EquityIndexOptionTrade EQUITY_INDEX_OPTION_TRADE_1 = createOptionTrade(BigDecimal.ONE);

  private static final BigDecimal POSITION_SIZE = BigDecimal.valueOf(5);
  private static final EquityIndexOptionTrade EQUITY_INDEX_OPTION_TRADE_2 = createOptionTrade(POSITION_SIZE);

  private static final MulticurveBundle createBundle() {
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    double[] time = {0.003, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0};
    double[] zc = {0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025, 0.0025};
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(time, zc, linearFlat, true, JPY_DISCOUNTING);
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.JPY, new YieldCurve(JPY_DISCOUNTING, curve));
    return new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());
  }

  private static ExposureFunctions createExposureFunction() {
    List<String> exposureFunctions =  ImmutableList.of(CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of("CurrencyISO", "JPY"), "MultiCurve");
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }

  private static MarketDataEnvironment createMarketDataEnvironment() {
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    builder.add(MulticurveId.of("MultiCurve"), createBundle());
    builder.add(RawId.of(ID.toBundle()), 19624.84);
    builder.add(ForwardCurveId.of(ID.getValue()), createForwardCurve());
    builder.add(VolatilitySurfaceId.of("CALL_NK225"), createVolatilitySurface());
    builder.add(SurfaceId.of("CALL_NK225"), createPriceSurface());
    builder.valuationTime(VALUATION_TIME);
    return builder.build();
  }

  private static EquityIndexOptionTrade createOptionTrade(BigDecimal quantity) {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createEquityIndexOptionSecurity(), quantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumCurrency(Currency.JPY);
    return new EquityIndexOptionTrade(trade);
  }

  private static EquityIndexOptionSecurity createEquityIndexOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 10500;
    Currency currency = Currency.JPY;
    ExternalId underlyingId = ID;
    ExerciseType exerciseType = ExerciseType.of("European");
    Expiry expiry = new Expiry(VALUATION_TIME.plusDays(59));
    double pointValue = 1;
    String exchange = "XJPY";
    EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType,
                                                                       strike,
                                                                       currency,
                                                                       underlyingId,
                                                                       exerciseType,
                                                                       expiry,
                                                                       pointValue,
                                                                       exchange);
    security.setName(ID.getValue() + " " + optionType.toString() + " Option " + expiry.getExpiry().toString());
    return security;
  }

  @BeforeClass
  public void setUp() {

    FunctionModelConfig configFlatForward =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(createExposureFunction())))),
            implementations(
                EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
                StaticReplicationDataBundleFn.class, StrikeDataBundleFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                ForwardCurveFn.class, UnderlyingForwardCurveFn.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class));

    FunctionModelConfig configForward =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(createExposureFunction())))),
            implementations(
                EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
                StaticReplicationDataBundleFn.class, StrikeDataBundleFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                ForwardCurveFn.class, DefaultForwardCurveFn.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class));

    FunctionModelConfig configPriceSurface =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(createExposureFunction()))),
                function(
                    DefaultGridInterpolator2DFn.class,
                    argument("xInterpolatorName", "Linear"),
                    argument("xLeftExtrapolatorName", "FlatExtrapolator"),
                    argument("xRightExtrapolatorName", "FlatExtrapolator"),
                    argument("yInterpolatorName", "Linear"),
                    argument("yLeftExtrapolatorName", "FlatExtrapolator"),
                    argument("yRightExtrapolatorName", "FlatExtrapolator"))
            ),
            implementations(
                GridInterpolator2DFn.class, DefaultGridInterpolator2DFn.class,
                EquityIndexOptionFn.class, DefaultEquityIndexOptionFn.class,
                StaticReplicationDataBundleFn.class, StrikeDataFromPriceBundleFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                ForwardCurveFn.class, DefaultForwardCurveFn.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class));

    ImmutableMap<Class<?>, Object> components = generateBaseComponents();

    ComponentMap componentMap = ComponentMap.of(components);
    _functionFlatForward = FunctionModel.build(EquityIndexOptionFn.class, configFlatForward, componentMap);
    _functionForward = FunctionModel.build(EquityIndexOptionFn.class, configForward, componentMap);
    _functionPriceSurface = FunctionModel.build(EquityIndexOptionFn.class, configPriceSurface, componentMap);

    EmptyMarketDataFactory dataFactory = new EmptyMarketDataFactory();
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolved(componentMap.getComponent(CurrencyMatrix.class));
    List<MarketDataBuilder> builders = MarketDataBuilders.standard(componentMap, "dataSource", currencyMatrixLink);
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(dataFactory, builders);
    _functionRunner = new FunctionRunner(environmentFactory);
  }

  private static ImmutableMap<Class<?>, Object> generateBaseComponents() {
    return generateComponentMap(mock(RegionSource.class),
                                mock(HolidaySource.class),
                                mock(HistoricalTimeSeriesSource.class),
                                mock(SecuritySource.class),
                                mock(ConfigSource.class),
                                mock(ConventionBundleSource.class),
                                mock(LegalEntitySource.class),
                                mock(CurrencyMatrix.class));
  }

  private static ImmutableMap<Class<?>, Object> generateComponentMap(Object... components) {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Object component : components) {
      builder.put(component.getClass().getInterfaces()[0], component);
    }
    return builder.build();
  }

  private static VolatilitySurface createVolatilitySurface() {
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(linearFlat, linearFlat);
    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(
        new double[] {31d/356, 59d/365, 94d/365, 31d/356, 59d/365, 94d/365},
        new double[] {20500, 10500, 12750, 19625, 10750, 13000},
        new double[] {17.3885/100, 78.7594/100, 39.8789/100, 19.0244/100, 76.0916/100, 38.2484/100},
        interpolator2D
    );
    return new VolatilitySurface(surface);
  }

  private static DoublesSurface createPriceSurface() {
    return new NodalDoublesSurface(
        new double[] {31d/356, 59d/365, 94d/365, 31d/356, 59d/365, 94d/365},
        new double[] {20500, 10500, 12750, 19625, 10750, 13000},
        new double[] {110, 9150, 6880, 435, 8900, 6630}
    );
  }

  private static ForwardCurve createForwardCurve() {
    double[] maturities = {31d/365, 122d/365, 213d/365, 304d/365, 395d/365, 486d/365, 577d/365, 759d/365,
                           941d/365, 1123d/365, 1312d/365, 1494d/365, 1676d/365};
    double[] prices = {19627, 19612, 19518, 19498, 19389, 19374, 19279, 19151, 19043, 18917, 18812, 18687, 18582};
    Interpolator1D interpolator = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
        Interpolator1DFactory.LINEAR,
        Interpolator1DFactory.LINEAR_EXTRAPOLATOR,
        Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    return new ForwardCurve(InterpolatedDoublesCurve.from(maturities, prices, interpolator));
  }

  @Test
  public void testPresentValueWithPriceSurface() {
    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _functionPriceSurface.calculatePv(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getAmount(), is(closeTo(EXPECTED_PV_PRICE_SURFACE, TOLERANCE_PV)));
  }

  @Test
  public void testPresentValueWithFlatForwardCurve() {
    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _functionFlatForward.calculatePv(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
  }

  @Test
  public void testPresentValueWithForwardCurve() {
    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _functionForward.calculatePv(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getAmount(), is(closeTo(EXPECTED_PV, TOLERANCE_PV)));
  }

  @Test
  public void testPresentValuePositionScalingWithForwardCurve() {
    Result<CurrencyAmount> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<CurrencyAmount>>() {
          @Override
          public Result<CurrencyAmount> apply(Environment env) {
            return _functionForward.calculatePv(env, EQUITY_INDEX_OPTION_TRADE_2);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue().getAmount(), is(closeTo(POSITION_SIZE.doubleValue() * EXPECTED_PV, TOLERANCE_PV)));
  }

  @Test
  public void testDeltaWithForwardCurve() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _functionForward.calculateDelta(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(closeTo(EXPECTED_DELTA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testGammaWithForwardCurve() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _functionForward.calculateGamma(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(closeTo(EXPECTED_GAMMA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testVegaWithForwardCurve() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _functionForward.calculateVega(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(closeTo(EXPECTED_VEGA, TOLERANCE_GREEKS)));
  }

  @Test
  public void testPV01WithForwardCurve() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _functionForward.calculatePv01(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(closeTo(EXPECTED_PV01, TOLERANCE_GREEKS)));
  }

  @Test
  public void testPV01PositionScalingWithForwardCurve() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment, Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _functionForward.calculatePv01(env, EQUITY_INDEX_OPTION_TRADE_2);
          }
        });
    assertThat(result.isSuccess(), is(true));
    assertThat(result.getValue(), is(closeTo(POSITION_SIZE.doubleValue() * EXPECTED_PV01, TOLERANCE_GREEKS)));
  }

  @Test
  public void testBucketedPV01WithForwardCurve() {
    Result<BucketedCurveSensitivities> result = _functionRunner.runFunction(
        ARGS, ENV,
        new Function<Environment,Result<BucketedCurveSensitivities>>() {
          @Override
          public Result<BucketedCurveSensitivities> apply(Environment env) {
            return _functionForward.calculateBucketedPv01(env, EQUITY_INDEX_OPTION_TRADE_1);
          }
        });
    assertThat(result.isSuccess(), is(true));
  }

}
