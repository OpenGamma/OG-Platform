/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.irfutureoption;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.CurveDefinitionCurveLabellingFn;
import com.opengamma.sesame.CurveDefinitionFn;
import com.opengamma.sesame.CurveLabellingFn;
import com.opengamma.sesame.CurveNodeConverterFn;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.CurveSelectorMulticurveBundleFn;
import com.opengamma.sesame.CurveSpecificationFn;
import com.opengamma.sesame.CurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveSpecificationFn;
import com.opengamma.sesame.DefaultCurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultFXMatrixFn;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ExposureFunctionsDiscountingMulticurveCombinerFn;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.TestMarketDataFactory;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Tests for interest rate future options analytics functions using the black calculator.
 */
@Test(groups = TestGroup.UNIT)
public class IRFutureOptionFnTest {

  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 1, 22);
  public static final LocalDate TRADE_DATE = LocalDate.of(2000, 1, 1);
  public static final OffsetTime TRADE_TIME = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
  private static final LocalDate MARKET_DATA_DATE = LocalDate.of(2014, 2, 18);
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private IRFutureOptionFn _blackIRFutureOptionFn;
  private IRFutureOptionFn _normalIRFutureOptionFn;
  private InterestRateFutureSecurity _irFuture = createIRFuture();
  private IRFutureOptionTrade _irFutureOptionTrade = createIRFutureOptionTrade();
  private FunctionRunner _functionRunner;
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(LiveMarketDataSpecification.LIVE_SPEC)
          .build();
  private static final Interpolator1D LINEAR_FLAT =
      CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                              Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                              Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(LINEAR_FLAT, LINEAR_FLAT);
  private static final InterpolatedDoublesSurface TEST_SURFACE = InterpolatedDoublesSurface.from(
      new double[] { .1, .2, .3 },
      new double[] { .1, .2, .3 },
      new double[] { .1, .2, .3 },
      INTERPOLATOR_2D
  );

  private static MarketDataEnvironment createSuppliedData() {
    LocalDateDoubleTimeSeries optionPrice = ImmutableLocalDateDoubleTimeSeries.of(VALUATION_TIME.toLocalDate(), 0.975);
    RawId<Double> optionRawId = RawId.of(ExternalSchemes.syntheticSecurityId("Test future option").toBundle());
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    builder.add(optionRawId, optionPrice);
    builder.add(VolatilitySurfaceId.of("TestExchange"), new VolatilitySurface(TEST_SURFACE));
    builder.valuationTime(VALUATION_TIME);
    return builder.build();
  }
  
  @BeforeClass
  public void setUpClass() {

    ImmutableMap<Class<?>, Object> components = generateComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);

    ComponentMap componentMap = ComponentMap.of(components);
    MarketDataSource marketDataSource = InterestRateMockSources.createMarketDataSource(MARKET_DATA_DATE, true);
    TestMarketDataFactory marketDataFactory = new TestMarketDataFactory(marketDataSource);
    ConfigLink<CurrencyMatrix> currencyMatrixLink = ConfigLink.resolved(componentMap.getComponent(CurrencyMatrix.class));
    List<MarketDataBuilder> builders = MarketDataBuilders.standard(componentMap, "dataSource", currencyMatrixLink);

    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(marketDataFactory, builders);

    _functionRunner = new FunctionRunner(environmentFactory);
    _normalIRFutureOptionFn = FunctionModel.build(IRFutureOptionFn.class, normalConfig(), componentMap);
    _blackIRFutureOptionFn = FunctionModel.build(IRFutureOptionFn.class, blackConfig(), componentMap);

  }

  private FunctionModelConfig blackConfig() {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(InterestRateMockSources.mockExposureFunctions()))),
                function(
                    RootFinderConfiguration.class,
                    argument("rootFinderAbsoluteTolerance", 1e-9),
                    argument("rootFinderRelativeTolerance", 1e-9),
                    argument("rootFinderMaxIterations", 1000)),
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1))))),
            implementations(
                IRFutureOptionFn.class, DefaultIRFutureOptionFn.class,
                IRFutureOptionCalculatorFactory.class, IRFutureOptionBlackCalculatorFactory.class,
                CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                BlackSTIRFuturesProviderFn.class, TestBlackSTIRFuturesProviderFn.class,
                DiscountingMulticurveCombinerFn.class, ExposureFunctionsDiscountingMulticurveCombinerFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                CurveLabellingFn.class, CurveDefinitionCurveLabellingFn.class,
                CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                MarketDataFn.class, DefaultMarketDataFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class));

    return config;
  }

  private FunctionModelConfig normalConfig() {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(InterestRateMockSources.mockExposureFunctions()))),
                function(
                    RootFinderConfiguration.class,
                    argument("rootFinderAbsoluteTolerance", 1e-9),
                    argument("rootFinderRelativeTolerance", 1e-9),
                    argument("rootFinderMaxIterations", 1000)),
                function(
                    TestIRFutureOptionNormalSurfaceProviderFn.class,
                    argument("moneynessOnPrice", false)),
                function(
                    DefaultCurveNodeConverterFn.class,
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1))))),
            implementations(
                IRFutureOptionFn.class, DefaultIRFutureOptionFn.class,
                IRFutureOptionCalculatorFactory.class, IRFutureOptionNormalCalculatorFactory.class,
                CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                CurveLabellingFn.class, CurveDefinitionCurveLabellingFn.class,
                CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                MarketDataFn.class, DefaultMarketDataFn.class,
                CurveSelector.class, MarketExposureSelector.class,
                IRFutureOptionNormalSurfaceProviderFn.class, TestIRFutureOptionNormalSurfaceProviderFn.class,
                DiscountingMulticurveCombinerFn.class, CurveSelectorMulticurveBundleFn.class));

    return config;
  }
  
  private ImmutableMap<Class<?>, Object> generateComponents() {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Map.Entry<Class<?>, Object> entry: InterestRateMockSources.generateBaseComponents().entrySet()) {
      Class<?> key = entry.getKey();
      if (key.equals(SecuritySource.class)) {
        appendSecuritySource((SecuritySource) entry.getValue());
      }
      builder.put(key, entry.getValue());
    }
    return builder.build();
  }

  // TODO - this assumes knowledge of the underlying source, should find a better way to do this
  private void appendSecuritySource(SecuritySource source) {
    SecurityMaster master = ((MasterSecuritySource) source).getMaster();
    master.add(new SecurityDocument(_irFuture));
  }
  
  private InterestRateFutureSecurity createIRFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2014, 6, 18), LocalTime.of(0, 0), ZoneOffset.UTC));
    String tradingExchange = "";
    String settlementExchange = "";
    Currency currency = Currency.USD;
    double unitAmount = 1000;
    ExternalId underlyingId = InterestRateMockSources.getLiborIndexId();
    String category = "";
    InterestRateFutureSecurity irFuture = new InterestRateFutureSecurity(expiry, 
                                                                         tradingExchange, 
                                                                         settlementExchange, 
                                                                         currency, 
                                                                         unitAmount, 
                                                                         underlyingId, 
                                                                         category);
    // Need this for time series lookup
    ExternalId irFutureId = ExternalSchemes.syntheticSecurityId("Test future");
    irFuture.setExternalIdBundle(irFutureId.toBundle());
    return irFuture;
  }

  private IRFutureOptionTrade createIRFutureOptionTrade() {
    
    String exchange = "TestExchange";
    ExerciseType exerciseType = new EuropeanExerciseType();
    double pointValue = Double.NaN;
    boolean margined = true;
    double strike = 0.99;
    OptionType optionType = OptionType.PUT;
    ExternalId irFutureId = Iterables.getOnlyElement(_irFuture.getExternalIdBundle());
    IRFutureOptionSecurity irFutureOption = new IRFutureOptionSecurity(exchange, 
                                                                      _irFuture.getExpiry(), 
                                                                      exerciseType, 
                                                                      irFutureId, 
                                                                      pointValue, 
                                                                      margined, 
                                                                      _irFuture.getCurrency(), 
                                                                      strike, 
                                                                      optionType);
    // Need this for time series lookup
    irFutureOption.setExternalIdBundle(ExternalSchemes.syntheticSecurityId("Test future option").toBundle());
    
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    SimpleTrade trade = new SimpleTrade(irFutureOption, tradeQuantity, counterparty, TRADE_DATE, TRADE_TIME);
    trade.setPremium(10.0);
    trade.setPremiumCurrency(Currency.USD);
    return new IRFutureOptionTrade(trade);
  }
  
  @Test
  public void testBlackPresentValue() {
    Result<MultipleCurrencyAmount> result = _functionRunner.runFunction(
        ARGS, createSuppliedData(), new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _blackIRFutureOptionFn.calculatePV(env, _irFutureOptionTrade);
          }
        });
    assertSuccess(result);

    MultipleCurrencyAmount mca = result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-972.460677, STD_TOLERANCE_PV)));
  }

  @Test
  public void testBlackBucketedZeroDelta() {
    Result<BucketedCurveSensitivities> result = _functionRunner.runFunction(
        ARGS, createSuppliedData(), new Function<Environment, Result<BucketedCurveSensitivities>>() {
          @Override
          public Result<BucketedCurveSensitivities> apply(Environment env) {
            return _blackIRFutureOptionFn.calculateBucketedZeroIRDelta(env, _irFutureOptionTrade);
          }
        });
    assertSuccess(result);
  }

  @Test
  public void testNormalPresentValue() {
    Result<MultipleCurrencyAmount> result = _functionRunner.runFunction(
        ARGS, createSuppliedData(), new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _normalIRFutureOptionFn.calculatePV(env, _irFutureOptionTrade);
          }
        });
    assertSuccess(result);
    MultipleCurrencyAmount mca = result.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(-902.7156551, STD_TOLERANCE_PV)));
  }

  @Test
  public void testNormalPrice() {
    Result<Double> result = _functionRunner.runFunction(
        ARGS, createSuppliedData(), new Function<Environment,Result<Double>>() {
          @Override
          public Result<Double> apply(Environment env) {
            return _normalIRFutureOptionFn.calculateModelPrice(env, _irFutureOptionTrade);
          }
        });
    assertSuccess(result);
    Double price = result.getValue();
    assertThat(price, is(closeTo(0.072284344, STD_TOLERANCE_PV)));
  }

  @Test
  public void testNormalBucketedZeroDelta() {
    Result<BucketedCurveSensitivities> result = _functionRunner.runFunction(
        ARGS, createSuppliedData(), new Function<Environment, Result<BucketedCurveSensitivities>>() {
          @Override
          public Result<BucketedCurveSensitivities> apply(Environment env) {
            return _normalIRFutureOptionFn.calculateBucketedZeroIRDelta(env, _irFutureOptionTrade);
          }
        });
    assertSuccess(result);
  }

}
