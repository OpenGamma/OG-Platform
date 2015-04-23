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

import java.util.List;
import java.util.Set;

import com.opengamma.sesame.credit.curve.CreditCurveDataProviderFn;
import com.opengamma.sesame.credit.curve.YieldCurveDataProviderFn;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.link.SnapshotLink;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.OutputNames;
import com.opengamma.sesame.config.FunctionModelConfig;
import com.opengamma.sesame.config.ViewConfig;
import com.opengamma.sesame.credit.DefaultIsdaCompliantYieldCurveFn;
import com.opengamma.sesame.credit.IsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.IsdaCompliantYieldCurveFn;
import com.opengamma.sesame.credit.StandardIsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.config.CreditCurveDataKeyMap;
import com.opengamma.sesame.credit.config.RestructuringSettings;
import com.opengamma.sesame.credit.converter.DefaultIndexCdsConverterFn;
import com.opengamma.sesame.credit.converter.DefaultLegacyCdsConverterFn;
import com.opengamma.sesame.credit.converter.DefaultStandardCdsConverterFn;
import com.opengamma.sesame.credit.converter.IndexCdsConverterFn;
import com.opengamma.sesame.credit.converter.LegacyCdsConverterFn;
import com.opengamma.sesame.credit.converter.StandardCdsConverterFn;
import com.opengamma.sesame.credit.market.CreditKeyMapperFn;
import com.opengamma.sesame.credit.market.DefaultCreditKeyMapperFn;
import com.opengamma.sesame.credit.market.DefaultIndexCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.DefaultLegacyCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.DefaultStandardCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.IndexCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.LegacyCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.StandardCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.measures.CreditCs01Fn;
import com.opengamma.sesame.credit.measures.CreditPvFn;
import com.opengamma.sesame.credit.measures.DefaultCreditCs01Fn;
import com.opengamma.sesame.credit.measures.DefaultCreditPvFn;
import com.opengamma.sesame.credit.snapshot.SnapshotCreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotYieldCurveDataProviderFn;
import com.opengamma.util.money.Currency;

/**
 * Utility class for credit views
 */
public final class CreditViewUtils {

