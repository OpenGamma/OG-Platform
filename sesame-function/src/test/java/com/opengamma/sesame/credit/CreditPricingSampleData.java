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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;
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
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.impl.SimpleRegion;
import com.opengamma.financial.analytics.isda.credit.CdsQuote;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.financial.analytics.isda.credit.CreditDefaultSwapType;
import com.opengamma.financial.analytics.isda.credit.ParSpreadQuote;
import com.opengamma.financial.analytics.isda.credit.PointsUpFrontQuote;
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
import com.opengamma.sesame.credit.measures.DefaultCreditBucketedCs01Fn;
import com.opengamma.sesame.credit.measures.DefaultCreditCs01Fn;
import com.opengamma.sesame.credit.measures.DefaultCreditPvFn;
import com.opengamma.sesame.credit.snapshot.CreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotCreditCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.SnapshotYieldCurveDataProviderFn;
import com.opengamma.sesame.credit.snapshot.YieldCurveDataProviderFn;
import com.opengamma.sesame.trade.IndexCDSTrade;
import com.opengamma.sesame.trade.LegacyCDSTrade;
import com.opengamma.sesame.trade.StandardCDSTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Sample credit data - curves, securities and mappings.
 */
public class CreditPricingSampleData {

  private static final String PEPSICO_INC = "Pepsico Inc";
  private static final String JCP = "JCP";
  private static final String IG_INDEX = "CDX.NA.IG.23-V1 5Y";
  private static final String INDEX_NAME = "10M." + IG_INDEX;
  private static final String SAMPLE_CREDIT_CURVE = "Sample Credit Curve";
  private static final String SAMPLE_YIELD_CURVE = "Sample Yield Curve";

  private static final SeniorityLevel SNRFOR = SeniorityLevel.SNRFOR;
  private static final RestructuringClause XR = RestructuringClause.XR;
  private static final Currency USD = Currency.USD;
  private static final ExternalIdBundle SCDS_BUNDLE = ExternalIdBundle.of("Sample", PEPSICO_INC);
  private static final ExternalIdBundle PUFSCDS_BUNDLE = ExternalIdBundle.of("Sample", JCP);
  private static final ExternalIdBundle LCDS_BUNDLE = ExternalIdBundle.of("Sample", PEPSICO_INC);
  private static final ExternalIdBundle CDX_BUNDLE = ExternalIdBundle.of("Sample", INDEX_NAME);
  private static final ExternalIdBundle CDXD_BUNDLE = ExternalIdBundle.of("Sample", IG_INDEX);
  private static final ExternalId REF_ID = ExternalId.of("SHORT-NAME", PEPSICO_INC);
  private static final ExternalId PUF_REF_ID = ExternalId.of("SHORT-NAME", JCP);
  private static final Set<ExternalId> USNY = Sets.newHashSet(ExternalId.of(ExternalSchemes.ISDA_HOLIDAY, "USNY"));
  private static final ExternalId REGION_US = ExternalId.of("FINANCIAL_REGION", "US");

  /**
   * Create an instance of a Standard CDS
   * This StandardCDSSecurity is structured to create the same CDSAnalytic as the LegacyCDSSecurity below
   * @return StandardCDSSecurity
   */
  public static StandardCDSTrade createPointsUpFrontStandardCDSSecurity() {

    StandardCDSSecurity cds = new StandardCDSSecurity(
                                     PUFSCDS_BUNDLE,                              //id
                                     JCP,                                         //name
                                     LocalDate.of(2014, 10, 16),                  //trade date
                                     LocalDate.of(2019, 12, 20),                  //maturity date
                                     PUF_REF_ID,                                  //reference id
                                     new InterestRateNotional(USD, 1_000_000),    //notional
                                     true,                                        //buy/sell
                                     0.05,                                        //coupon
                                     SNRFOR);                                     //seniority

    Trade trade = new SimpleTrade(cds,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());
    return new StandardCDSTrade(trade);

  }

