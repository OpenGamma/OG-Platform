/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.financial.credit.RestructuringClause;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.BarrierDirection;
import com.opengamma.financial.security.option.BarrierType;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.MonitoringType;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SamplingFrequency;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.FixedInflationSwapLeg;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.VolatilitySwapType;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public class ExposureFunctionTestHelper {
  private static final Currency USD = Currency.USD;
  private static final Currency EUR = Currency.EUR;
  private static final ExternalId US = ExternalId.of("Test", "US");
  private static final ExternalId DE = ExternalId.of("Test", "DE");
  private static final DayCount DC = DayCounts.THIRTY_U_360;
  private static final BusinessDayConvention BDC = BusinessDayConventions.NONE;
  private static final String SETTLEMENT = "X";
  private static final String TRADING = "Y";

  public static AgricultureForwardSecurity getAgricultureForwardSecurity() {
    final AgricultureForwardSecurity security = new AgricultureForwardSecurity("Cows", 100., new Expiry(DateUtils.getUTCDate(2013, 1, 1)), USD, 10000, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "7894"));
    return security;
  }

  public static AgricultureFutureSecurity getAgricultureFutureSecurity() {
    final AgricultureFutureSecurity security = new AgricultureFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD, 1000, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "213"));
    return security;
  }

  public static BondFutureOptionSecurity getBondFutureOptionSecurity() {
    final UniqueId underlyingId = getBondFutureSecurity().getUniqueId();
    final BondFutureOptionSecurity security = new BondFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 1000, false, EUR, 99, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "3873"));
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
    final CashSecurity security = new CashSecurity(USD, US, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2014, 1, 1), DC, 0.01, 10000);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return security;
  }

  public static ContinuousZeroDepositSecurity getContinuousZeroDepositSecurity() {
    final ContinuousZeroDepositSecurity security = new ContinuousZeroDepositSecurity(EUR, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 2, 1), 0.001, DE);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "556"));
    return security;
  }

  public static CorporateBondSecurity getCorporateBondSecurity() {
    final CorporateBondSecurity security = new CorporateBondSecurity("OG", "Company", "US", "US", USD, SimpleYieldConvention.TRUE, new Expiry(DateUtils.getUTCDate(2020, 1, 1)),
        "Coupon", 0.01, PeriodFrequency.SEMI_ANNUAL,
        DC, DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), 100., 300, 1, 1, 100, 1);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "94876"));
    return security;
  }

  public static CreditDefaultSwapIndexDefinitionSecurity getCreditDefaultSwapIndexDefinitionSecurity() {
    final CDSIndexTerms terms = CDSIndexTerms.of(Tenor.ONE_YEAR);
    final CDSIndexComponentBundle components = CDSIndexComponentBundle.of(new CreditDefaultSwapIndexComponent("NAME", ExternalId.of("Test", "A"), 1., ExternalId.of("Test", "Bond")));
    final CreditDefaultSwapIndexDefinitionSecurity security = new CreditDefaultSwapIndexDefinitionSecurity("1", "1", "All", USD, 0.02, terms, components);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1223"));
    return security;
  }

  public static CreditDefaultSwapIndexSecurity getCreditDefaultSwapIndexSecurity() {
    final UniqueId underlyingId = getCreditDefaultSwapIndexDefinitionSecurity().getUniqueId();
    final CreditDefaultSwapIndexSecurity security = new CreditDefaultSwapIndexSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()),
        DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), true, false, DateUtils.getUTCDate(2013, 1, 1), false, new InterestRateNotional(EUR, 1), 0.02);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23165"));
    return security;
  }

  public static CreditDefaultSwapOptionSecurity getCreditDefaultSwapOptionSecurity() {
    final UniqueId underlyingId = getStandardVanillaCDSSecurity().getUniqueId();
    final CreditDefaultSwapOptionSecurity security = new CreditDefaultSwapOptionSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), DateUtils.getUTCDate(2012, 1, 1),
        DateUtils.getUTCDate(2012, 12, 1), USD, 100., 100., false, false, new AmericanExerciseType(), ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "981"));
    return security;
  }

  public static DeliverableSwapFutureSecurity getDeliverableSwapFutureSecurity() {
    final UniqueId underlyingId = getPayFixedFloatSwapSecurity().getUniqueId();
    final DeliverableSwapFutureSecurity security = new DeliverableSwapFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 1, 1)), TRADING, SETTLEMENT, EUR, 100, "Swap",
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 10000);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1441"));
    return security;
  }

  public static EnergyForwardSecurity getEnergyForwardSecurity() {
    final EnergyForwardSecurity security = new EnergyForwardSecurity("Watts", 100., new Expiry(DateUtils.getUTCDate(2013, 1, 1)), USD, 10000, "Energy");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "784"));
    return security;
  }

  public static CommodityFutureOptionSecurity getEnergyFutureOptionSecurity() {
    final UniqueId underlyingId = getEnergyFutureSecurity().getUniqueId();
    final CommodityFutureOptionSecurity security = new CommodityFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 125, EUR, 120, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9357"));
    return security;
  }

  public static EnergyFutureSecurity getEnergyFutureSecurity() {
    final EnergyFutureSecurity security = new EnergyFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD, 1000, "Energy");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "456"));
    return security;
  }

  public static EquityBarrierOptionSecurity getEquityBarrierOptionSecurity() {
    final UniqueId underlyingId = getEquitySecurity().getUniqueId();
    final EquityBarrierOptionSecurity security = new EquityBarrierOptionSecurity(OptionType.PUT, 100, EUR, ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()),
        new EuropeanExerciseType(), new Expiry(DateUtils.getUTCDate(2013, 4, 1)), 150, SETTLEMENT, BarrierType.DOWN, BarrierDirection.KNOCK_IN, MonitoringType.CONTINUOUS,
        SamplingFrequency.CONTINUOUS, 110);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "989"));
    return security;
  }

  public static EquityFutureSecurity getEquityFutureSecurity() {
    final EquityFutureSecurity security = new EquityFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD,
        1000, DateUtils.getUTCDate(2013, 4, 2), ExternalSchemes.syntheticSecurityId("ABC"), "Equity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "345"));
    return security;
  }

  public static EquityIndexDividendFutureOptionSecurity getEquityIndexDividendFutureOptionSecurity() {
    final UniqueId underlyingId = getEquityIndexDividendFutureSecurity().getUniqueId();
    final EquityIndexDividendFutureOptionSecurity security = new EquityIndexDividendFutureOptionSecurity(SETTLEMENT, new Expiry(DateUtils.getUTCDate(2013, 3, 1)), new EuropeanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 100, false, USD, 123, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "6879"));
    return security;
  }

  public static EquityIndexDividendFutureSecurity getEquityIndexDividendFutureSecurity() {
    final EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 6, 1)), TRADING, SETTLEMENT, USD, 100,
        DateUtils.getUTCDate(2013, 6, 1), ExternalSchemes.syntheticSecurityId("SPX"), "Equity Index");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "123"));
    return security;
  }

  public static EquityIndexFutureOptionSecurity getEquityIndexFutureOptionSecurity() {
    final UniqueId underlyingId = getEquityFutureSecurity().getUniqueId();
    final EquityIndexFutureOptionSecurity security = new EquityIndexFutureOptionSecurity(SETTLEMENT, new Expiry(DateUtils.getUTCDate(2013, 3, 1)), new EuropeanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 100, false, USD, 123, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "3957"));
    return security;
  }

  public static EquityIndexOptionSecurity getEquityIndexOptionSecurity() {
    final EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(OptionType.CALL, 400, EUR, ExternalSchemes.syntheticSecurityId("DJX"), new AmericanExerciseType(), new Expiry(DateUtils.getUTCDate(2015, 1, 1)), 20, SETTLEMENT);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "346"));
    return security;
  }

  public static EquityOptionSecurity getEquityOptionSecurity() {
    final UniqueId underlyingId = getEquitySecurity().getUniqueId();
    final EquityOptionSecurity security = new EquityOptionSecurity(OptionType.CALL, 400, EUR, ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), new AmericanExerciseType(), new Expiry(DateUtils.getUTCDate(2015, 1, 1)), 20, SETTLEMENT);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "574"));
    return security;
  }

  public static EquitySecurity getEquitySecurity() {
    final EquitySecurity security = new EquitySecurity(SETTLEMENT, TRADING, "OG", USD);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "98797"));
    return security;
  }

  public static EquityVarianceSwapSecurity getEquityVarianceSwapSecurity() {
    final EquityVarianceSwapSecurity security = new EquityVarianceSwapSecurity(ExternalSchemes.syntheticSecurityId("SPX"), USD, 130, 100000, false, 252, DateUtils.getUTCDate(2012, 1, 1),
        DateUtils.getUTCDate(2015, 1, 1), DateUtils.getUTCDate(2011, 1, 1), US, PeriodFrequency.DAILY);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "93867"));
    return security;
  }

  public static SwapSecurity getPayFixedFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg payLeg = new FixedInterestRateLeg(DC, PeriodFrequency.SEMI_ANNUAL, DE, BDC, notional, false, 0.04);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final SwapSecurity security = new SwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "65456"));
    return security;
  }

  public static SwapSecurity getReceiveFixedFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg receiveLeg = new FixedInterestRateLeg(DC, PeriodFrequency.SEMI_ANNUAL, DE, BDC, notional, false, 0.04);
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final SwapSecurity security = new SwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "65456"));
    return security;
  }

  public static SwapSecurity getFloatFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("6m Euribor"), FloatingRateType.IBOR);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final SwapSecurity security = new SwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "874"));
    return security;
  }

  public static FRASecurity getFRASecurity() {
    final FRASecurity security = new FRASecurity(USD, US, DateUtils.getUTCDate(2013, 3, 1), DateUtils.getUTCDate(2013, 6, 1), 0.02, 1000,
        ExternalSchemes.bloombergTickerSecurityId("US0003 Index"), DateUtils.getUTCDate(2013, 6, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1234"));
    return security;
  }

  public static ForwardSwapSecurity getPayForwardFixedFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg payLeg = new FixedInterestRateLeg(DC, PeriodFrequency.SEMI_ANNUAL, DE, BDC, notional, false, 0.04);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final ForwardSwapSecurity security = new ForwardSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg, DateUtils.getUTCDate(2014, 1, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23498"));
    return security;
  }

  public static ForwardSwapSecurity getReceiveForwardFixedFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg receiveLeg = new FixedInterestRateLeg(DC, PeriodFrequency.SEMI_ANNUAL, DE, BDC, notional, false, 0.04);
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final ForwardSwapSecurity security = new ForwardSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg, DateUtils.getUTCDate(2014, 1, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23498"));
    return security;
  }

  public static ForwardSwapSecurity getForwardFloatFloatSwapSecurity() {
    final InterestRateNotional notional = new InterestRateNotional(EUR, 1000000);
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("6m Euribor"), FloatingRateType.IBOR);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, notional, false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final ForwardSwapSecurity security = new ForwardSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg, DateUtils.getUTCDate(2014, 1, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23498"));
    return security;
  }

  public static ForwardSwapSecurity getForwardXCcySwapSecurity() {
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, ExternalSchemes.syntheticSecurityId("3m USD Libor"), FloatingRateType.IBOR);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, new InterestRateNotional(EUR, 100000), false, ExternalSchemes.syntheticSecurityId("6m Euribor"), FloatingRateType.IBOR);
    final ForwardSwapSecurity security = new ForwardSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg, DateUtils.getUTCDate(2014, 1, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23498"));
    return security;
  }

  public static FXBarrierOptionSecurity getFXBarrierOptionSecurity() {
    final FXBarrierOptionSecurity security = new FXBarrierOptionSecurity(EUR, USD, 10000, 12000, new Expiry(DateUtils.getUTCDate(2013, 7, 1)),
        DateUtils.getUTCDate(2013, 7, 3), BarrierType.DOWN, BarrierDirection.KNOCK_OUT, MonitoringType.CONTINUOUS, SamplingFrequency.CONTINUOUS, 1.1, false);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "321"));
    return security;
  }

  public static FXDigitalOptionSecurity getFXDigitalOptionSecurity() {
    final FXDigitalOptionSecurity security = new FXDigitalOptionSecurity(USD, EUR, 12000, 10000, EUR, new Expiry(DateUtils.getUTCDate(2014, 1, 1)), DateUtils.getUTCDate(2014, 1, 3), false);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "321"));
    return security;
  }

  public static FXForwardSecurity getFXForwardSecurity() {
    final FXForwardSecurity security = new FXForwardSecurity(EUR, 10000, USD, 12000, DateUtils.getUTCDate(2014, 1, 1), DE);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9385"));
    return security;
  }

  public static FxFutureOptionSecurity getFXFutureOptionSecurity() {
    final UniqueId underlyingId = getEnergyFutureSecurity().getUniqueId();
    final FxFutureOptionSecurity security = new FxFutureOptionSecurity(SETTLEMENT, TRADING, new Expiry(DateUtils.getUTCDate(2013, 1, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 125, EUR, 120, OptionType.CALL);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "9595"));
    return security;
  }

  public static FXFutureSecurity getFXFutureSecurity() {
    final FXFutureSecurity security = new FXFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 12, 1)), TRADING, SETTLEMENT, EUR, 100, USD, EUR, "Currency");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "987"));
    return security;
  }

  public static FXOptionSecurity getFXOptionSecurity() {
    final FXOptionSecurity security = new FXOptionSecurity(EUR, USD, 1200, 1000, new Expiry(DateUtils.getUTCDate(2015, 1, 1)), DateUtils.getUTCDate(2015, 1, 3), false, new AmericanExerciseType());
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "54"));
    return security;
  }

  public static FXVolatilitySwapSecurity getFXVolatilitySwapSecurity() {
    final FXVolatilitySwapSecurity security = new FXVolatilitySwapSecurity(USD, 10000, VolatilitySwapType.VEGA, 1, DateUtils.getUTCDate(2014, 1, 1),
        DateUtils.getUTCDate(2018, 1, 1), 252, DateUtils.getUTCDate(2014, 1, 1), DateUtils.getUTCDate(2018, 1, 1), PeriodFrequency.DAILY, EUR, USD);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "867786"));
    return security;
  }

  public static GovernmentBondSecurity getGovernmentBondSecurity() {
    final GovernmentBondSecurity security = new GovernmentBondSecurity("US", "US", "US", "US", USD, SimpleYieldConvention.TRUE, new Expiry(DateUtils.getUTCDate(2020, 1, 1)),
        "Coupon", 0.01, PeriodFrequency.SEMI_ANNUAL,
        DC, DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), 100., 300, 1, 1, 100, 1);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "78"));
    return security;
  }

  public static IndexFutureSecurity getIndexFutureSecurity() {
    final IndexFutureSecurity security = new IndexFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 12, 1)), TRADING, SETTLEMENT, EUR, 1000, "Equity Index");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "2345"));
    return security;
  }

  public static IRFutureOptionSecurity getInterestRateFutureOptionSecurity() {
    final UniqueId underlyingId = getIndexFutureSecurity().getUniqueId();
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(TRADING, new Expiry(DateUtils.getUTCDate(2013, 11, 1)), new AmericanExerciseType(),
        ExternalId.of(underlyingId.getScheme(), underlyingId.getValue()), 125, false, EUR, 97, OptionType.PUT);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "897"));
    return security;
  }

  public static InterestRateFutureSecurity getInterestRateFutureSecurity() {
    final InterestRateFutureSecurity security = new InterestRateFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 9, 1)), TRADING, SETTLEMENT, USD, 12500,
        ExternalSchemes.syntheticSecurityId("USD 3m Libor"), "Financial");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "4567"));
    return security;
  }

  public static FederalFundsFutureSecurity getFederalFundsFutureSecurity() {
    final FederalFundsFutureSecurity security = new FederalFundsFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 9, 1)), TRADING, SETTLEMENT, USD, 12500,
        ExternalSchemes.syntheticSecurityId("Fed Funds"), "Financial");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "867"));
    return security;
  }

  public static LegacyFixedRecoveryCDSSecurity getLegacyFixedRecoveryCDSSecurity() {
    final LegacyFixedRecoveryCDSSecurity security = new LegacyFixedRecoveryCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), 0.4, true, false, 0.01);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static LegacyRecoveryLockCDSSecurity getLegacyRecoveryLockCDSSecurity() {
    final LegacyRecoveryLockCDSSecurity security = new LegacyRecoveryLockCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), 0.4, true, false, 0.01);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static LegacyVanillaCDSSecurity getLegacyVanillaCDSSecurity() {
    final LegacyVanillaCDSSecurity security = new LegacyVanillaCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), true, false, 0.01);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static MetalForwardSecurity getMetalForwardSecurity() {
    final MetalForwardSecurity security = new MetalForwardSecurity("Troy oz", 100., new Expiry(DateUtils.getUTCDate(2013, 1, 1)), USD, 10000, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "841351"));
    return security;
  }

  public static MetalFutureSecurity getMetalFutureSecurity() {
    final MetalFutureSecurity security = new MetalFutureSecurity(new Expiry(DateUtils.getUTCDate(2013, 4, 1)), TRADING, SETTLEMENT, USD, 100, "Commodity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "23456"));
    return security;
  }

  public static MunicipalBondSecurity getMunicipalBondSecurity() {
    final MunicipalBondSecurity security = new MunicipalBondSecurity("NY", "NY", "NY", "NY", USD, SimpleYieldConvention.TRUE, new Expiry(DateUtils.getUTCDate(2020, 1, 1)),
        "Coupon", 0.01, PeriodFrequency.SEMI_ANNUAL,
        DC, DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), DateUtils.getUTCDate(2010, 1, 1), 100., 300, 1, 1, 100, 1);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "65465"));
    return security;
  }

  public static NonDeliverableFXDigitalOptionSecurity getNonDeliverableFXDigitalOptionSecurity() {
    final NonDeliverableFXDigitalOptionSecurity security = new NonDeliverableFXDigitalOptionSecurity(USD, EUR, 12000, 10000, EUR,
        new Expiry(DateUtils.getUTCDate(2014, 1, 1)), DateUtils.getUTCDate(2014, 1, 3), false, false);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "321"));
    return security;
  }

  public static NonDeliverableFXForwardSecurity getNonDeliverableFXForwardSecurity() {
    final NonDeliverableFXForwardSecurity security = new NonDeliverableFXForwardSecurity(EUR, 10000, USD, 12000, DateUtils.getUTCDate(2014, 1, 1), DE, true);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "34"));
    return security;
  }

  public static NonDeliverableFXOptionSecurity getNonDeliverableFXOptionSecurity() {
    final NonDeliverableFXOptionSecurity security = new NonDeliverableFXOptionSecurity(EUR, USD, 1200, 1000, new Expiry(DateUtils.getUTCDate(2015, 1, 1)), DateUtils.getUTCDate(2015, 1, 3), false, new AmericanExerciseType(), true);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "5654"));
    return security;
  }

  public static PeriodicZeroDepositSecurity getPeriodicZeroDepositSecurity() {
    final PeriodicZeroDepositSecurity security = new PeriodicZeroDepositSecurity(USD, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 2, 1), 0.02, 1, US);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "571"));
    return security;
  }

  public static SimpleZeroDepositSecurity getSimpleZeroDepositSecurity() {
    final SimpleZeroDepositSecurity security = new SimpleZeroDepositSecurity(USD, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 2, 1), 0.00992, US);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "571"));
    return security;
  }

  public static StandardFixedRecoveryCDSSecurity getStandardFixedRecoveryCDSSecurity() {
    final StandardFixedRecoveryCDSSecurity security = new StandardFixedRecoveryCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), 0.4, true, false, 0.01, new InterestRateNotional(EUR, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static StandardRecoveryLockCDSSecurity getStandardRecoveryLockCDSSecurity() {
    final StandardRecoveryLockCDSSecurity security = new StandardRecoveryLockCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), 0.4, true, false, 0.01, new InterestRateNotional(EUR, 1));
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static StandardVanillaCDSSecurity getStandardVanillaCDSSecurity() {
    final StandardVanillaCDSSecurity security = new StandardVanillaCDSSecurity(false, ExternalId.of("Test", "A"), ExternalId.of("Test", "B"), ExternalId.of("Test", "C"), DebtSeniority.JRSUBUT2,
        RestructuringClause.CR, DE, DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), StubType.LONG_END, PeriodFrequency.SEMI_ANNUAL, DC, BDC,
        false, false, false, new InterestRateNotional(EUR, 1000), true, false, 0.01, new InterestRateNotional(EUR, 1), 0.02, DateUtils.getUTCDate(2013, 1, 1), false);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "1216"));
    return security;
  }

  public static StockFutureSecurity getStockFutureSecurity() {
    final StockFutureSecurity security = new StockFutureSecurity(new Expiry(DateUtils.getUTCDate(2014, 1, 1)), TRADING, SETTLEMENT, USD, 10000, "Equity");
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "234"));
    return security;
  }

  public static SwaptionSecurity getPaySwaptionSecurity() {
    final UniqueId underlying = getPayFixedFloatSwapSecurity().getUniqueId();
    final SwaptionSecurity security = new SwaptionSecurity(false, ExternalId.of(underlying.getScheme(), underlying.getValue()), true, new Expiry(DateUtils.getUTCDate(2012, 1, 1)), false, EUR);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "5417"));
    return security;
  }

  public static SwaptionSecurity getReceiveSwaptionSecurity() {
    final UniqueId underlying = getReceiveFixedFloatSwapSecurity().getUniqueId();
    final SwaptionSecurity security = new SwaptionSecurity(false, ExternalId.of(underlying.getScheme(), underlying.getValue()), true, new Expiry(DateUtils.getUTCDate(2012, 1, 1)), false, EUR);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "5417"));
    return security;
  }

  public static SwapSecurity getXCcySwapSecurity() {
    final SwapLeg payLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, ExternalSchemes.syntheticSecurityId("3m USD Libor"), FloatingRateType.IBOR);
    final SwapLeg receiveLeg = new FloatingInterestRateLeg(DC, PeriodFrequency.QUARTERLY, DE, BDC, new InterestRateNotional(EUR, 100000), false, ExternalSchemes.syntheticSecurityId("3m Euribor"), FloatingRateType.IBOR);
    final SwapSecurity security = new SwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        payLeg, receiveLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "311"));
    return security;
  }

  public static YearOnYearInflationSwapSecurity getPayYoYInflationSwapSecurity() {
    final FixedInflationSwapLeg fixedLeg = new FixedInflationSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, 0.02);
    final InflationIndexSwapLeg indexLeg = new InflationIndexSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), true, ExternalSchemes.syntheticSecurityId("CPI"), 2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final YearOnYearInflationSwapSecurity security = new YearOnYearInflationSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        fixedLeg, indexLeg, true, true, Tenor.TEN_YEARS);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "36"));
    return security;
  }

  public static YearOnYearInflationSwapSecurity getReceiveYoYInflationSwapSecurity() {
    final FixedInflationSwapLeg fixedLeg = new FixedInflationSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, 0.02);
    final InflationIndexSwapLeg indexLeg = new InflationIndexSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, ExternalSchemes.syntheticSecurityId("CPI"), 2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final YearOnYearInflationSwapSecurity security = new YearOnYearInflationSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        indexLeg, fixedLeg, true, true, Tenor.TEN_YEARS);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "4562"));
    return security;
  }

  public static ZeroCouponInflationSwapSecurity getPayZeroCouponInflationSwapSecurity() {
    final FixedInflationSwapLeg fixedLeg = new FixedInflationSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, 0.02);
    final InflationIndexSwapLeg indexLeg = new InflationIndexSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), true, ExternalSchemes.syntheticSecurityId("CPI"), 2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final ZeroCouponInflationSwapSecurity security = new ZeroCouponInflationSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        fixedLeg, indexLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "684"));
    return security;
  }

  public static ZeroCouponInflationSwapSecurity getReceiveZeroCouponInflationSwapSecurity() {
    final FixedInflationSwapLeg fixedLeg = new FixedInflationSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, 0.02);
    final InflationIndexSwapLeg indexLeg = new InflationIndexSwapLeg(DC, PeriodFrequency.QUARTERLY, US, BDC, new InterestRateNotional(USD, 100000), false, ExternalSchemes.syntheticSecurityId("CPI"), 2, 3, InterpolationMethod.MONTH_START_LINEAR);
    final ZeroCouponInflationSwapSecurity security = new ZeroCouponInflationSwapSecurity(DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2013, 1, 1), DateUtils.getUTCDate(2023, 1, 1), "OG",
        fixedLeg, indexLeg);
    security.setUniqueId(UniqueId.of(UniqueId.EXTERNAL_SCHEME.getName(), "3216"));
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
