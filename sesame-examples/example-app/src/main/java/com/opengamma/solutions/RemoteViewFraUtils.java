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

import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.sesame.ConfigDbMarketExposureSelectorFn;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultDiscountingMulticurveBundleFn;
import com.opengamma.sesame.DefaultHistoricalTimeSeriesFn;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.fra.DiscountingFRACalculatorFactory;
import com.opengamma.sesame.fra.DiscountingFRAFn;
import com.opengamma.sesame.fra.FRACalculatorFactory;
import com.opengamma.sesame.fra.FRAFn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Utility class for remote views
 */
public final class RemoteViewFraUtils {

  private RemoteViewFraUtils() { /* private constructor */ }

  /** List of Forward Rate Agreement inputs */
  public static final List<Object> INPUTS = ImmutableList.<Object>of(createSingleForwardRateAgreement());

  /**
   * Utility for creating a fra specific view column
   * @param output output name, not null
   * @param exposureConfig exposure function config, not null
   * @param currencyMatrixLink currency matrix config, not null
   */
  public static ViewColumn createFraViewColumn(String output,
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
                    FRAFn.class,
                    DiscountingFRAFn.class,
                    FRACalculatorFactory.class,
                    DiscountingFRACalculatorFactory.class)
            )
        );
  }

  /* Sample Forward Rate Agreements */

  private static ForwardRateAgreementSecurity createSingleForwardRateAgreement() {

    return new ForwardRateAgreementSecurity(
        Currency.USD,
        ExternalId.of("BLOOMBERG_TICKER", "US0003M Index"),
        SimpleFrequency.QUARTERLY,
        LocalDate.of(2014, 9, 12), // start date
        LocalDate.of(2014, 12, 12), // end date
        0.0125,
        -10000000,
        DayCounts.ACT_360,
        BusinessDayConventions.MODIFIED_FOLLOWING,
        Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY")),
        Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY")),
        2);
  }

}