  public static StandardCDSTrade createStandardCDSSecurity() {
    StandardCDSSecurity cds = new StandardCDSSecurity(
                                     SCDS_BUNDLE,                                 //id
                                     PEPSICO_INC,                                 //name
                                     LocalDate.of(2014, 10, 16),                  //trade date
                                     LocalDate.of(2019, 12, 20),                  //maturity date
                                     REF_ID,                                      //reference id
                                     new InterestRateNotional(USD, 1_000_000),    //notional
                                     true,                                        //buy/sell
                                     0.01,                                        //coupon
                                     SNRFOR);                                     //seniority
    Trade trade = new SimpleTrade(cds,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());

    return new StandardCDSTrade(trade);
  }

  /**
   * Create an instance of a Legacy CDS
   * This LegacyCDSSecurity is structured to create the same CDSAnalytic as the  StandardCDSSecurity above
   * @return LegacyCDSSecurity
   */
  public static LegacyCDSTrade createLegacyCDSSecurity() {
    LegacyCDSSecurity cds =  new LegacyCDSSecurity(
                                     LCDS_BUNDLE,
                                     LocalDate.of(2014, 10, 16),
                                     LocalDate.of(2014, 7, 16),
                                     LocalDate.of(2019, 12, 20),
                                     REF_ID,
                                     new InterestRateNotional(Currency.USD, 1_000_000),
                                     true,
                                     0.01,
                                     SNRFOR,
                                     SimpleFrequency.QUARTERLY,
                                     DayCounts.ACT_360,
                                     BusinessDayConventions.FOLLOWING,
                                     USNY,
                                     XR,
                                     new InterestRateNotional(USD, 1_000_000),
                                     LocalDate.of(2019, 12, 20),
                                     true);

    Trade trade = new SimpleTrade(cds,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());

    return new LegacyCDSTrade(trade);

  }