  private static final String SHORT_NAME = "Pepsico Inc";
  private static final SeniorityLevel SNRFOR = SeniorityLevel.SNRFOR;
  private static final RestructuringClause XR = RestructuringClause.XR;
  private static final Currency USD = Currency.USD;
  private static final ExternalIdBundle SCDS_BUNDLE = ExternalIdBundle.of("Sample", SHORT_NAME);
  private static final ExternalIdBundle LCDS_BUNDLE = ExternalIdBundle.of("Sample", SHORT_NAME);
  private static final ExternalId REF_ID = ExternalId.of("SHORT-NAME", SHORT_NAME);
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));

  private CreditViewUtils() { /* private constructor */ }

  /** List of Credit inputs */
  public static final List<Object> INPUTS =
      ImmutableList.<Object>of(createStandardCDSSecurity(),
                          createLegacyCDSSecurity());

  /**
   * Utility for creating a credit specific view column
   * @param creditCurveName credit curve name, not null
   * @param yieldCurveName yield curve name, not null
   */
  public static ViewConfig createViewConfig(String creditCurveName, String yieldCurveName) {

    return
        configureView(
            "CDS view",
            createFunctionLevelConfig(creditCurveName, yieldCurveName),
            column(OutputNames.PRESENT_VALUE, output(OutputNames.PRESENT_VALUE, StandardCDSSecurity.class)),
            column(OutputNames.CS01, output(OutputNames.CS01, StandardCDSSecurity.class)));
  }

  private static FunctionModelConfig createFunctionLevelConfig(String creditCurveName, String yieldCurveName) {

    SnapshotLink<CreditCurveDataSnapshot> creditCurve =
        SnapshotLink.resolvable(creditCurveName, CreditCurveDataSnapshot.class);
    SnapshotLink<YieldCurveDataSnapshot> yieldCurve =
        SnapshotLink.resolvable(yieldCurveName, YieldCurveDataSnapshot.class);
    ConfigLink<RestructuringSettings> restructuringSettings =
        ConfigLink.resolvable("SampleRestructuringMap", RestructuringSettings.class);

    CreditCurveDataKeyMap configKeyMap = CreditCurveDataKeyMap.builder()
        .securityCurveMappings(ImmutableMap.<CreditCurveDataKey, CreditCurveDataKey>of())
        .build();

    return config(
        arguments(
            function(
                DefaultCreditCs01Fn.class,
                argument("accrualOnDefaultFormulae", AccrualOnDefaultFormulae.OrignalISDA)),
            function(
                DefaultCreditPvFn.class,
                argument("priceType", PriceType.CLEAN),
                argument("accrualOnDefaultFormulae", AccrualOnDefaultFormulae.OrignalISDA)),
            function(
                DefaultCreditKeyMapperFn.class,
                argument("keyMap", configKeyMap)),
            function(
                DefaultStandardCdsMarketDataResolverFn.class,
                argument("restructuringSettings", restructuringSettings)),
            function(
                SnapshotCreditCurveDataProviderFn.class,
                argument("snapshotLink", creditCurve)),
            function(
                SnapshotYieldCurveDataProviderFn.class,
                argument("snapshotLink", yieldCurve))),
        implementations(
            CreditPvFn.class, DefaultCreditPvFn.class,
            CreditCs01Fn.class, DefaultCreditCs01Fn.class,
            IsdaCompliantYieldCurveFn.class, DefaultIsdaCompliantYieldCurveFn.class,
            YieldCurveDataProviderFn.class, SnapshotYieldCurveDataProviderFn.class,
            CreditCurveDataProviderFn.class, SnapshotCreditCurveDataProviderFn.class,
            IsdaCompliantCreditCurveFn.class, StandardIsdaCompliantCreditCurveFn.class,
            LegacyCdsConverterFn.class, DefaultLegacyCdsConverterFn.class,
            StandardCdsConverterFn.class, DefaultStandardCdsConverterFn.class,
            IndexCdsConverterFn.class, DefaultIndexCdsConverterFn.class,
            StandardCdsMarketDataResolverFn.class, DefaultStandardCdsMarketDataResolverFn.class,
            IndexCdsMarketDataResolverFn.class, DefaultIndexCdsMarketDataResolverFn.class,
            LegacyCdsMarketDataResolverFn.class, DefaultLegacyCdsMarketDataResolverFn.class,
            CreditKeyMapperFn.class, DefaultCreditKeyMapperFn.class));
  }

  /**
   * Create an instance of a Standard CDS
   * This StandardCDSSecurity is structured to create the same CDSAnalytic as the LegacyCDSSecurity below
   * @return StandardCDSSecurity
   */
  public static StandardCDSSecurity createStandardCDSSecurity() {
    return new StandardCDSSecurity(SCDS_BUNDLE,
                                   "Standard CDS " + SHORT_NAME,
                                   LocalDate.of(2014, 9, 20),
                                   LocalDate.of(2019, 12, 20),
                                   REF_ID,
                                   new InterestRateNotional(USD, 10_000_000),
                                   true,
                                   0.01,
                                   SNRFOR);
  }

  /**
   * Create an instance of a Legacy CDS
   * This LegacyCDSSecurity is structured to create the same CDSAnalytic as the  StandardCDSSecurity above
   * @return LegacyCDSSecurity
   */
  public static LegacyCDSSecurity createLegacyCDSSecurity() {
    return new LegacyCDSSecurity(LCDS_BUNDLE,
                                 "Legacy CDS " + SHORT_NAME,
                                 LocalDate.of(2014, 9, 20),
                                 LocalDate.of(2014, 6, 20),
                                 LocalDate.of(2019, 12, 20),
                                 REF_ID,
                                 new InterestRateNotional(Currency.USD, 10_000_000),
                                 true,
                                 0.01,
                                 SNRFOR,
                                 SimpleFrequency.QUARTERLY,
                                 DayCounts.ACT_360,
                                 BusinessDayConventions.FOLLOWING,
                                 USNY,
                                 XR,
                                 new InterestRateNotional(USD, 10_000_000),
                                 LocalDate.of(2019, 12, 20),
                                 true);

  }

}
