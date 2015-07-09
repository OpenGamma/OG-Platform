/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.IborCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.OvernightCurveTypeConfiguration;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.OvernightIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.impl.InMemoryConfigMaster;
import com.opengamma.master.config.impl.MasterConfigSource;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.impl.InMemoryConventionMaster;
import com.opengamma.master.convention.impl.MasterConventionSource;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.service.ServiceContext;
import com.opengamma.service.ThreadLocalServiceContext;
import com.opengamma.service.VersionCorrectionProvider;
import com.opengamma.sesame.MulticurveBundle;
import com.opengamma.sesame.engine.FixedInstantVersionCorrectionProvider;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Helper methods for testing {@link MarketDataFilter} implementations operating on {@link MulticurveBundle}.
 */
public class MulticurveFilterTestUtils {

  static final String USD_DISCOUNTING = "USD Discounting";
  static final String GBP_DISCOUNTING = "GBP Discounting";
  static final String USD_OVERNIGHT = "USD Overnight";
  static final String GBP_OVERNIGHT = "GBP Overnight";
  static final String EUR_LIBOR_6M = "EUR LIBOR 6M";
  static final String USD_LIBOR_3M = "USD LIBOR 3M";

  static final Set<String> CURVE_NAMES =
      ImmutableSet.of(
          USD_DISCOUNTING,
          GBP_DISCOUNTING,
          USD_OVERNIGHT,
          GBP_OVERNIGHT,
          EUR_LIBOR_6M,
          USD_LIBOR_3M);

  static final String USD_OVERNIGHT_INDEX_NAME = "USD Overnight Index";
  static final String GBP_OVERNIGHT_INDEX_NAME = "GBP Overnight Index";
  static final String EUR_LIBOR_6M_INDEX_NAME = "EUR LIBOR 6M Index";

  static final IndexON USD_OVERNIGHT_INDEX = new IndexON(USD_OVERNIGHT_INDEX_NAME, Currency.USD, DayCounts.ACT_360, 0);
  static final IndexON GBP_OVERNIGHT_INDEX = new IndexON(GBP_OVERNIGHT_INDEX_NAME, Currency.GBP, DayCounts.ACT_360, 1);

  static final IborIndex EUR_LIBOR_6M_INDEX =
      new IborIndex(
          Currency.EUR,
          Period.ofMonths(6),
          1,
          DayCounts.ACT_365,
          BusinessDayConventions.FOLLOWING,
          false,
          EUR_LIBOR_6M_INDEX_NAME);

  static final IborIndex USD_LIBOR_3M_INDEX =
      new IborIndex(
          Currency.USD,
          Period.ofMonths(3),
          1,
          DayCounts.ACT_365,
          BusinessDayConventions.FOLLOWING,
          false,
          "USD LIBOR 3M Index");
  private static final ExternalId REGION_ID = ExternalId.of("reg", "foo");

  static final ExternalId USD_OVERNIGHT_SECURITY_ID = ExternalId.of("sec", "USD Overnight");
  static final ExternalId USD_OVERNIGHT_CONVENTION_ID = ExternalId.of("con", "USD Overnight");
  static final OvernightIndex USD_OVERNIGHT_SECURITY =
      new OvernightIndex(
          USD_OVERNIGHT_INDEX_NAME,
          "desc",
          USD_OVERNIGHT_CONVENTION_ID,
          USD_OVERNIGHT_SECURITY_ID.toBundle());
  static final OvernightIndexConvention USD_OVERNIGHT_CONVENTION =
      new OvernightIndexConvention(
          "USD Overnight Convention",
          USD_OVERNIGHT_CONVENTION_ID.toBundle(),
          DayCounts.ACT_365,
          0,
          Currency.USD,
          REGION_ID);

  static final ExternalId GBP_OVERNIGHT_SECURITY_ID = ExternalId.of("sec", "GBP Overnight");
  static final ExternalId GBP_OVERNIGHT_CONVENTION_ID = ExternalId.of("con", "GBP Overnight");
  static final OvernightIndex GBP_OVERNIGHT_SECURITY =
      new OvernightIndex(
          GBP_OVERNIGHT_INDEX_NAME,
          "desc",
          GBP_OVERNIGHT_CONVENTION_ID,
          GBP_OVERNIGHT_SECURITY_ID.toBundle());
  static final OvernightIndexConvention GBP_OVERNIGHT_CONVENTION =
      new OvernightIndexConvention(
          "GBP Overnight Convention",
          GBP_OVERNIGHT_CONVENTION_ID.toBundle(),
          DayCounts.ACT_360,
          1,
          Currency.GBP,
          REGION_ID);

