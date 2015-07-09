/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.bondfuture;

import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityShortName;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.impl.WeekendHolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.exposure.CurrencyExposureFunction;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.ConventionBundleMaster;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.MasterRegionSource;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.impl.InMemorySecurityMaster;
import com.opengamma.master.security.impl.MasterSecuritySource;
import com.opengamma.sesame.IssuerProviderBundle;
import com.opengamma.sesame.marketdata.IssuerMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.marketdata.MarketDataEnvironmentBuilder;
import com.opengamma.sesame.marketdata.VolatilitySurfaceId;
import com.opengamma.sesame.trade.BondFutureOptionTrade;
import com.opengamma.sesame.trade.BondFutureTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Test data for bond futures and bond future options
 */
public class BondFutureTestData {

  private static ExternalId BOND_ID = ExternalSchemes.isinSecurityId("10Y JGB");
  private static ExternalId BOND_FUTURE_ID = ExternalSchemes.isinSecurityId("JBU5");
  private static ExternalId BOND_FUTURE_OPTION_ID = ExternalSchemes.isinSecurityId("JBN_146_5");
  private static String VOL_ID = "PUT_" + BOND_FUTURE_ID.getValue();
  private static String JP_NAME = "JP GOVT";
  public static final ZonedDateTime VALUATION_TIME =  DateUtils.getUTCDate(2015, 5, 12);

  public static BondFutureOptionTrade createBondFutureOptionTrade() {

    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(-100);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createBondFutureOptionSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumCurrency(Currency.JPY);
    return new BondFutureOptionTrade(trade);
  }

