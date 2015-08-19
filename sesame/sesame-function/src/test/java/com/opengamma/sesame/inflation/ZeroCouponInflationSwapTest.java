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
import static com.opengamma.util.result.ResultTestUtils.assertSuccess;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.Notional;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.TestMarketDataFactory;
import com.opengamma.sesame.cache.FunctionCache;
import com.opengamma.sesame.cache.NoOpFunctionCache;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.interestrate.InterestRateMockSources;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.MarketDataSource;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataBuilders;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

@Test(groups = TestGroup.UNIT)
public class ZeroCouponInflationSwapTest {

  private static final double EXPECTED_PV = 42;
  private static final double STD_TOLERANCE_PV = 1.0E-3;
  private static final LocalDate MARKET_DATA_DATE = LocalDate.of(2014, 1, 22);
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 1, 22);
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(LiveMarketDataSpecification.LIVE_SPEC)
          .build();

  private FunctionRunner _functionRunner;
  private ZeroCouponInflationSwapFn _function;
  private ZeroCouponInflationSwapTrade _trade = createZCInflationTrade();

  private static final ZonedDateTime TRADE_DATE = DateUtils.getUTCDate(2013, 7, 1);
  private static final ZonedDateTime EFFECTIVE_DATE = DateUtils.getUTCDate(2013, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtils.getUTCDate(2023, 7, 1);
  private static final String COUNTERPARTY = "OG";
  private static final DayCount DC = DayCounts.ACT_360;
  private static final Frequency FREQUENCY = SimpleFrequency.SEMI_ANNUAL;
  private static final ExternalId REGION_ID = ExternalId.of("Test", "US");
  private static final BusinessDayConvention BDC = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final Notional NOTIONAL = new InterestRateNotional(Currency.USD, 10000);
  private static final boolean EOM = true;

  private ZeroCouponInflationSwapTrade createZCInflationTrade() {
    FixedInflationSwapLeg payLeg = new FixedInflationSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, 0.002);
    InflationIndexSwapLeg receiveLeg =
        new InflationIndexSwapLeg(DC, FREQUENCY, REGION_ID, BDC, NOTIONAL, EOM, ExternalId.of("Test","SDF"),
                                  2, 3, InterpolationMethod.MONTH_START_LINEAR);
    ZeroCouponInflationSwapSecurity security = new ZeroCouponInflationSwapSecurity(TRADE_DATE,
                                                                                   EFFECTIVE_DATE,
                                                                                   MATURITY,
                                                                                   COUNTERPARTY,
                                                                                   payLeg,
                                                                                   receiveLeg);

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(10);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(security, tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumCurrency(Currency.USD);
    return new ZeroCouponInflationSwapTrade(trade);

  }

  @BeforeClass
  public void setUpClass() throws IOException {
    FunctionModelConfig config =
        config(
            arguments(
                function(
                    MarketExposureSelector.class,
                    argument("exposureFunctions", ConfigLink.resolved(InterestRateMockSources.mockExposureFunctions())))),
            implementations(
                InflationSwapConverterFn.class, DefaultInflationSwapConverterFn.class,
                ZeroCouponInflationSwapFn.class, DiscountingZeroCouponInflationSwapFn.class,
                HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class,
                FixingsFn.class, DefaultFixingsFn.class,
                FunctionCache.class, NoOpFunctionCache.class
            ));

    Map<Class<?>, Object> components = InterestRateMockSources.generateBaseComponents();
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
    _function = FunctionModel.build(ZeroCouponInflationSwapFn.class, config, ComponentMap.of(components));
  }

  @Test
  public void testCalculatePV() throws Exception {
    Result<MultipleCurrencyAmount> resultPV = _functionRunner.runFunction(
        ARGS, new Function<Environment, Result<MultipleCurrencyAmount>>() {
          @Override
          public Result<MultipleCurrencyAmount> apply(Environment env) {
            return _function.calculatePV(env, _trade);
          }
        });
    assertSuccess(resultPV);

    MultipleCurrencyAmount mca = resultPV.getValue();
    assertThat(mca.getCurrencyAmount(Currency.USD).getAmount(), is(closeTo(EXPECTED_PV, STD_TOLERANCE_PV)));

  }
}