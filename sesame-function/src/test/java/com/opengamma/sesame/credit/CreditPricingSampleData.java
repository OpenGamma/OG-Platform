/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit;

import static com.opengamma.sesame.config.ConfigBuilder.argument;
import static com.opengamma.sesame.config.ConfigBuilder.arguments;
import static com.opengamma.sesame.config.ConfigBuilder.config;
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.lang.StringUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.analytics.financial.credit.isdastandardmodel.AccrualOnDefaultFormulae;
import com.opengamma.analytics.financial.credit.isdastandardmodel.PriceType;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.link.ConventionLink;
import com.opengamma.core.link.ResolvedSnapshotLink;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.core.link.SnapshotLink;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.financial.analytics.isda.credit.CdsQuote;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.CreditDefaultSwapType;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.convention.IsdaCreditCurveConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.sesame.config.FunctionModelConfig;
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
import com.opengamma.sesame.credit.snapshot.CreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotCreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotYieldCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.YieldCurveDataProviderFn;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Sample credit data - curves, securities and mappings.
 */
public class CreditPricingSampleData {

  private static final String SHORT_NAME = "Pepsico Inc";
  private static final String INDEX_DEFINITION_NAME = "CDX.NA.HY.S23.V3";
  private static final String INDEX_NAME = "10M." + INDEX_DEFINITION_NAME;
  private static final String SAMPLE_CREDIT_CURVE = "Sample Credit Curve";
  private static final String SAMPLE_YIELD_CURVE = "Sample Yield Curve";

  private static final SeniorityLevel SNRFOR = SeniorityLevel.SNRFOR;
  private static final RestructuringClause XR = RestructuringClause.XR;
  private static final Currency USD = Currency.USD;
  private static final ExternalIdBundle SCDS_BUNDLE = ExternalIdBundle.of("Sample", SHORT_NAME);
  private static final ExternalIdBundle LCDS_BUNDLE = ExternalIdBundle.of("Sample", SHORT_NAME);
  private static final ExternalIdBundle CDX_BUNDLE = ExternalIdBundle.of("Sample", INDEX_NAME);
  private static final ExternalIdBundle CDXD_BUNDLE = ExternalIdBundle.of("Sample", INDEX_DEFINITION_NAME);
  private static final ExternalId REF_ID = ExternalId.of("SHORT-NAME", SHORT_NAME);
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
  private static final ExternalId REGION_US = ExternalId.of("FINANCIAL_REGION", "US");

