/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions;

import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.*;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.*;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.DiscountingInterestRateSwapFn;
import com.opengamma.sesame.irs.InterestRateSwapCalculatorFactory;
import com.opengamma.sesame.irs.InterestRateSwapFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import java.util.*;

import static com.opengamma.sesame.config.ConfigBuilder.*;

/**
 * Utility class for remote views
 */
public final class RemoteViewSwapUtils {

  private RemoteViewSwapUtils() { /* private constructor */ }

  private static InterestRateSwapNotional NOTIONAL = new InterestRateSwapNotional(Currency.USD, 100_000_000);
  private static PeriodFrequency P6M = PeriodFrequency.of(Period.ofMonths(6));
  private static PeriodFrequency P3M = PeriodFrequency.of(Period.ofMonths(3));
  private static PeriodFrequency P1Y = PeriodFrequency.of(Period.ofYears(1));
  private static Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));


  /** List of Vanilla IRS inputs */
  public static  List<Object> VANILLA_INPUTS = new ArrayList<Object>() {
    {
      add(createVanillaFixedVsLibor3mSwap());
    }
  };

  /** List of Compounding IRS inputs */
  public static  List<Object> COMPOUNDING_INPUTS = new ArrayList<Object>() {
    {
      add(createFixedVsONCompoundedSwap());
      add(createCompoundingFFAAVsLibor3mSwap());
      add(createLibor3mCompounded6mVsLibor6mSwap());
    }
  };

  /** List of Spread IRS inputs */
  public static  List<Object> SPREAD_INPUTS = new ArrayList<Object>() {
    {
      add(createLibor3mSpreadVsLibor6mSwap());
      add(createSpreadFFAAVsLibor3mSwap());
    }
  };

  /** List of Fixing IRS inputs */
  public static  List<Object> FIXING_INPUTS = new ArrayList<Object>() {
    {
      add(createFixingFixedVsLibor3mSwap());
      add(createFixingFixedVsONSwap());
    }
  };

  /** List of Stub IRS inputs */
  public static  List<Object> STUB_INPUTS = new ArrayList<Object>() {
    {
      add(createFixedVsLibor3mStub3MSwap());
      add(createFixedVsLibor3mStub1MSwap());
      add(createFixedVsLibor6mStub3MSwap());
      add(createFixedVsLibor6mStub4MSwap());
    }
  };

  /** List of All IRS inputs */
  public static  List<Object> SWAP_INPUTS = new ArrayList<Object>() {
    {
      addAll(VANILLA_INPUTS);
      addAll(COMPOUNDING_INPUTS);
      addAll(SPREAD_INPUTS);
      addAll(FIXING_INPUTS);
    }
  };

  public static ViewColumn createInterestRateSwapViewColumn(String output,
                                                            ConfigLink<ExposureFunctions> exposureConfig,
                                                            ConfigLink<CurrencyMatrix> currencyMatrixLink) {
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

  public static void outputMultipleCurrencyAmount(String label, Result result) {

    if (result.isSuccess()) {
      MultipleCurrencyAmount mca = (MultipleCurrencyAmount) result.getValue();
      System.out.println(label + ": PV "  + mca.toString());
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }
  }

  public static void outputBucketedCurveSensitivities(String label, Result result) {

    if (result.isSuccess()) {
      System.out.println(label  + ": Bucketed PV01");

      BucketedCurveSensitivities bcs = (BucketedCurveSensitivities) result.getValue();
      Map sensitivities = bcs.getSensitivities();
      Iterator entryIterator = sensitivities.entrySet().iterator();
      while(entryIterator.hasNext()) {
        Map.Entry entry = (Map.Entry) entryIterator.next();
        Pair pair = (Pair) entry.getKey();
        DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D) sensitivities.get(pair);
        System.out.println("  " + pair.getFirst().toString() + ": " + pair.getSecond().toString());
        for (int i=0; i < matrix.getLabels().length; i++) {
          System.out.println("    " + matrix.getLabels()[i].toString() + ": " + matrix.getValues()[i]);
        }
      }
    } else {
      System.out.println(label + ": Error - " + result.getFailureMessage());
    }

  }

  /* Sample Interest Rate Swaps */

  private static InterestRateSwapSecurity createLibor3mCompounded6mVsLibor6mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding - Libor 3m Compounded 6m vs Libor 6m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createLibor3mSpreadVsLibor6mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread - Libor 3m + Spread vs Libor 6m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixedVsONCompoundedSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding  - Fixed vs ON Compounded",
        LocalDate.of(2014, 2, 5), // effective date
        LocalDate.of(2014, 4, 5), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixingFixedVsONSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Fixing - Fixed vs ON",
        LocalDate.of(2014, 1, 17), // effective date
        LocalDate.of(2014, 3, 17), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createVanillaFixedVsLibor3mSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla - Fixed vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createFixingFixedVsLibor3mSwap() {

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Fixing - Fixed vs Libor 3m",
        LocalDate.of(2013, 9, 12), // effective date
        LocalDate.of(2020, 9, 12), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createCompoundingFFAAVsLibor3mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Compounding - FF AA vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2020, 9, 14), // maturity date,
        legs);
  }

  private static InterestRateSwapSecurity createSpreadFFAAVsLibor3mSwap() {

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

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
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Swap Fixed vs Libor3M - Stub 3M",
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
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Swap Fixed vs Libor3M - Stub 1M",
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
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Swap Fixed vs Libor6M - Stub 3M",
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
    payLeg.setNotional(NOTIONAL);
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
    receiveLeg.setNotional(NOTIONAL);
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

    List<InterestRateSwapLeg> legs = new ArrayList<>();
    legs.add(payLeg);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Swap Fixed vs Libor6M - Stub 4M",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2016, 7, 12), // maturity date,
        legs);
  }









}