  static final ExternalId USD_LIBOR_SECURITY_ID = ExternalId.of("sec", "USD LIBOR");
  static final ExternalId USD_LIBOR_CONVENTION_ID = ExternalId.of("con", "USD LIBOR");
  static final com.opengamma.financial.security.index.IborIndex USD_LIBOR_SECURITY =
      new com.opengamma.financial.security.index.IborIndex(
          "USD LIBOR Security",
          "desc",
          Tenor.THREE_MONTHS,
          USD_LIBOR_CONVENTION_ID,
          USD_LIBOR_SECURITY_ID.toBundle());
  static final IborIndexConvention USD_LIBOR_CONVENTION =
      new IborIndexConvention(
          "USD LIBOR Convention",
          USD_LIBOR_CONVENTION_ID.toBundle(),
          DayCounts.ACT_365,
          BusinessDayConventions.FOLLOWING,
          1,
          false,
          Currency.USD,
          LocalTime.NOON,
          "America/New_York",
          REGION_ID,
          REGION_ID,
          "fixingPage");

  static final ExternalId EUR_LIBOR_SECURITY_ID = ExternalId.of("sec", "EUR LIBOR");
  static final ExternalId EUR_LIBOR_CONVENTION_ID = ExternalId.of("con", "EUR LIBOR");
  static final com.opengamma.financial.security.index.IborIndex EUR_LIBOR_SECURITY =
      new com.opengamma.financial.security.index.IborIndex(
          EUR_LIBOR_6M_INDEX_NAME,
          "desc",
          Tenor.SIX_MONTHS,
          EUR_LIBOR_CONVENTION_ID,
          EUR_LIBOR_SECURITY_ID.toBundle());
  static final IborIndexConvention EUR_LIBOR_CONVENTION =
      new IborIndexConvention(
          "EUR LIBOR Convention",
          EUR_LIBOR_CONVENTION_ID.toBundle(),
          DayCounts.ACT_365,
          BusinessDayConventions.FOLLOWING,
          1,
          false,
          Currency.EUR,
          LocalTime.NOON,
          "Europe/Frankfurt",
          REGION_ID,
          REGION_ID,
          "fixingPage");

  static final String CURVE_CONFIG_NAME = "curve config";
  static final ServiceContext SERVICE_CONTEXT;

  static {
    InMemoryConventionMaster conventionMaster = new InMemoryConventionMaster();
    conventionMaster.add(new ConventionDocument(EUR_LIBOR_CONVENTION));
    conventionMaster.add(new ConventionDocument(USD_LIBOR_CONVENTION));
    conventionMaster.add(new ConventionDocument(USD_OVERNIGHT_CONVENTION));
    conventionMaster.add(new ConventionDocument(GBP_OVERNIGHT_CONVENTION));
    MasterConventionSource conventionSource = new MasterConventionSource(conventionMaster);

    InMemorySecurityMaster securityMaster = new InMemorySecurityMaster();
    securityMaster.add(new SecurityDocument(EUR_LIBOR_SECURITY));
    securityMaster.add(new SecurityDocument(GBP_OVERNIGHT_SECURITY));
    securityMaster.add(new SecurityDocument(USD_LIBOR_SECURITY));
    securityMaster.add(new SecurityDocument(USD_OVERNIGHT_SECURITY));
    MasterSecuritySource securitySource = new MasterSecuritySource(securityMaster);

    Map<String, List<? extends CurveTypeConfiguration>> curveMap =
        ImmutableMap.<String, List<? extends CurveTypeConfiguration>>builder()
            .put(USD_DISCOUNTING, list(new DiscountingCurveTypeConfiguration("USD")))
            .put(GBP_DISCOUNTING, list(new DiscountingCurveTypeConfiguration("GBP")))
            .put(USD_OVERNIGHT, list(new OvernightCurveTypeConfiguration(USD_OVERNIGHT_SECURITY_ID)))
            .put(GBP_OVERNIGHT, list(new OvernightCurveTypeConfiguration(GBP_OVERNIGHT_SECURITY_ID)))
            .put(USD_LIBOR_3M, list(new IborCurveTypeConfiguration(USD_LIBOR_SECURITY_ID, Tenor.THREE_MONTHS)))
            .put(EUR_LIBOR_6M, list(new IborCurveTypeConfiguration(EUR_LIBOR_SECURITY_ID, Tenor.SIX_MONTHS)))
            .build();
    CurveGroupConfiguration groupConfig = new CurveGroupConfiguration(0, curveMap);
    CurveConstructionConfiguration curveConfig =
        new CurveConstructionConfiguration(
            CURVE_CONFIG_NAME,
            ImmutableList.of(groupConfig),
            ImmutableList.<String>of());

    InMemoryConfigMaster configMaster = new InMemoryConfigMaster();
    configMaster.add(new ConfigDocument(ConfigItem.of(curveConfig, CURVE_CONFIG_NAME)));
    MasterConfigSource configSource = new MasterConfigSource(configMaster);

    Map<Class<?>, Object> serviceMap =
        ImmutableMap.of(
            SecuritySource.class, securitySource,
            ConfigSource.class, configSource,
            VersionCorrectionProvider.class, new FixedInstantVersionCorrectionProvider(Instant.now()),
            ConventionSource.class, conventionSource);
    SERVICE_CONTEXT = ServiceContext.of(serviceMap);
  }