  /**
   * Create an instance of a Standard CDS
   * This StandardCDSSecurity is structured to create the same CDSAnalytic as the LegacyCDSSecurity below
   * @return StandardCDSSecurity
   */
  public static StandardCDSSecurity createStandardCDSSecurity() {
    return new StandardCDSSecurity(SCDS_BUNDLE,
                                   SHORT_NAME,
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

  public static IndexCDSSecurity createIndexCDSSecurity() {

    // 97 components in the basket, with total weight of 0.97
    List<CreditDefaultSwapIndexComponent> components = new ArrayList<>();
    for (int i = 1; i <= 97; i++) {
      ExternalId externalId = ExternalId.of("Basket", String.valueOf(i));
      CreditDefaultSwapIndexComponent component =
          new CreditDefaultSwapIndexComponent(externalId.getValue(),
                                              externalId,
                                              0.01,
                                              externalId);
      components.add(component);
    }
    CDSIndexComponentBundle componentBundle = CDSIndexComponentBundle.of(components);

    IndexCDSDefinitionSecurity definition =
        new IndexCDSDefinitionSecurity(CDXD_BUNDLE,
                                       INDEX_DEFINITION_NAME,
                                       LocalDate.of(1950, 1, 1),
                                       "V1",
                                       "S23",
                                       "HY",
                                       USD,
                                       0.4,
                                       SimpleFrequency.QUARTERLY,
                                       0.01,
                                       CDSIndexTerms.of(Tenor.ONE_YEAR),
                                       componentBundle,
                                       USNY,
                                       BusinessDayConventions.MODIFIED_FOLLOWING);

    return new IndexCDSSecurity(CDX_BUNDLE,
                                INDEX_NAME,
                                true,
                                SecurityLink.resolved(definition),
                                LocalDate.of(2014, 9, 20),
                                LocalDate.of(2019, 12, 20),
                                new InterestRateNotional(USD, 10_000_000));
  }

  public static RestructuringSettings createRestructuringSettings() {
    Map<Currency, RestructuringClause> mappings = ImmutableMap.of(USD, XR);
    return RestructuringSettings.builder().restructuringMappings(mappings).build();
  }

  public static CreditCurveDataSnapshot createCreditCurveDataSnapshot() {
    ImmutableMap.Builder<CreditCurveDataKey, CreditCurveData> builder = ImmutableMap.builder();
    builder.put(curveCreditCurveDataKey(SHORT_NAME), createCreditCurveData());
    builder.put(curveIndexCreditCurveDataKey(INDEX_DEFINITION_NAME), createCreditCurveData());
    return CreditCurveDataSnapshot.builder().name(SAMPLE_CREDIT_CURVE).creditCurves(builder.build()).build();
  }

  public static YieldCurveDataSnapshot createYieldCurveDataSnapshot() {
    Map<Currency, YieldCurveData> map = ImmutableMap.of(USD, createYieldCurveData());
    return YieldCurveDataSnapshot.builder().name(SAMPLE_YIELD_CURVE).yieldCurves(map).build();
  }

  public static FunctionModelConfig createFunctionModelConfig() {

    CreditCurveDataKeyMap configKeyMap = CreditCurveDataKeyMap.builder()
        .securityCurveMappings(ImmutableMap.<CreditCurveDataKey, CreditCurveDataKey>of())
        .build();
    SnapshotLink<CreditCurveDataSnapshot> creditCurve = ResolvedSnapshotLink.resolved(createCreditCurveDataSnapshot());
    SnapshotLink<YieldCurveDataSnapshot> yieldCurve = ResolvedSnapshotLink.resolved(createYieldCurveDataSnapshot());
    RestructuringSettings restructuringSettings = createRestructuringSettings();

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
            IndexCdsConverterFn.class, DefaultIndexCdsConverterFn.class,
            StandardCdsConverterFn.class, DefaultStandardCdsConverterFn.class,
            StandardCdsMarketDataResolverFn.class, DefaultStandardCdsMarketDataResolverFn.class,
            IndexCdsMarketDataResolverFn.class, DefaultIndexCdsMarketDataResolverFn.class,
            LegacyCdsMarketDataResolverFn.class, DefaultLegacyCdsMarketDataResolverFn.class,
            CreditKeyMapperFn.class, DefaultCreditKeyMapperFn.class));
  }

  private static YieldCurveData createYieldCurveData() {

    SortedMap<Tenor, Double> cashData = ImmutableSortedMap.<Tenor, Double>naturalOrder()
        .put(Tenor.ONE_MONTH, 0.00156)
        .put(Tenor.TWO_MONTHS, 0.001948)
        .put(Tenor.THREE_MONTHS, 0.002351)
        .put(Tenor.SIX_MONTHS, 0.003289)
        .put(Tenor.ONE_YEAR, 0.00554)
        .build();

    SortedMap<Tenor, Double> swapData =  ImmutableSortedMap.<Tenor, Double>naturalOrder()
        .put(Tenor.TWO_YEARS, 0.006785)
        .put(Tenor.THREE_YEARS, 0.0112)
        .put(Tenor.FOUR_YEARS, 0.01495)
        .put(Tenor.FIVE_YEARS, 0.01785)
        .put(Tenor.SIX_YEARS, 0.02008)
        .put(Tenor.SEVEN_YEARS, 0.021905)
        .put(Tenor.EIGHT_YEARS, 0.02343)
        .put(Tenor.NINE_YEARS, 0.0247)
        .put(Tenor.TEN_YEARS, 0.025805)
        .put(Tenor.ofYears(12), 0.02753)
        .put(Tenor.ofYears(15), 0.029425)
        .put(Tenor.ofYears(20), 0.03103)
        .put(Tenor.ofYears(25), 0.0317)
        .put(Tenor.ofYears(30), 0.032025)
        .build();

    return YieldCurveData.builder()
        .currency(USD)
        .curveDayCount(DayCounts.ACT_365)
        .curveBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
        .cashDayCount(DayCounts.ACT_360)
        .swapDayCount(DayCounts.THIRTY_U_360)
        .spotDate(LocalDate.of(2014, 8, 19))
        .regionId(REGION_US)
        .swapFixedLegInterval(Tenor.SIX_MONTHS)
        .cashData(cashData)
        .swapData(swapData)
        .build();
  }

