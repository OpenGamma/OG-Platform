/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class ExposureFunctionTestHelper {
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final ExternalId US = ExternalId.of("Test", "US");
  private static final ExternalId DE = ExternalId.of("Test", "DE");
  private static final DayCount DC = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final String SETTLEMENT = "X";
  private static final String TRADING = "Y";

  public static AgricultureFutureSecurity getAgricultureFutureSecurity() {
    final AgricultureFutureSecurity security = new AgricultureFutureSecurity(new Expiry(DateUtils.getUTCDate(2014, 1, 1)), TRADING, SETTLEMENT, EUR, 100, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "12"));
    return security;
  }

  public static BondFutureOptionSecurity getBondFutureOptionSecurity() {
    final UniqueId underlyingId = getBondFutureSecurity().getUniqueId();
    final BondFutureOptionSecurity security = new BondFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 1000, EUR, 99, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9357"));
    return security;
  }

  public static BondFutureSecurity getBondFutureSecurity() {
    final Collection<BondFutureDeliverable> basket = Collections.emptySet();
    final BondFutureSecurity security = new BondFutureSecurity(new Expiry(DateUtils.getUTCDate(2015, 1, 1)), TRADING, SETTLEMENT, EUR, 1200, basket, DateUtils.getUTCDate(2015, 1, 1),
        DateUtils.getUTCDate(2015, 2, 1), "Financial");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "12345"));
    return security;
  }

  public static CapFloorCMSSpreadSecurity getCapFloorCMSSpreadSecurity() {
    final CapFloorCMSSpreadSecurity security = new CapFloorCMSSpreadSecurity(DateUtils.getUTCDate(2012, 1, 1), DateUtils.getUTCDate(2022, 1, 1), 100000, ExternalSchemes.syntheticSecurityId("USD 10y Swap"),
        ExternalSchemes.syntheticSecurityId("USD 15y Swap"), 0.002, PeriodFrequency.SEMI_ANNUAL, EUR, DC, true, false);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "2643"));
    return security;
  }

  public static CapFloorSecurity getCapFloorSecurity() {
    final CapFloorSecurity security = new CapFloorSecurity(DateUtils.getUTCDate(2012, 2, 1), DateUtils.getUTCDate(2017, 2, 1), 10000,
        ExternalSchemes.syntheticSecurityId("USD 6m Libor"), 0.003, PeriodFrequency.ANNUAL, USD, DC, false, true, true);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "10395"));
    return security;
  }

  public static CashFlowSecurity getCashFlowSecurity() {
    final CashFlowSecurity security = new CashFlowSecurity(EUR, DateUtils.getUTCDate(2013, 9, 1), 10000);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "34985"));
    return security;
  }

  public static CashSecurity getCashSecurity() {
    final CashSecurity cash = new CashSecurity(USD, US, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DC, 0.01, 10000);
    cash.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return cash;
  }

  public static ContinuousZeroDepositSecurity getContinuousZeroDepositSecurity() {
    final ContinuousZeroDepositSecurity security = new ContinuousZeroDepositSecurity(EUR, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 2, 1), 0.001, DE);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return security;
  }

  public static CorporateBondSecurity getCorporateBondSecurity() {
    final CorporateBondSecurity security = new CorporateBondSecurity("OG", "Company", "US", "US", USD, SimpleYieldConvention.TRUE, new Expiry(DateUtils.getUTCDate(2020, 1, 1)), "Coupon", 0.01, PeriodFrequency.SEMI_ANNUAL,
        DC, DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), 100., 300, 1, 1, 100, 1);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "94876"));
    return security;
  }

  public static EnergyFutureSecurity getEnergyFutureSecurity() {
    final EnergyFutureSecurity security = new EnergyFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD, 1000, "Energy");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "456"));
    return security;
  }

  public static CommodityFutureOptionSecurity getEnergyFutureOptionSecurity() {
    final UniqueId underlyingId = getEnergyFutureSecurity().getUniqueId();
    final CommodityFutureOptionSecurity security = new CommodityFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 125, EUR, 120, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9357"));
    return security;
  }

  public static EquityFutureSecurity getEquityFutureSecurity() {
    final EquityFutureSecurity security = new EquityFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD,
        1000, DateUtils.getUTCDate(2013, 4, 2), ExternalSchemes.syntheticSecurityId("ABC"), "Equity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "345"));
    return security;
  }

  public static EquityIndexDividendFutureSecurity getEquityIndexDividendFutureSecurity() {
    final EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 6, 1)), TRADING, SETTLEMENT, USD, 100,
        DateUtils.getUTCDate(2013, 6, 1), ExternalSchemes.syntheticSecurityId("SPX"), "Equity Index");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return security;
  }

  public static FRASecurity getFRASecurity() {
    final FRASecurity fra = new FRASecurity(USD, US, DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), 0.02, 1000,
        ExternalSchemes.bloombergTickerSecurityId("US0003 Index"), DateUtils.getUTCDate(2013, 6, 1));
    fra.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1234"));
    return fra;
  }

  public static FxFutureOptionSecurity getFXFutureOptionSecurity() {
    final UniqueId underlyingId = getEnergyFutureSecurity().getUniqueId();
    final FxFutureOptionSecurity security = new FxFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 125, EUR, 120, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9357"));
    return security;
  }

  public static FXFutureSecurity getFXFutureSecurity() {
    final FXFutureSecurity security = new FXFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 12, 1)), TRADING, SETTLEMENT, EUR, 100, USD, EUR, "Currency");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "987"));
    return security;
  }

  public static IndexFutureSecurity getIndexFutureSecurity() {
    final IndexFutureSecurity security = new IndexFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 12, 1)), TRADING, SETTLEMENT, EUR, 1000, "Equity Index");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "2345"));
    return security;
  }

  public static InterestRateFutureSecurity getInterestRateFutureSecurity() {
    final InterestRateFutureSecurity security = new InterestRateFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 9, 1)), TRADING, SETTLEMENT, USD, 12500,
        ExternalSchemes.syntheticSecurityId("USD 3m Libor"), "Financial");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "4567"));
    return security;
  }

  public static MetalFutureSecurity getMetalFutureSecurity() {
    final MetalFutureSecurity security = new MetalFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD, 100, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23456"));
    return security;
  }

  public static StockFutureSecurity getStockFutureSecurity() {
    final StockFutureSecurity security = new StockFutureSecurity(new Expiry(DateUtils.getUTCDate(2014, 1, 1)), TRADING, SETTLEMENT, USD, 10000, "Equity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "234"));
    return security;
  }

  public static SecuritySource getSecuritySource(final Security security) {
    return new SecuritySource() {

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Collection<Security>> getAll(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Collection<Security> get(final ExternalIdBundle bundle) {
        return null;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle) {
        return security;
      }

      @Override
      public Security getSingle(final ExternalIdBundle bundle, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<ExternalIdBundle, Security> getSingle(final Collection<ExternalIdBundle> bundles, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Security get(final UniqueId uniqueId) {
        return null;
      }

      @Override
      public Security get(final ObjectId objectId, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public Map<UniqueId, Security> get(final Collection<UniqueId> uniqueIds) {
        return null;
      }

      @Override
      public Map<ObjectId, Security> get(final Collection<ObjectId> objectIds, final VersionCorrection versionCorrection) {
        return null;
      }

      @Override
      public ChangeManager changeManager() {
        return null;
      }

    };
  }
}
