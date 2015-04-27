/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.currency;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.SimpleCurrencyMatrix;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.engine.CalculationArguments;
import com.opengamma.sesame.engine.ComponentMap;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.sesame.engine.FunctionRunner;
import com.opengamma.sesame.graph.FunctionModel;
import com.opengamma.sesame.marketdata.EmptyMarketDataFactory;
import com.opengamma.sesame.marketdata.EmptyMarketDataSpec;
import com.opengamma.sesame.marketdata.FxRateId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.builders.FxRateMarketDataBuilder;
import com.opengamma.sesame.marketdata.builders.MarketDataEnvironmentFactory;
import com.opengamma.sesame.trade.FXForwardTrade;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.function.Function;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Tests fx rates for base currency against various trades.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyRatesTest {

  private static CurrencyPair EUR_USD = CurrencyPair.of(Currency.EUR, Currency.USD);
  private static CurrencyPair EUR_GBP = CurrencyPair.of(Currency.EUR, Currency.GBP);
  private static CurrencyPair EUR_AUD = CurrencyPair.of(Currency.EUR, Currency.AUD);
  private static double EUR_USD_RATE = 1.09;
  private static double EUR_GBP_RATE = 0.72;
  private static double EUR_AUD_RATE = 1.39;
  private static double EUR_EUR_RATE = 1.0;
  private static final ZonedDateTime VALUATION_TIME = DateUtils.getUTCDate(2014, 1, 22);
  private static final MarketDataEnvironment ENV = getMarketDataEnvironment(VALUATION_TIME);
  private static final CalculationArguments ARGS =
      CalculationArguments.builder()
          .valuationTime(VALUATION_TIME)
          .marketDataSpecification(EmptyMarketDataSpec.INSTANCE)
          .build();
  private CurrencyRatesFn _function;
  private FunctionRunner _functionRunner;


  @BeforeClass
  public void setUpClass() throws IOException {

    FunctionModelConfig engineFunctionConfig =
        config(
            arguments(
                function(
                    DefaultCurrencyRatesFn.class,
                    argument("baseCurrency", Currency.EUR))
            ),
            implementations(
                CurrencyRatesFn.class, DefaultCurrencyRatesFn.class));

    SimpleCurrencyMatrix matrix = new SimpleCurrencyMatrix();
    FxRateMarketDataBuilder fxBuilder = new FxRateMarketDataBuilder(ConfigLink.<CurrencyMatrix>resolved(matrix));
    ComponentMap map = ComponentMap.EMPTY;
    VersionCorrectionProvider vcProvider = new FixedInstantVersionCorrectionProvider(Instant.now());
    ServiceContext serviceContext = ServiceContext.of(map.getComponents()).with(VersionCorrectionProvider.class, vcProvider);
    ThreadLocalServiceContext.init(serviceContext);
    EmptyMarketDataFactory dataFactory = new EmptyMarketDataFactory();
    MarketDataEnvironmentFactory environmentFactory = new MarketDataEnvironmentFactory(dataFactory, fxBuilder);
    _functionRunner = new FunctionRunner(environmentFactory);
    _function = FunctionModel.build(CurrencyRatesFn.class, engineFunctionConfig, map);
  }

  public static MarketDataEnvironment getMarketDataEnvironment(ZonedDateTime valuation) {
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    FxRateId eudUsd = FxRateId.of(EUR_USD);
    builder.add(eudUsd, EUR_USD_RATE);
    FxRateId eurGbp = FxRateId.of(EUR_GBP);
    builder.add(eurGbp, EUR_GBP_RATE);
    FxRateId eurAud = FxRateId.of(EUR_AUD);
    builder.add(eurAud, EUR_AUD_RATE);
    builder.valuationTime(valuation);
    return builder.build();
  }

  private static final InterestRateSwapNotional USD_NOTIONAL = new InterestRateSwapNotional(Currency.USD, 100_000_000);
  private static final InterestRateSwapNotional GBP_NOTIONAL = new InterestRateSwapNotional(Currency.GBP, 61_600_000);
  private static final PeriodFrequency P6M = PeriodFrequency.of(Period.ofMonths(6));
  private static final PeriodFrequency P3M = PeriodFrequency.of(Period.ofMonths(3));
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
  private static final Set<ExternalId> GBLO = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "GBLO"));

  private static InterestRateSwapSecurity createUsdGbpSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateCalendars(USNY);
    payLeg.setRate(new Rate(0.03));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(GBP_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_365);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(GBLO);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(GBLO);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(GBLO);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setMaturityDateCalendars(GBLO);
    receiveLeg.setFixingDateCalendars(GBLO);
    receiveLeg.setFixingDateOffset(0);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "BP0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "XCCY - US Fixed vs Libor BP 3m",
        LocalDate.of(2014, 1, 24), // effective date
        LocalDate.of(2021, 1, 24), // maturity date,
        legs);
  }

  private static ForwardRateAgreementSecurity createAudFra() {
    return new ForwardRateAgreementSecurity(
        Currency.AUD,
        ExternalId.of("BLOOMBERG_TICKER", "AU0003M"),
        SimpleFrequency.QUARTERLY,
        LocalDate.of(2014, 9, 12), // start date
        LocalDate.of(2014, 12, 12), // end date
        0.0125,
        -10000000,
        DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING,
        Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "AUD")),
        Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "AUD")),
        2);
  }

  private static StandardCDSSecurity createEurCds() {
    return new StandardCDSSecurity(ExternalIdBundle.of("Sample", "Bundle"),
                                   "Standard CDS",
                                   LocalDate.of(2014, 9, 20),
                                   LocalDate.of(2019, 12, 20),
                                   ExternalId.of("Sample", "Id"),
                                   new InterestRateNotional(Currency.EUR, 10_000_000),
                                   true,
                                   0.01,
                                   SeniorityLevel.SNRFOR);
  }

  private static FXForwardSecurity createGbpZarFxForward(){
    return new FXForwardSecurity(Currency.GBP,
                                 1_000_000,
                                 Currency.ZAR,
                                 16_000_000,
                                 DateUtils.getUTCDate(2019, 2, 4),
                                 ExternalSchemes.currencyRegionId(Currency.GBP));
  }

  private static FXForwardSecurity createGbpEurFxForward(){
    return new FXForwardSecurity(Currency.GBP,
                                 1_000_000,
                                 Currency.EUR,
                                 1_600_000,
                                 DateUtils.getUTCDate(2019, 2, 4),
                                 ExternalSchemes.currencyRegionId(Currency.GBP));
  }

  private static FXForwardTrade createGbpEurFxForwardTrade(){
    FXForwardSecurity security = new FXForwardSecurity(Currency.GBP,
                                                       1_000_000,
                                                       Currency.EUR,
                                                       1_600_000,
                                                       DateUtils.getUTCDate(2019, 2, 4),
                                                       ExternalSchemes.currencyRegionId(Currency.GBP));

    Trade trade = new SimpleTrade(security,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());
    return new FXForwardTrade(trade);
  }

  @Test
  public void testUsdGbpSwap() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createUsdGbpSwap());
          }
    });

    assertThat(result.isSuccess(), is(true));
    Map<Currency, Double> rates = result.getValue();
    assertThat(rates.size(), is(2));
    assertThat(rates.get(Currency.USD), is(EUR_USD_RATE));
    assertThat(rates.get(Currency.GBP), is(EUR_GBP_RATE));
    assertNull(rates.get(Currency.AUD));
  }

  @Test
  public void testAudFra() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createAudFra());
          }
        });

    assertThat(result.isSuccess(), is(true));
    Map<Currency, Double> rates = result.getValue();
    assertThat(rates.size(), is(1));
    assertThat(rates.get(Currency.AUD), is(EUR_AUD_RATE));
    assertNull(rates.get(Currency.USD));
    assertNull(rates.get(Currency.GBP));
  }

  @Test
  public void testGbpZarFxForward() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createGbpZarFxForward());
          }
        });

    // No ZAR values exist
    assertThat(result.isSuccess(), is(false));
  }

  @Test
  public void testGbpEurFxForward() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createGbpEurFxForward());
          }
        });

    assertThat(result.isSuccess(), is(true));
    Map<Currency, Double> rates = result.getValue();
    assertThat(rates.size(), is(2));
    assertThat(rates.get(Currency.GBP), is(EUR_GBP_RATE));
    assertThat(rates.get(Currency.EUR), is(EUR_EUR_RATE));
  }

  @Test
  public void testGbpEurFxForwardTrade() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createGbpEurFxForwardTrade());
          }
        });

    assertThat(result.isSuccess(), is(true));
    Map<Currency, Double> rates = result.getValue();
    assertThat(rates.size(), is(2));
    assertThat(rates.get(Currency.GBP), is(EUR_GBP_RATE));
    assertThat(rates.get(Currency.EUR), is(EUR_EUR_RATE));
  }

  @Test
  public void testEurCds() {
    Result<Map<Currency, Double>> result =
        _functionRunner.runFunction(ARGS, ENV, new Function<Environment, Result<Map<Currency, Double>>>() {
          @Override
          public Result<Map<Currency, Double>> apply(Environment env) {
            return _function.getFxRates(env, createEurCds());
          }
        });

    assertThat(result.isSuccess(), is(true));
    Map<Currency, Double> rates = result.getValue();
    assertThat(rates.size(), is(1));
    assertThat(rates.get(Currency.EUR), is(EUR_EUR_RATE));
  }

}
