/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.solutions.util;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.column;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.configureView;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;
import static com.opengamma.sesame.config.ConfigBuilder.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.sesame.CurveSelector;
import com.opengamma.sesame.DefaultFixingsFn;
import com.opengamma.sesame.FixingsFn;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.LookupIssuerProviderFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.bondfuture.BondFutureCalculatorFactory;
import com.opengamma.sesame.bondfuture.BondFutureDiscountingCalculatorFactory;
import com.opengamma.sesame.bondfuture.BondFutureFn;
import com.opengamma.sesame.bondfuture.DefaultBondFutureFn;
import com.opengamma.sesame.bondfutureoption.BlackBondFuturesProviderFn;
import com.opengamma.sesame.bondfutureoption.BlackExpStrikeBondFuturesProviderFn;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionBlackCalculatorFactory;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionCalculatorFactory;
import com.opengamma.sesame.bondfutureoption.BondFutureOptionFn;
import com.opengamma.sesame.bondfutureoption.DefaultBondFutureOptionFn;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.HistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.IssuerMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Utility class for Bond Future Options views
 */
public final class BondFutureViewUtils {

  private BondFutureViewUtils() { /* private constructor */ }
  private static String CURVE_BUNDLE = "CurveBundle";

  /**
   * Utility for creating a Bond Future Options specific view column
   * @param currencies
   */
  public static ViewConfig createViewConfig(Collection<String> currencies) {

    return
        configureView(
            "Bond Future View",
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", ConfigLink.resolved(createExposureFunction(currencies))))),
                implementations(
                    BondFutureFn.class, DefaultBondFutureFn.class,
                    BondFutureCalculatorFactory.class, BondFutureDiscountingCalculatorFactory.class,
                    IssuerProviderFn.class, LookupIssuerProviderFn.class,
                    CurveSelector.class, MarketExposureSelector.class,
                    HistoricalMarketDataFn.class, DefaultHistoricalMarketDataFn.class)),
            column(OutputNames.PRESENT_VALUE, output(OutputNames.PRESENT_VALUE, BondFutureOptionTrade.class)),
            column(OutputNames.SECURITY_MODEL_PRICE, output(OutputNames.SECURITY_MODEL_PRICE, BondFutureOptionTrade.class)),
            column(OutputNames.PV01, output(OutputNames.PV01, BondFutureOptionTrade.class)),
            column(OutputNames.BUCKETED_PV01, output(OutputNames.BUCKETED_PV01, BondFutureOptionTrade.class))
        );
  }

  private static ExposureFunctions createExposureFunction(Collection<String> currencies) {
    List<String> exposureFunctions = ImmutableList.of(CurrencyExposureFunction.NAME);
    ImmutableList<String> currencyList = ImmutableSet.copyOf(currencies).asList();
    Map<ExternalId, String> idsToNames = new HashMap<>();
    for (String currency : currencyList) {
      idsToNames.put(ExternalId.of("CurrencyISO", currency), CURVE_BUNDLE);
    }
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }


}
