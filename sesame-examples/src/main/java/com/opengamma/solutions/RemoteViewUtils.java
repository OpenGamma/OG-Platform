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
import static com.opengamma.sesame.config.ConfigBuilder.output;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.instrument.annuity.CompoundingMethod;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.analytics.model.fixedincome.BucketedCurveSensitivities;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.irs.FixedInterestRateSwapLeg;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapNotional;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.irs.PayReceiveType;
import com.opengamma.financial.security.irs.Rate;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.ManageableSecurity;
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
import com.opengamma.util.GUIDGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Utility class for remote views
 */
public final class RemoteViewUtils {

  private RemoteViewUtils() { /* private constructor */ }


  /** List of Vanilla IRS inputs */
  public static  List<ManageableSecurity> VANILLA_INPUTS = new ArrayList<ManageableSecurity>() {
    {
      add(createVanillaFixedVsLiborSwap());
      add(createVanillaFixedVsONCompoundedSwap());
      add(createVanillaFFAAVsLiborSwap());
    }
  };

  /** List of Spread IRS inputs */
  public static  List<ManageableSecurity> SPREAD_INPUTS = new ArrayList<ManageableSecurity>() {
    {
      add(createSpreadFFAAVsLiborSwap());
      add(createSpreadLiborVsLiborSwap());
      add(createSpreadFixedVsONCompoundedSwap());
    }
  };

  /** List of All IRS inputs */
  public static  List<ManageableSecurity> SWAP_INPUTS = new ArrayList<ManageableSecurity>() {
    {
      addAll(VANILLA_INPUTS);
      addAll(SPREAD_INPUTS);
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
                 implementations(InterestRateSwapFn.class,
                                 DiscountingInterestRateSwapFn.class,
                                 InterestRateSwapCalculatorFactory.class,
                                 DiscountingInterestRateSwapCalculatorFactory.class)
             ),
             output(output, InterestRateSwapSecurity.class)
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

//    if (result.isSuccess()) {
//      System.out.println(label  + ": Bucketed PV01");
//
//      BucketedCurveSensitivities bcs = (BucketedCurveSensitivities) result.getValue();
//      Map sensitivities = bcs.getSensitivities();
//      Iterator entryIterator = sensitivities.entrySet().iterator();
//      while(entryIterator.hasNext()) {
//        Map.Entry entry = (Map.Entry) entryIterator.next();
//        Pair pair = (Pair) entry.getKey();
//        DoubleLabelledMatrix1D matrix = (DoubleLabelledMatrix1D) sensitivities.get(pair);
//        System.out.println("  " + pair.getFirst().toString() + ": " + pair.getSecond().toString());
//        for (int i=0; i < matrix.getLabels().length; i++) {
//          System.out.println("    " + matrix.getLabels()[i].toString() + ": " + matrix.getValues()[i]);
//        }
//      }
//    } else {
//      System.out.println(label + ": Error - " + result.getFailureMessage());
//    }

  }

  /* Sample Interest Rate Swaps */
  /* Vanilla */


  public static InterestRateSwapSecurity createVanillaFFAAVsLiborSwap() {

    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(freq3m);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq3m);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setResetPeriodFrequency(freq3m); //? Daily
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(calendarUSNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(calendarUSNY);
    payLeg.setFixingDateOffset(0);
    payLeg.setFloatingRateType(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    legs.add(payLeg);


    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla FF AA Libor 3m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  public static InterestRateSwapSecurity createSpreadFFAAVsLiborSwap() {

    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(freq3m);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq3m);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setResetPeriodFrequency(freq3m); //? Daily
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(calendarUSNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(calendarUSNY);
    payLeg.setFixingDateOffset(0);
    payLeg.setFloatingRateType(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE);
    payLeg.setCompoundingMethod(CompoundingMethod.NONE);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    payLeg.setSpreadSchedule(new Rate(0.0025));
    legs.add(payLeg);


    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);

    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread FF AA vs Libor 3m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);
  }