  public static IndexCDSTrade createIndexCDSSecurity() {

    // 100 components in the basket, with total weight of 1
    List<CreditDefaultSwapIndexComponent> components = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
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
        new IndexCDSDefinitionSecurity(CDXD_BUNDLE,                                //id
                                       IG_INDEX,                                   //name
                                       LocalDate.of(2014, 9, 20),                  //start date
                                       "V1",                                       //version
                                       "23",                                       //series
                                       "IG",                                       //family
                                       USD,                                        //currency
                                       0.4,                                        //recovery rate
                                       SimpleFrequency.QUARTERLY,                  //coupon frequency
                                       0.01,                                       //coupon
                                       CDSIndexTerms.of(Tenor.FIVE_YEARS),         //terms
                                       componentBundle,                            //basket
                                       USNY,                                       //calendar
                                       BusinessDayConventions.MODIFIED_FOLLOWING); //business day conventions

    IndexCDSSecurity cds = new IndexCDSSecurity(
                                      CDX_BUNDLE,                                        //id
                                      INDEX_NAME,                                        //name
                                      true,                                              //buy/sell
                                      SecurityLink.resolved(definition),                 //definition
                                      LocalDate.of(2014, 10, 16),                        //trade date
                                      LocalDate.of(2019, 12, 20),                        //maturity date
                                      new InterestRateNotional(USD, 1_000_000));         //notional

    Trade trade = new SimpleTrade(cds,
                                  BigDecimal.ONE,
                                  new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "CPARTY")),
                                  LocalDate.now(),
                                  OffsetTime.now());

    return new IndexCDSTrade(trade);
  }

  public static RestructuringSettings createRestructuringSettings() {
    Map<Currency, RestructuringClause> mappings = ImmutableMap.of(USD, XR);
    return RestructuringSettings.builder().restructuringMappings(mappings).build();
  }

  public static CreditCurveDataSnapshot createCreditCurveDataSnapshot() {
    ImmutableMap.Builder<CreditCurveDataKey, CreditCurveData> builder = ImmutableMap.builder();
    builder.put(curveCreditCurveDataKey(PEPSICO_INC), createSingleNameCreditCurveData());
    builder.put(curveCreditCurveDataKey(JCP), createPUFSingleNameCreditCurveData());
    builder.put(curveIndexCreditCurveDataKey(IG_INDEX), createIndexCreditCurveData());
    return CreditCurveDataSnapshot.builder().name(SAMPLE_CREDIT_CURVE).creditCurves(builder.build()).build();
  }
  
  public static CreditCurveDataSnapshot createMultiPointIndexCurveCreditCurveDataSnapshot() {
    ImmutableMap.Builder<CreditCurveDataKey, CreditCurveData> builder = ImmutableMap.builder();
    builder.put(curveIndexCreditCurveDataKey(IG_INDEX), createMultiPointIndexCreditCurveData());
    return CreditCurveDataSnapshot.builder().name(SAMPLE_CREDIT_CURVE).creditCurves(builder.build()).build();
  }

  public static YieldCurveDataSnapshot createYieldCurveDataSnapshot() {
    Map<Currency, YieldCurveData> map = ImmutableMap.of(USD, createYieldCurveData());
    return YieldCurveDataSnapshot.builder().name(SAMPLE_YIELD_CURVE).yieldCurves(map).build();
  }

  public static FunctionModelConfig createFunctionModelConfig() {
    return createFunctionModelConfig(createCreditCurveDataSnapshot());
  }
  
  public static FunctionModelConfig createFunctionModelConfig(CreditCurveDataSnapshot customCreditSnapshot) {

    CreditCurveDataKeyMap configKeyMap = CreditCurveDataKeyMap.builder()
        .securityCurveMappings(ImmutableMap.<CreditCurveDataKey, CreditCurveDataKey>of())
        .build();
    SnapshotLink<CreditCurveDataSnapshot> creditCurve = ResolvedSnapshotLink.resolved(customCreditSnapshot);
    SnapshotLink<YieldCurveDataSnapshot> yieldCurve = ResolvedSnapshotLink.resolved(createYieldCurveDataSnapshot());
    RestructuringSettings restructuringSettings = createRestructuringSettings();

    return config(
        arguments(
            function(
                DefaultCreditBucketedCs01Fn.class,
                argument("accrualOnDefaultFormulae", AccrualOnDefaultFormulae.OrignalISDA)),
            function(
                DefaultCreditCs01Fn.class,
                argument("accrualOnDefaultFormulae", AccrualOnDefaultFormulae.OrignalISDA)),
            function(
                DefaultCreditPvFn.class,
                argument("priceType", PriceType.DIRTY),
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

  public static FunctionModelConfig createYCMappingFunctionModelConfig() {

    CreditCurveDataKeyMap configKeyMap = CreditCurveDataKeyMap.builder()
        .securityCurveMappings(ImmutableMap.<CreditCurveDataKey, CreditCurveDataKey>of())
        .build();
    SnapshotLink<CreditCurveDataSnapshot> creditCurve = ResolvedSnapshotLink.resolved(createCreditCurveDataSnapshot());
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
                MappingIsdaCompliantYieldCurveFn.class,
                argument("multicurveName", "Curve Bundle")),
            function(
                SnapshotCreditCurveDataProviderFn.class,
                argument("snapshotLink", creditCurve))),
        implementations(
            CreditPvFn.class, DefaultCreditPvFn.class,
            CreditCs01Fn.class, DefaultCreditCs01Fn.class,
            IsdaCompliantYieldCurveFn.class, MappingIsdaCompliantYieldCurveFn.class,
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
        .put(Tenor.ONE_MONTH, 0.001535)
        .put(Tenor.TWO_MONTHS, 0.001954)
        .put(Tenor.THREE_MONTHS, 0.002281)
        .put(Tenor.SIX_MONTHS, 0.003217)
        .put(Tenor.ONE_YEAR, 0.005444)
        .build();

    SortedMap<Tenor, Double> swapData = ImmutableSortedMap.<Tenor, Double>naturalOrder()
        .put(Tenor.TWO_YEARS,0.005905)
        .put(Tenor.THREE_YEARS,0.009555)
        .put(Tenor.FOUR_YEARS,0.012775)
        .put(Tenor.FIVE_YEARS,0.015395)
        .put(Tenor.SIX_YEARS,0.017445)
        .put(Tenor.SEVEN_YEARS,0.019205)
        .put(Tenor.EIGHT_YEARS,0.02066)
        .put(Tenor.NINE_YEARS,0.021885)
        .put(Tenor.TEN_YEARS,0.02294)
        .put(Tenor.ofYears(12),0.024615)
        .put(Tenor.ofYears(15),0.0263)
        .put(Tenor.ofYears(20),0.02795)
        .put(Tenor.ofYears(25),0.028715)
        .put(Tenor.ofYears(30),0.02916)
        .build();

    return YieldCurveData.builder()
        .currency(USD)
        .curveDayCount(DayCounts.ACT_365)
        .curveBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
        .cashDayCount(DayCounts.ACT_360)
        .swapDayCount(DayCounts.THIRTY_U_360)
        .spotDate(LocalDate.of(2014, 10, 20))
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

  public static CreditCurveData createSingleNameCreditCurveData() {

      ConventionLink<IsdaCreditCurveConvention> conventionLink = ConventionLink.resolved(
          createUsdIsdaCreditCurveConvention());

      SortedMap<Tenor, CdsQuote> spreadData = ImmutableSortedMap.<Tenor, CdsQuote>naturalOrder()
          .put(Tenor.SIX_MONTHS, ParSpreadQuote.from(0.0028))
          .put(Tenor.ONE_YEAR, ParSpreadQuote.from(0.0028))
          .put(Tenor.TWO_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.THREE_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.FOUR_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.FIVE_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.SEVEN_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.TEN_YEARS, ParSpreadQuote.from(0.0028))
          .put(Tenor.ofYears(20), ParSpreadQuote.from(0.0028))
          .put(Tenor.ofYears(30), ParSpreadQuote.from(0.0028))
          .build();

      return CreditCurveData.builder()
          .curveConventionLink(conventionLink)
          .recoveryRate(0.4)
          .cdsQuotes(spreadData)
          .build();
    }

  private static CreditCurveData createPUFSingleNameCreditCurveData() {

    ConventionLink<IsdaCreditCurveConvention> conventionLink = ConventionLink.resolved(
        createUsdIsdaCreditCurveConvention());

    SortedMap<Tenor, CdsQuote> spreadData = ImmutableSortedMap.<Tenor, CdsQuote>naturalOrder()
        .put(Tenor.FIVE_YEARS, PointsUpFrontQuote.from(0.05, -0.01))
        .build();

    return CreditCurveData.builder()
        .curveConventionLink(conventionLink)
        .recoveryRate(0.4)
        .cdsQuotes(spreadData)
        .build();
  }

  private static CreditCurveData createIndexCreditCurveData() {

    ConventionLink<IsdaCreditCurveConvention> conventionLink = ConventionLink.resolved(
        createUsdIsdaCreditCurveConvention());

    SortedMap<Tenor, CdsQuote> spreadData = ImmutableSortedMap.<Tenor, CdsQuote>naturalOrder()
        .put(Tenor.FIVE_YEARS, ParSpreadQuote.from(0.006))
        .build();

    return CreditCurveData.builder()
        .curveConventionLink(conventionLink)
        .recoveryRate(0.4)
        .cdsQuotes(spreadData)
        .build();
  }

  private static CreditCurveData createMultiPointIndexCreditCurveData() {

    ConventionLink<IsdaCreditCurveConvention> conventionLink = ConventionLink.resolved(
        createUsdIsdaCreditCurveConvention());

    SortedMap<Tenor, CdsQuote> spreadData = ImmutableSortedMap.<Tenor, CdsQuote>naturalOrder()
        .put(Tenor.TWO_YEARS, ParSpreadQuote.from(0.006))
        .put(Tenor.THREE_YEARS, ParSpreadQuote.from(0.006))
        .put(Tenor.FIVE_YEARS, ParSpreadQuote.from(0.006))
        .put(Tenor.TEN_YEARS, ParSpreadQuote.from(0.006))
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