  private static BondFutureOptionSecurity createBondFutureOptionSecurity() {

    String tradingExchange = "";
    String settlementExchange = "";
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2015, 6, 30));
    ExerciseType exerciseType = new EuropeanExerciseType();
    ExternalId underlyingId = BOND_FUTURE_ID;
    double pointValue = 1;
    Currency currency = Currency.JPY;
    double strike = 1.465;
    OptionType optionType = OptionType.PUT;
    boolean margined = false;
    BondFutureOptionSecurity security = new BondFutureOptionSecurity(tradingExchange, settlementExchange, expiry,
                                                                     exerciseType, underlyingId, pointValue, margined,
                                                                     currency, strike, optionType);
    security.setExternalIdBundle(BOND_FUTURE_OPTION_ID.toBundle());
    return security;
  }

  public static BondFutureTrade createBondFutureTrade() {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(1);
    LocalDate tradeDate = LocalDate.of(2000, 1, 1);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createBondFutureSecurity(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.0);
    trade.setPremiumCurrency(Currency.JPY);
    return new BondFutureTrade(trade);
  }

  private static BondFutureSecurity createBondFutureSecurity() {

    Currency currency = Currency.JPY;

    Expiry expiry = new Expiry(DateUtils.getUTCDate(2015, 9, 9));
    String tradingExchange = "";
    String settlementExchange = "";
    double unitAmount = 100_000_000;
    Collection<BondFutureDeliverable> basket = new ArrayList<>();
    BondFutureDeliverable bondFutureDeliverable =
        new BondFutureDeliverable(BOND_ID.toBundle(), 0.706302);
    basket.add(bondFutureDeliverable);

    ZonedDateTime firstDeliveryDate = DateUtils.getUTCDate(2015, 9, 18);
    ZonedDateTime lastDeliveryDate = DateUtils.getUTCDate(2015, 9, 18);
    String category = "test";

    BondFutureSecurity security =  new BondFutureSecurity(expiry, tradingExchange, settlementExchange, currency, unitAmount, basket,
                                                          firstDeliveryDate, lastDeliveryDate, category);
    security.setExternalIdBundle(BOND_FUTURE_ID.toBundle());
    return security;
  }

  private static BondSecurity createGovernmentBondSecurity() {

    String issuerName = "JP GOVT";
    String issuerDomicile = "JP";
    String issuerType = "Sovereign";
    Currency currency = Currency.JPY;
    YieldConvention yieldConvention = YieldConventionFactory.INSTANCE.getYieldConvention("STREET CONVENTION");
    DayCount dayCountConvention = DayCounts.ACT_ACT_ISDA;

    Period couponPeriod = Period.parse("P6M");
    String couponType = "Fixed";
    double couponRate = 0.80;
    Frequency couponFrequency = PeriodFrequency.of(couponPeriod);

    ZonedDateTime maturityDate = DateUtils.getUTCDate(2022, 9, 20);
    ZonedDateTime firstCouponDate = DateUtils.getUTCDate(2013, 3, 20);
    ZonedDateTime interestAccrualDate = DateUtils.getUTCDate(2012, 9, 20);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2022, 9, 20);
    Expiry lastTradeDate = new Expiry(maturityDate);

    double issuancePrice = 1.0;
    double totalAmountIssued = 1.0;
    double minimumAmount = 1.0;
    double minimumIncrement = 1.0;
    double parAmount = 1.0;
    double redemptionValue = 1.0;

    GovernmentBondSecurity bond =
        new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, issuerType, currency, yieldConvention,
                                   lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention,
                                   interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
                                   totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);
    bond.setExternalIdBundle(BOND_ID.toBundle());
    return bond;
  }

  public static ExposureFunctions createExposureFunction() {
    List<String> exposureFunctions =  ImmutableList.of(CurrencyExposureFunction.NAME);
    Map<ExternalId, String> idsToNames = new HashMap<>();
    idsToNames.put(ExternalId.of("CurrencyISO", "JPY"), "IssuerMultiCurve");
    return new ExposureFunctions("Exposure", exposureFunctions, idsToNames);
  }

  public static ImmutableMap<Class<?>, Object> generateBaseComponents() {
    return generateComponentMap(getRegionSource(),
                                getHolidaySource(),
                                mock(HistoricalTimeSeriesSource.class),
                                getSecuritySource(),
                                mock(ConfigSource.class),
                                mock(ConventionSource.class),
                                getConventionBundleSource(),
                                mock(LegalEntitySource.class),
                                mock(CurrencyMatrix.class));
  }

  private static HolidaySource getHolidaySource() {
    return new WeekendHolidaySource();
  }

  private static RegionSource getRegionSource() {
    RegionMaster master = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(master);
    return new MasterRegionSource(master);
  }

  private static ConventionBundleSource getConventionBundleSource() {
    ConventionBundleMaster master = new InMemoryConventionBundleMaster();
    return new DefaultConventionBundleSource(master);
  }

  private static SecuritySource getSecuritySource() {

    SecurityMaster master = new InMemorySecurityMaster();
    master.add(new SecurityDocument(createGovernmentBondSecurity()));
    master.add(new SecurityDocument(createBondFutureSecurity()));
    return new MasterSecuritySource(master);
  }

  private static ImmutableMap<Class<?>, Object> generateComponentMap(Object... components) {
    ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();
    for (Object component : components) {
      builder.put(component.getClass().getInterfaces()[0], component);
    }
    return builder.build();
  }

  public static MarketDataEnvironment createMarketDataEnvironment() {
    MarketDataEnvironmentBuilder builder = new MarketDataEnvironmentBuilder();
    builder.add(IssuerMulticurveId.of("IssuerMultiCurve"), createIssuerBundle());
    builder.add(VolatilitySurfaceId.of(VOL_ID), createVolatilitySurface());
    builder.valuationTime(VALUATION_TIME);
    return builder.build();
  }

  private static IssuerProviderBundle createIssuerBundle() {
    Map<Pair<Object, LegalEntityFilter<LegalEntity>>, YieldAndDiscountCurve> issuer = new LinkedHashMap<>();
    Pair<Object, LegalEntityFilter<LegalEntity>> key =
        Pairs.<Object, LegalEntityFilter<LegalEntity>>of(JP_NAME, new LegalEntityShortName());
    issuer.put(key, createIssuerCurve());
    return new IssuerProviderBundle(new IssuerProviderDiscount(createDiscountingCurve(), issuer),
                                    new CurveBuildingBlockBundle());
  }

  private static YieldAndDiscountCurve createIssuerCurve() {
    String name = "JPY-JP-GOVT";
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    double[] time = {0.5, 1.0, 2.0, 5.0, 7.0, 10.0, 20.0};
    double[] zc = {-0.0001, -0.0002, -0.0002, 0.0012, 0.0030, 0.0045, 0.0115};
    InterpolatedDoublesCurve curve =
        new InterpolatedDoublesCurve(time, zc, linearFlat, true, name);
    return new YieldCurve(name, curve);
  }

  private static MulticurveProviderDiscount createDiscountingCurve() {
    String name = "JPY Discounting";
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    double[] time = {0.003, 0.25, 0.5, 1.0, 2.0, 5.0, 10.0};
    double[] zc = {0.0006, 0.0006, 0.0006, 0.0006, 0.0007, 0.0020, 0.0050};
    InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(time, zc, linearFlat, true, name);

    MulticurveProviderDiscount multicurve = new MulticurveProviderDiscount();
    multicurve.setCurve(Currency.JPY, new YieldCurve(name, curve));
    return multicurve;
  }

  private static VolatilitySurface createVolatilitySurface() {
    Interpolator1D linearFlat =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR,
                                                                Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    GridInterpolator2D interpolator2D = new GridInterpolator2D(linearFlat, linearFlat);
    InterpolatedDoublesSurface surface = InterpolatedDoublesSurface.from(
        new double[] {19.0/365.0, 19.0/365.0, 19.0/365.0, 19.0/365.0, 49.0/365.0, 49.0/365.0, 49.0/365.0, 49.0/365.0},
        new double[] {1.45, 1.46, 1.47, 1.48, 1.45, 1.46, 1.47, 1.48},
        new double[] {0.035, 0.032, 0.031, 0.028, 0.0325, 0.0315, 0.0305, 0.0295},
        interpolator2D
    );
    return new VolatilitySurface(surface);
  }

}
