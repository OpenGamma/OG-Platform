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
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.irs.*;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.*;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.fra.DiscountingFRACalculatorFactory;
import com.opengamma.sesame.fra.DiscountingFRAFn;
import com.opengamma.sesame.fra.FRACalculatorFactory;
import com.opengamma.sesame.fra.FRAFn;
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
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import java.util.*;

import static com.opengamma.sesame.config.ConfigBuilder.*;

/**
 * Utility class for remote views
 */
public final class RemoteViewFraUtils {

  private RemoteViewFraUtils() { /* private constructor */ }

  /** List of Forward Rate Agreement inputs */
  public static  List<Object> INPUTS = new ArrayList<Object>() {
    {
      add(createSingleForwardRateAgreement());
    }
  };

  public static ViewColumn createFraViewColumn(String output,
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