  private MulticurveFilterTestUtils() {
  }

  /**
   * Returns a curve bundle with the following curves:
   * <ul>
   *   <li>Discounting: USD, GBP</li>
   *   <li>Overnight index: USD, GBP</li>
   *   <li>Forward IBOR: USD LIBOR 3M, EUR LIBOR 6M</li>
   * </ul>
   *
   * @return a curve bundle with a selection of curves for USD, GBP and EUR
   */
  static MulticurveBundle bundle() {
    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();

    ConstantDoublesCurve usdDiscountingConstantCurve = new ConstantDoublesCurve(1d, USD_DISCOUNTING);
    YieldCurve usdDiscountingYieldCurve = YieldCurve.from(usdDiscountingConstantCurve);
    multicurve.setCurve(Currency.USD, usdDiscountingYieldCurve);

    ConstantDoublesCurve gbpDiscountingConstantCurve = new ConstantDoublesCurve(1.1, GBP_DISCOUNTING);
    YieldCurve gbpDiscountingYieldCurve = YieldCurve.from(gbpDiscountingConstantCurve);
    multicurve.setCurve(Currency.GBP, gbpDiscountingYieldCurve);

    ConstantDoublesCurve usdOvernightConstantCurve = new ConstantDoublesCurve(1.2, USD_OVERNIGHT);
    YieldCurve usdOvernightYieldCurve = YieldCurve.from(usdOvernightConstantCurve);
    multicurve.setCurve(USD_OVERNIGHT_INDEX, usdOvernightYieldCurve);

    ConstantDoublesCurve gbpOvernightConstantCurve = new ConstantDoublesCurve(1.3, GBP_OVERNIGHT);
    YieldCurve gbpOvernightYieldCurve = YieldCurve.from(gbpOvernightConstantCurve);
    multicurve.setCurve(GBP_OVERNIGHT_INDEX, gbpOvernightYieldCurve);

    ConstantDoublesCurve eurLibor6mConstantCurve = new ConstantDoublesCurve(1.4, EUR_LIBOR_6M);
    YieldCurve eurLibor6mYieldCurve = YieldCurve.from(eurLibor6mConstantCurve);
    multicurve.setCurve(EUR_LIBOR_6M_INDEX, eurLibor6mYieldCurve);

    ConstantDoublesCurve usdLibor3mConstantCurve = new ConstantDoublesCurve(1.5, USD_LIBOR_3M);
    YieldCurve usdLibor3mYieldCurve = YieldCurve.from(usdLibor3mConstantCurve);
    multicurve.setCurve(USD_LIBOR_3M_INDEX, usdLibor3mYieldCurve);

    return new MulticurveBundle(multicurve, new CurveBuildingBlockBundle());
  }

  static void initializeServiceContext() {
    ThreadLocalServiceContext.init(SERVICE_CONTEXT);
  }

  private static List<? extends CurveTypeConfiguration> list(CurveTypeConfiguration configuration) {
    return ImmutableList.of(configuration);
  }
}
