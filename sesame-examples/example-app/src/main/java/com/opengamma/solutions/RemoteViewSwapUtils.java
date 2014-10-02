/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.rolldate.RollConvention;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.NotionalExchange;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.irs.StubCalculationMethod;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.ConfigDbMarketExposureSelectorFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultHistoricalTimeSeriesFn;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.trade.InterestRateSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;

/**
 * Utility class for remote views
 */
public final class RemoteViewSwapUtils {

  private RemoteViewSwapUtils() { /* private constructor */ }

  private static final InterestRateSwapNotional USD_NOTIONAL = new InterestRateSwapNotional(Currency.USD, 100_000_000);
  private static final InterestRateSwapNotional GBP_NOTIONAL = new InterestRateSwapNotional(Currency.GBP, 61_600_000);
  private static final PeriodFrequency P6M = PeriodFrequency.of(Period.ofMonths(6));
  private static final PeriodFrequency P3M = PeriodFrequency.of(Period.ofMonths(3));
  private static final PeriodFrequency P1Y = PeriodFrequency.of(Period.ofYears(1));
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
  private static final Set<ExternalId> GBLO = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "GBLO"));

  /** List of Vanilla IRS inputs */
  public static final List<Object> VANILLA_INPUTS = ImmutableList.<Object>of(
      createVanillaFixedVsLibor3mSwap());

  /** List of Compounding IRS inputs */
  public static final List<Object> COMPOUNDING_INPUTS = ImmutableList.<Object>of(
      createFixedVsONCompoundedSwap(),
      createCompoundingFFAAVsLibor3mSwap(),
      createLibor3mCompounded6mVsLibor6mSwap());

  /** List of Spread IRS inputs */
  public static final List<Object> SPREAD_INPUTS = ImmutableList.<Object>of(
      createLibor3mSpreadVsLibor6mSwap(),
      createSpreadFFAAVsLibor3mSwap());

  /** List of Fixing IRS inputs */
  public static final List<Object> FIXING_INPUTS = ImmutableList.<Object>of(
      createFixingFixedVsLibor3mSwap(),
      createFixingFixedVsONSwap());

  /** List of Stub IRS inputs */
  public static final List<Object> STUB_INPUTS = ImmutableList.<Object>of(
      createFixedVsLibor3mStub3MSwap(),
      createFixedVsLibor3mStub1MSwap(),
      createFixedVsLibor6mStub3MSwap(),
      createFixedVsLibor6mStub4MSwap(),
      createFixedVsLibor3mLongStartStub6MSwap(),
      createFixedVsLibor6mShortEndStub2MSwap());

  /** List of Cross Currency IRS inputs */
  public static final List<Object> XCCY_INPUTS = ImmutableList.<Object>of(
      createLiborBP3MVsLiborUS3MSwap(),
      createFixedUSVsLiborBP3mSwap());

  /** List of Fee IRS inputs */
  public static final List<Object> FEES_INPUT = ImmutableList.<Object>of(
      createFeeFixedVsLibor3mSwap());

  /** List of single leg IRS inputs */
  public static final List<Object> SINGLE_LEG_INPUT = ImmutableList.<Object>of(
      createSingleFixedLegSwap(),
      createSingleFloatLegSwap());

  /** List of zero coupon compounding swap inputs */
  public static final List<Object> ZERO_COUPON_COMPOUNDING_INPUT = ImmutableList.<Object>of(
      createZeroCouponCompoundingSwap());

  /** List of notional exchange swap inputs */
  //TODO REQS-462 - Interim exchange
  public static final List<Object> NOTIONAL_EXCHANGE_INPUT = ImmutableList.<Object>of(
      createInitialNotionalExchangeSwap(),
      createFinalNotionalExchangeSwap(),
      createInitialFinalNotionalExchangeSwap());

  /** List of All IRS inputs */
  public static final List<Object> SWAP_INPUTS = ImmutableList.<Object>builder()
      .addAll(VANILLA_INPUTS)
      .addAll(COMPOUNDING_INPUTS)
      .addAll(SPREAD_INPUTS)
      .addAll(FIXING_INPUTS)
      .addAll(STUB_INPUTS)
      .addAll(XCCY_INPUTS)
      .addAll(FEES_INPUT)
      .addAll(SINGLE_LEG_INPUT)
      .addAll(ZERO_COUPON_COMPOUNDING_INPUT)
      .addAll(NOTIONAL_EXCHANGE_INPUT)
      .build();

  /**
   * Utility for creating a fra specific view column
   * @param output output name, not null
   * @param exposureConfig exposure function config, not null
   * @param currencyMatrixLink currency matrix config, not null
   */
  public static ViewColumn createInterestRateSwapViewColumn(String output,
                                                            ConfigLink<ExposureFunctions> exposureConfig,
                                                            ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    ArgumentChecker.notNull(output, "output");
    ArgumentChecker.notNull(exposureConfig, "exposureConfig");
    ArgumentChecker.notNull(currencyMatrixLink, "currencyMatrixLink");

    return
        column(output,
            config(
                arguments(
                    function(ConfigDbMarketExposureSelectorFn.class,
                        argument("exposureConfig", exposureConfig)),
                    function(
                        RootFinderConfiguration.class,
                        argument("rootFinderAbsoluteTolerance", 1e-10),
                        argument("rootFinderRelativeTolerance", 1e-10),
                        argument("rootFinderMaxIterations", 1000)),
                    function(DefaultCurveNodeConverterFn.class,
                        argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                    function(DefaultHistoricalMarketDataFn.class,
                        argument("dataSource", "BLOOMBERG"),
                        argument("currencyMatrix", currencyMatrixLink)),
                    function(DefaultMarketDataFn.class,
                        argument("dataSource", "BLOOMBERG"),
                        argument("currencyMatrix", currencyMatrixLink)),
                    function(
                        DefaultHistoricalTimeSeriesFn.class,
                        argument("resolutionKey", "DEFAULT_TSS"),
                        argument("htsRetrievalPeriod", RetrievalPeriod.of((Period.ofYears(1))))),
                    function(
                        DefaultDiscountingMulticurveBundleFn.class,
                        argument("impliedCurveNames", StringSet.of()))),
                implementations(
                    InterestRateSwapFn.class,
                    DiscountingInterestRateSwapFn.class,
                    InterestRateSwapCalculatorFactory.class,
                    DiscountingInterestRateSwapCalculatorFactory.class)
            )
        );
  }

  /* Sample Interest Rate Swaps */

  private static InterestRateSwapSecurity createLibor3mCompounded6mVsLibor6mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P6M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding - Libor 3m Compounded 6m vs Libor 6m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createLibor3mSpreadVsLibor6mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P6M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setSpreadSchedule(new Rate(0.001));

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread - Libor 3m + Spread vs Libor 6m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsONCompoundedSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P1Y);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P1Y);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.00123));
    payLeg.setPaymentOffset(2);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P1Y);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P1Y);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P1Y);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(0);
    receiveLeg.setPaymentOffset(2);
    receiveLeg.setFloatingRateType(FloatingRateType.OIS);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding  - Fixed vs ON Compounded",
        LocalDate.of(2014, 2, 5), // effective date
        LocalDate.of(2014, 4, 5), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixingFixedVsONSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P1Y);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P1Y);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.00123));
    payLeg.setPaymentOffset(2);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P1Y);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P1Y);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P1Y);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(0);
    receiveLeg.setPaymentOffset(2);
    receiveLeg.setFloatingRateType(FloatingRateType.OIS);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);;

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Fixing - Fixed vs ON",
        LocalDate.of(2014, 1, 17), // effective date
        LocalDate.of(2014, 3, 17), // maturity date,
        legs);
  }

  private static InterestRateSwapTrade createFeeFixedVsLibor3mSwap() {

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2014, 1, 22);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createVanillaFixedVsLibor3mSwap(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumDate(tradeDate);
    trade.setPremiumCurrency(Currency.GBP);

    /* Fees are added as attributes on the Trade object.
    *  Multiple fees are added by grouping them in the following pattern: "FEE_{number}_{PART}.
    *  Fees are made up of four parts
    *  1. 'DATE' in the format YYYY-MM-DD
    *  2. 'CURRENCY' ISO currency code
    *  3. 'AMOUNT' fee payable
    *  4. 'DIRECTION' either 'PAY' or 'RECEIVE' */

    trade.addAttribute("FEE_1_DATE", "2014-05-22");
    trade.addAttribute("FEE_1_CURRENCY", "USD");
    trade.addAttribute("FEE_1_AMOUNT", "2000");
    trade.addAttribute("FEE_1_DIRECTION", "PAY");

    trade.addAttribute("FEE_2_DATE", "2014-03-22");
    trade.addAttribute("FEE_2_CURRENCY", "USD");
    trade.addAttribute("FEE_2_AMOUNT", "1000");
    trade.addAttribute("FEE_2_DIRECTION", "RECEIVE");

    /* A specific InterestRateSwapTrade object here is used to 'wrap' the underlying generic SimpleTrade */

    return new InterestRateSwapTrade(trade);

  }

  private static InterestRateSwapSecurity createZeroCouponCompoundingSwap() {

    // Set legs with payment frequency of NEVER and compounding method of straight
    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setCompoundingMethod(CompoundingMethod.STRAIGHT);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(PeriodFrequency.NEVER);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setCompoundingMethod(CompoundingMethod.STRAIGHT);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(PeriodFrequency.NEVER);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla - Fixed vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createVanillaFixedVsLibor3mSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla - Fixed vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixingFixedVsLibor3mSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Fixing - Fixed vs Libor 3m",
        LocalDate.of(2013, 9, 12), // effective date
        LocalDate.of(2020, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createCompoundingFFAAVsLibor3mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P3M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P3M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P3M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFloatingRateType(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding - FF AA vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2020, 9, 14), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createSpreadFFAAVsLibor3mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P3M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P3M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P3M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFloatingRateType(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setSpreadSchedule(new Rate(0.0025));

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread - FF AA + Spread vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2020, 9, 14), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsLibor3mStub3MSwap() {

    /* 21M Fixed vs Libor 3M Swap with a 3M fixed rate stub of 0.0100 on the fixed leg */

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.SHORT_START)
        .firstStubRate(0.0100);
    StubCalculationMethod stub = stubBuilder.build();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P3M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P3M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P3M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FixedInterestRateSwapLeg receiveLeg = new FixedInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P6M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setRate(new Rate(0.0100));
    receiveLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor3M - Short Start Stub 3M",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2016, 6, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsLibor3mStub1MSwap() {

    /* 22M Fixed vs Libor 3M Swap with a 1M stub with 3M rate */

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubEndDate(LocalDate.of(2014, 10, 12))
        .type(StubType.SHORT_START);
    StubCalculationMethod stub = stubBuilder.build();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P3M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P3M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P3M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    payLeg.setStubCalculationMethod(stub);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FixedInterestRateSwapLeg receiveLeg = new FixedInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P6M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setRate(new Rate(0.0100));
    receiveLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor3M - Short Start Stub 1M",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2016, 7, 12), // maturity date,
        legs);
  }


  private static InterestRateSwapSecurity createFixedVsLibor6mStub3MSwap() {

    /* 21M Fixed vs Libor 6M Swap with a 3M stub with 3M rate */

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubStartReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"))
        .firstStubEndReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"))
        .type(StubType.SHORT_START);
    StubCalculationMethod stub = stubBuilder.build();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P6M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    payLeg.setStubCalculationMethod(stub);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FixedInterestRateSwapLeg receiveLeg = new FixedInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P6M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setRate(new Rate(0.0100));
    receiveLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor6M - Short Start Stub 3M",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2016, 6, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsLibor6mStub4MSwap() {

    /* 22M Fixed vs Libor 6M Swap with a 4M stub with 3M/6M average rate */

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .firstStubStartReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"))
        .firstStubEndReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"))
        .type(StubType.SHORT_START);
    StubCalculationMethod stub = stubBuilder.build();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setResetPeriodFrequency(P6M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(USNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(USNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    payLeg.setStubCalculationMethod(stub);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FixedInterestRateSwapLeg receiveLeg = new FixedInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P6M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setRate(new Rate(0.0100));
    receiveLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor6M - Short Start Stub 4M",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2016, 7, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsLibor3mLongStartStub6MSwap() {

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()
        .type(StubType.LONG_START)
        .firstStubEndDate(LocalDate.of(2014, 9, 12))
        .firstStubStartReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"))
        .firstStubEndReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    StubCalculationMethod stub = stubBuilder.build();

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setStubCalculationMethod(stub);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    payLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor 3m - Long Start Stub 6M ",
        LocalDate.of(2014, 3, 12), // effective date
        LocalDate.of(2021, 9, 11), // maturity date,
        legs);

  }

  private static InterestRateSwapSecurity createFixedVsLibor6mShortEndStub2MSwap() {

    StubCalculationMethod.Builder stubBuilder = StubCalculationMethod.builder()

        .type(StubType.SHORT_END)
        .lastStubEndDate(LocalDate.of(2021, 11, 12))
        .lastStubStartReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0001M Index"))
        .lastStubEndReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));

    StubCalculationMethod stub = stubBuilder.build();

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setStubCalculationMethod(stub);
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P6M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P6M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P6M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    receiveLeg.setStubCalculationMethod(stub);
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Stub - Swap Fixed vs Libor 3m - Short End Stub 2M ",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 11, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createLiborBP3MVsLiborUS3MSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(GBP_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.ACT_365);
    payLeg.setPaymentDateFrequency(P3M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(GBLO);
    payLeg.setAccrualPeriodFrequency(P3M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(GBLO);
    payLeg.setResetPeriodFrequency(P3M);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(GBLO);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(GBLO);
    payLeg.setFixingDateOffset(0);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "BP0003M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setSpreadSchedule(new Rate(91.0 / 10000));
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "XCCY - Libor BP 3m vs Libor US 3m + Spread",
        LocalDate.of(2014, 1, 24), // effective date
        LocalDate.of(2021, 1, 24), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedUSVsLiborBP3mSwap() {

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

  private static InterestRateSwapSecurity createSingleFixedLegSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(new InterestRateSwapNotional(Currency.USD, 0));
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(USD_NOTIONAL);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Single leg - Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createSingleFloatLegSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(USD_NOTIONAL);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(P6M);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(USNY);
    payLeg.setAccrualPeriodFrequency(P6M);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(USNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(new InterestRateSwapNotional(Currency.USD, 0));
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(P3M);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(USNY);
    receiveLeg.setAccrualPeriodFrequency(P3M);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(USNY);
    receiveLeg.setResetPeriodFrequency(P3M);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(USNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(USNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setRollConvention(RollConvention.IMM);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    List<InterestRateSwapLeg> legs = ImmutableList.<InterestRateSwapLeg>of(payLeg, receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Single leg - Fixed",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFinalNotionalExchangeSwap() {

    NotionalExchange notionalExchange = NotionalExchange.builder().exchangeFinalNotional(true).build();
    InterestRateSwapSecurity swap = createFixedUSVsLiborBP3mSwap();
    swap.setNotionalExchange(notionalExchange);
    swap.setName("Final notional exchange - US Fixed vs Libor BP 3m");
    return swap;

  }

  private static InterestRateSwapSecurity createInitialNotionalExchangeSwap() {

    NotionalExchange notionalExchange = NotionalExchange.builder().exchangeInitialNotional(true).build();
    InterestRateSwapSecurity swap = createFixedUSVsLiborBP3mSwap();
    swap.setNotionalExchange(notionalExchange);
    swap.setName("Initial notional exchange - US Fixed vs Libor BP 3m");
    return swap;

  }

  private static InterestRateSwapSecurity createInitialFinalNotionalExchangeSwap() {

    NotionalExchange notionalExchange = NotionalExchange.builder().exchangeFinalNotional(true).
        exchangeInitialNotional(true).build();
    InterestRateSwapSecurity swap = createFixedUSVsLiborBP3mSwap();
    swap.setNotionalExchange(notionalExchange);
    swap.setName("Initial and Final notional exchange - US Fixed vs Libor BP 3m");
    return swap;

  }

}
