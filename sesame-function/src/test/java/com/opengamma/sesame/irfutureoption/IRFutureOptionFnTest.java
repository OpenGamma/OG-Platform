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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.AssertJUnit.fail;

import java.math.BigDecimal;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.ConfigDBCurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfigurationSource;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
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
import com.opengamma.sesame.CurveSpecificationFn;
import com.opengamma.sesame.CurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultCurveDefinitionFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultCurveSpecificationFn;
import com.opengamma.sesame.DefaultCurveSpecificationMarketDataFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DefaultFXMatrixFn;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.DiscountingMulticurveBundleFn;
import com.opengamma.sesame.DiscountingMulticurveBundleResolverFn;
import com.opengamma.sesame.DiscountingMulticurveCombinerFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.ExposureFunctionsDiscountingMulticurveCombinerFn;
import com.opengamma.sesame.FXMatrixFn;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.SimpleEnvironment;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MapMarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.MarketDataFn;
import com.opengamma.sesame.marketdata.RawId;
import com.opengamma.sesame.trade.IRFutureOptionTrade;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
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
  
  private static final Environment ENV = new SimpleEnvironment(VALUATION_TIME, createMarketDataBundle());

  private IRFutureOptionFn _irFutureOptionFn;

  private InterestRateFutureSecurity _irFuture = createIRFuture();
  
  private IRFutureOptionTrade _irFutureOptionTrade = createIRFutureOptionTrade();

  private static MarketDataBundle createMarketDataBundle() {
    LocalDate dataDate = LocalDate.of(2014, 2, 18);
    MarketDataEnvironmentBuilder builder = InterestRateMockSources.createMarketDataEnvironment(dataDate).toBuilder();

    LocalDate valuationDate = VALUATION_TIME.toLocalDate();
    LocalDateDoubleTimeSeries priceSeries = ImmutableLocalDateDoubleTimeSeries.of(valuationDate, 0.975);
    RawId<Double> id = RawId.of(ExternalSchemes.syntheticSecurityId("Test future option").toBundle());
    builder.add(id, priceSeries);
    return new MapMarketDataBundle(builder.build());
  }
  
  @BeforeClass
  public void setUpClass() {
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
                    argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                function(
                    DefaultDiscountingMulticurveBundleFn.class,
                    argument("impliedCurveNames", StringSet.of()))),
            implementations(
                IRFutureOptionFn.class, DefaultIRFutureOptionFn.class,
                IRFutureOptionCalculatorFactory.class, IRFutureOptionBlackCalculatorFactory.class,
                CurveSpecificationMarketDataFn.class, DefaultCurveSpecificationMarketDataFn.class,
                FXMatrixFn.class, DefaultFXMatrixFn.class,
                BlackSTIRFuturesProviderFn.class, TestBlackSTIRFuturesProviderFn.class,
                DiscountingMulticurveCombinerFn.class, ExposureFunctionsDiscountingMulticurveCombinerFn.class,
                CurveDefinitionFn.class, DefaultCurveDefinitionFn.class,
                CurveLabellingFn.class, CurveDefinitionCurveLabellingFn.class,
                DiscountingMulticurveBundleFn.class, DefaultDiscountingMulticurveBundleFn.class,
                DiscountingMulticurveBundleResolverFn.class, DefaultDiscountingMulticurveBundleResolverFn.class,
                CurveSpecificationFn.class, DefaultCurveSpecificationFn.class,
                CurveConstructionConfigurationSource.class, ConfigDBCurveConstructionConfigurationSource.class,
                CurveNodeConverterFn.class, DefaultCurveNodeConverterFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                MarketDataFn.class, DefaultMarketDataFn.class));

    ImmutableMap<Class<?>, Object> components = generateComponents();
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(components).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    
    _irFutureOptionFn = FunctionModel.build(IRFutureOptionFn.class, config, ComponentMap.of(components));
  }
  
  private ImmutableMap<Class<?>, Object> generateComponents() {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Map.Entry<Class<?>, Object> entry: InterestRateMockSources.generateBaseComponents().entrySet()) {
      Class<?> key = entry.getKey();
      if (key.equals(SecuritySource.class)) {
        appendSecuritySource((SecuritySource) entry.getValue());
      }
      if (!key.equals(HistoricalTimeSeriesSource.class)) {
        builder.put(key, entry.getValue());
      }
    }
    return builder.build();
  }

  // TODO - this assumes knowledge of the underlying source, should find a better way to do this
  private void appendSecuritySource(SecuritySource source) {
    SecurityMaster master = ((MasterSecuritySource) source).getMaster();
    master.add(new SecurityDocument(_irFuture));
  }
  
  private InterestRateFutureSecurity createIRFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2014, 6, 18), LocalTime.of(0, 0), ZoneId.systemDefault()));
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
    
    String exchange = "";
    ExerciseType exerciseType = new EuropeanExerciseType();
    double pointValue = Double.NaN;
    boolean margined = true;
    double strike = 0.01;
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
    BigDecimal tradeQuantity = BigDecimal.valueOf(10);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(irFutureOption, tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(10.0);
    trade.setPremiumCurrency(Currency.USD);
    return new IRFutureOptionTrade(trade);
  }
  
  @Test
  public void testPresentValue() {
    Result<MultipleCurrencyAmount> pvComputed = _irFutureOptionFn.calculatePV(ENV, _irFutureOptionTrade);
    if (!pvComputed.isSuccess()) {
      fail(pvComputed.getFailureMessage());
    }
  }
  
  
  @Test
  public void testBucketedZeroDelta() {
    Result<BucketedCurveSensitivities> bucketedZeroDelta = 
        _irFutureOptionFn.calculateBucketedZeroIRDelta(ENV, _irFutureOptionTrade);        
    if (!bucketedZeroDelta.isSuccess()) {
      fail(bucketedZeroDelta.getFailureMessage());
    }
    assertThat(bucketedZeroDelta.isSuccess(), is(true));
  }
}