  public static InterestRateSwapSecurity createSpreadLiborVsLiborSwap() {
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq6m = PeriodFrequency.of(Period.ofMonths(6));
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FloatingInterestRateSwapLeg payLeg = new FloatingInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(freq6m);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq3m);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setResetPeriodFrequency(freq3m);
    payLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setResetPeriodCalendars(calendarUSNY);
    payLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setFixingDateCalendars(calendarUSNY);
    payLeg.setFixingDateOffset(-2);
    payLeg.setFloatingRateType(FloatingRateType.IBOR);
    payLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0006M Index"));
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    legs.add(payLeg);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setSpreadSchedule(new Rate(0.001));

    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread Libor 6m vs Libor 3m",
        LocalDate.of(2014, 8, 27), // effective date
        LocalDate.of(2024, 8, 27), // maturity date,
        legs);

  }

  public static InterestRateSwapSecurity createSpreadFixedVsONCompoundedSwap() { //(X)
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq1y = PeriodFrequency.of(Period.ofYears(1));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(freq1y);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq1y);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setRate(new Rate(0.00123));
    payLeg.setPaymentOffset(2);
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    legs.add(payLeg);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq1y);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq1y);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq1y); //? Daily
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(0);
    receiveLeg.setPaymentOffset(2);
    receiveLeg.setFloatingRateType(FloatingRateType.OIS);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    receiveLeg.setSpreadSchedule(new Rate(0.0010));
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Spread Fixed vs ON Compounded",
        LocalDate.of(2014, 2, 5), // effective date
        LocalDate.of(2014, 4, 5), // maturity date,
        legs);

  }


  public static InterestRateSwapSecurity createVanillaFixedVsONCompoundedSwap() { //(X)
    InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq1y = PeriodFrequency.of(Period.ofYears(1));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.ACT_360);
    payLeg.setPaymentDateFrequency(freq1y);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq1y);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setRate(new Rate(0.00123));
    payLeg.setPaymentOffset(2);
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    legs.add(payLeg);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq1y);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq1y);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq1y); //? Daily
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(0);
    receiveLeg.setPaymentOffset(2);
    receiveLeg.setFloatingRateType(FloatingRateType.OIS);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "FEDL01 Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla Fixed vs ON Compounded",
        LocalDate.of(2014, 2, 5), // effective date
        LocalDate.of(2014, 4, 5), // maturity date,
        legs);

  }

  public static InterestRateSwapSecurity createVanillaFixedVsLiborSwap() { //(X)

      InterestRateSwapNotional notional = new InterestRateSwapNotional(Currency.USD, 100_000_000);
    PeriodFrequency freq6m = PeriodFrequency.of(Period.ofMonths(6));
    PeriodFrequency freq3m = PeriodFrequency.of(Period.ofMonths(3));
    Set<ExternalId> calendarUSNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
    List<InterestRateSwapLeg> legs = new ArrayList<>();

    FixedInterestRateSwapLeg payLeg = new FixedInterestRateSwapLeg();
    payLeg.setNotional(notional);
    payLeg.setDayCountConvention(DayCounts.THIRTY_U_360);
    payLeg.setPaymentDateFrequency(freq6m);
    payLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setPaymentDateCalendars(calendarUSNY);
    payLeg.setAccrualPeriodFrequency(freq6m);
    payLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    payLeg.setAccrualPeriodCalendars(calendarUSNY);
    payLeg.setRate(new Rate(0.015));
    payLeg.setPayReceiveType(PayReceiveType.PAY);
    legs.add(payLeg);

    FloatingInterestRateSwapLeg receiveLeg = new FloatingInterestRateSwapLeg();
    receiveLeg.setNotional(notional);
    receiveLeg.setDayCountConvention(DayCounts.ACT_360);
    receiveLeg.setPaymentDateFrequency(freq3m);
    receiveLeg.setPaymentDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setPaymentDateCalendars(calendarUSNY);
    receiveLeg.setAccrualPeriodFrequency(freq3m);
    receiveLeg.setAccrualPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setAccrualPeriodCalendars(calendarUSNY);
    receiveLeg.setResetPeriodFrequency(freq3m);
    receiveLeg.setResetPeriodBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setResetPeriodCalendars(calendarUSNY);
    receiveLeg.setFixingDateBusinessDayConvention(BusinessDayConventions.PRECEDING);
    receiveLeg.setMaturityDateBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING);
    receiveLeg.setFixingDateCalendars(calendarUSNY);
    receiveLeg.setFixingDateOffset(-2);
    receiveLeg.setFloatingRateType(FloatingRateType.IBOR);
    receiveLeg.setFloatingReferenceRateId(ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"));
    receiveLeg.setPayReceiveType(PayReceiveType.RECEIVE);
    legs.add(receiveLeg);

    return new InterestRateSwapSecurity(
        ExternalIdBundle.of(ExternalId.of("UUID", GUIDGenerator.generate().toString())),
        "Vanilla Fixed vs Libor 3m",
        LocalDate.of(2014, 9, 12), // effective date
        LocalDate.of(2021, 9, 12), // maturity date,
        legs);
  }

    /* Spread */


}