  private static CreditCurveDataKey curveCreditCurveDataKey(String code) {
    return CreditCurveDataKey.builder()
        .currency(USD)
        .curveName(code)
        .seniority(SNRFOR)
        .restructuring(XR)
        .cdsType(CreditDefaultSwapType.SINGLE_NAME) // not needed as key defaults to SINGLE_NAME
        .build();
  }

  private static CreditCurveDataKey curveIndexCreditCurveDataKey(String code) {
    return CreditCurveDataKey.builder()
        .currency(USD)
        .curveName(code)
        .cdsType(CreditDefaultSwapType.INDEX)
        .build();
  }

  public static IsdaCreditCurveConvention createUsdIsdaCreditCurveConvention() {
    return createIsdaCreditCurveConvention(REGION_US);
  }

  private static IsdaCreditCurveConvention createIsdaCreditCurveConvention(ExternalId region) {
    IsdaCreditCurveConvention convention = new IsdaCreditCurveConvention();
    convention.setName("USD Isda Credit Curve Convention");
    convention.setStepIn(1);
    convention.setCashSettle(3);
    convention.setPayAccOnDefault(true);
    convention.setCouponInterval(Period.ofMonths(3));
    convention.setStubType(StubType.FRONTSHORT);
    convention.setBusinessDayConvention(BusinessDayConventions.FOLLOWING);
    convention.setRegionId(region);
    convention.setAccrualDayCount(DayCounts.ACT_360);
    convention.setCurveDayCount(DayCounts.ACT_365);
    convention.setProtectFromStartOfDay(true);
    return convention;
  }

  private static CreditCurveData createCreditCurveData() {

    ConventionLink<IsdaCreditCurveConvention> conventionLink = ConventionLink.resolved(
        createUsdIsdaCreditCurveConvention());

    SortedMap<Tenor, CdsQuote> spreadData = ImmutableSortedMap.<Tenor, CdsQuote>naturalOrder()
        .put(Tenor.SIX_MONTHS, ParSpreadQuote.from(0.002))
        .put(Tenor.ONE_YEAR, ParSpreadQuote.from(0.004))
        .put(Tenor.TWO_YEARS, ParSpreadQuote.from(0.006))
        .put(Tenor.THREE_YEARS, ParSpreadQuote.from(0.008))
        .put(Tenor.FOUR_YEARS, ParSpreadQuote.from(0.01))
        .put(Tenor.FIVE_YEARS, ParSpreadQuote.from(0.012))
        .put(Tenor.SEVEN_YEARS, ParSpreadQuote.from(0.014))
        .put(Tenor.TEN_YEARS, ParSpreadQuote.from(0.016))
        .put(Tenor.ofYears(20), ParSpreadQuote.from(0.018))
        .put(Tenor.ofYears(30), ParSpreadQuote.from(0.02))
        .build();

    return CreditCurveData.builder()
        .curveConventionLink(conventionLink)
        .recoveryRate(0.4)
        .cdsQuotes(spreadData)
        .build();

  }

  public static ImmutableMap<Class<?>, Object> generateBaseComponents() {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();

    HolidaySource holidaySource = new WeekendHolidaySource();
    builder.put(holidaySource.getClass().getInterfaces()[0], holidaySource);

    RegionMaster regionMaster = new InMemoryRegionMaster();
    SimpleRegion regionUs = new SimpleRegion();
    regionUs.addExternalId(ExternalSchemes.financialRegionId("US"));
    regionUs.addExternalId(ExternalSchemes.currencyRegionId(USD));
    regionUs.setUniqueId(UniqueId.of("REGION", "1"));
    regionMaster.add(new RegionDocument(regionUs));
    MasterRegionSource regionSource = new MasterRegionSource(regionMaster);
    builder.put(regionSource.getClass().getInterfaces()[0], regionSource);

    return builder.build();

  }
}
