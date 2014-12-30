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
import static com.opengamma.sesame.config.ConfigBuilder.function;
import static com.opengamma.sesame.config.ConfigBuilder.implementations;

import java.math.BigDecimal;
import java.util.List;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.link.ConfigLink;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.financial.analytics.curve.exposure.ExposureFunctions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.currency.CurrencyMatrix;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.DefaultCurveNodeConverterFn;
import com.opengamma.sesame.DefaultDiscountingIssuerProviderBundleFn;
import com.opengamma.sesame.ExposureFunctionsIssuerProviderFn;
import com.opengamma.sesame.IssuerProviderBundleFn;
import com.opengamma.sesame.IssuerProviderFn;
import com.opengamma.sesame.MarketExposureSelector;
import com.opengamma.sesame.RootFinderConfiguration;
import com.opengamma.sesame.bond.BondFn;
import com.opengamma.sesame.bond.DiscountingBondFn;
import com.opengamma.sesame.component.RetrievalPeriod;
import com.opengamma.sesame.component.StringSet;
import com.opengamma.sesame.config.ViewColumn;
import com.opengamma.sesame.marketdata.DefaultHistoricalMarketDataFn;
import com.opengamma.sesame.marketdata.DefaultMarketDataFn;
import com.opengamma.sesame.trade.BondTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Utility class for remote views
 */
public final class RemoteViewBondUtils {

  private RemoteViewBondUtils() { /* private constructor */ }

  /**
   * Utility for creating a fra specific view column
   * @param columnName column output name, not null
   * @param output output name, not null
   * @param exposureConfig exposure function config, not null
   * @param currencyMatrixLink currency matrix config, not null
   */
  public static ViewColumn createBondViewColumn(String columnName,
                                                String output,
                                                ConfigLink<ExposureFunctions> exposureConfig,
                                                ConfigLink<CurrencyMatrix> currencyMatrixLink) {
    ArgumentChecker.notNull(columnName, "column name");
    ArgumentChecker.notNull(output, "output");
    ArgumentChecker.notNull(exposureConfig, "exposureConfig");
    ArgumentChecker.notNull(currencyMatrixLink, "currencyMatrixLink");

    return
        column(
            columnName, output,
            config(
                arguments(
                    function(
                        MarketExposureSelector.class,
                        argument("exposureFunctions", exposureConfig)),
                    function(
                        RootFinderConfiguration.class,
                        argument("rootFinderAbsoluteTolerance", 1e-10),
                        argument("rootFinderRelativeTolerance", 1e-10),
                        argument("rootFinderMaxIterations", 1000)),
                    function(
                        DefaultCurveNodeConverterFn.class,
                        argument("timeSeriesDuration", RetrievalPeriod.of(Period.ofYears(1)))),
                    function(
                        DefaultHistoricalMarketDataFn.class,
                        argument("dataSource", "BLOOMBERG"),
                        argument("currencyMatrix", currencyMatrixLink)),
                    function(
                        DefaultMarketDataFn.class,
                        argument("dataSource", "BLOOMBERG"),
                        argument("currencyMatrix", currencyMatrixLink)),
                    function(
                        DefaultDiscountingIssuerProviderBundleFn.class,
                        argument("impliedCurveNames", StringSet.of()))),
                implementations(
                    BondFn.class, DiscountingBondFn.class,
                    IssuerProviderBundleFn.class, DefaultDiscountingIssuerProviderBundleFn.class,
                    IssuerProviderFn.class, ExposureFunctionsIssuerProviderFn.class)));
  }


  /** List of Bond inputs */
  public static final List<Object> BOND_INPUTS = 
      ImmutableList.<Object>of(createGovernmentBondTradeSpot(), createGovernmentBondTradePast(),
          createGovernmentBondTradeFwd());


  private static BondTrade createGovernmentBondTradeSpot() {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(10_000_000);
    LocalDate tradeDate = LocalDate.of(2014, 7, 11);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createGovernmentBondSecurityUK(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.99);
    trade.setPremiumDate(LocalDate.of(2014, 7, 16));
    trade.setPremiumCurrency(Currency.GBP);
    return new BondTrade(trade);
  }
  
  private static BondTrade createGovernmentBondTradePast() {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(10_000_000);
    LocalDate tradeDate = LocalDate.of(2014, 7, 9);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createGovernmentBondSecurityUK(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.99);
    trade.setPremiumDate(LocalDate.of(2014, 7, 10));
    trade.setPremiumCurrency(Currency.GBP);
    return new BondTrade(trade);
  }
  
  private static BondTrade createGovernmentBondTradeFwd() {
    Counterparty counterparty = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "COUNTERPARTY"));
    BigDecimal tradeQuantity = BigDecimal.valueOf(10_000_000);
    LocalDate tradeDate = LocalDate.of(2014, 7, 11);
    OffsetTime tradeTime = OffsetTime.of(LocalTime.of(0, 0), ZoneOffset.UTC);
    SimpleTrade trade = new SimpleTrade(createGovernmentBondSecurityUK(), tradeQuantity, counterparty, tradeDate, tradeTime);
    trade.setPremium(0.99);
    trade.setPremiumDate(LocalDate.of(2014, 7, 25));
    trade.setPremiumCurrency(Currency.GBP);
    return new BondTrade(trade);
  }

  private static BondSecurity createGovernmentBondSecurityUK() {
    // TODO: only 1 security for 3 trade?

    String issuerName = "UK TREASURY";
    String issuerDomicile = "GB";
    String issuerType = "Sovereign";
    Currency currency = Currency.GBP;
    YieldConvention yieldConvention = SimpleYieldConvention.UK_BUMP_DMO_METHOD;
    DayCount dayCountConvention = DayCounts.ACT_ACT_ICMA;

    Period couponPeriod = Period.parse("P6M");
    String couponType = "Fixed";
    double couponRate = 8.0;
    Frequency couponFrequency = PeriodFrequency.of(couponPeriod);

    ZonedDateTime maturityDate = DateUtils.getUTCDate(2021, 6, 7);
    ZonedDateTime firstCouponDate = DateUtils.getUTCDate(2011, 12, 7);
    ZonedDateTime interestAccrualDate = DateUtils.getUTCDate(2011, 6, 7);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2011, 6, 7);
    Expiry lastTradeDate = new Expiry(maturityDate);

    double issuancePrice = 100;
    double totalAmountIssued = 32980000000.0;
    double minimumAmount = 100;
    double minimumIncrement = 100;
    double parAmount = 100;
    double redemptionValue = 100;

    GovernmentBondSecurity bond =
        new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, issuerType, currency, yieldConvention,
                                   lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention,
                                   interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
                                   totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);

    ExternalId isinId = ExternalSchemes.isinSecurityId("GB0009997999");
    ExternalId bloombergId = ExternalSchemes.bloombergTickerSecurityId("UKT 8 2021-06-07 Govt");
    bond.setExternalIdBundle(ExternalIdBundle.of(isinId, bloombergId));
    return bond;
  }

  private static BondSecurity createGovernmentBondSecurityUS() {

    String issuerName = "US TREASURY N/B";
    String issuerDomicile = "US";
    String issuerType = "Sovereign";
    Currency currency = Currency.USD;
    YieldConvention yieldConvention = SimpleYieldConvention.US_STREET;
    DayCount dayCountConvention = DayCounts.ACT_ACT_ICMA;

    Period couponPeriod = Period.parse("P6M");
    String couponType = "Fixed";
    double couponRate = 5.0;
    Frequency couponFrequency = PeriodFrequency.of(couponPeriod);

    ZonedDateTime maturityDate = DateUtils.getUTCDate(2014, 8, 15);
    ZonedDateTime firstCouponDate = DateUtils.getUTCDate(2012, 2, 15);
    ZonedDateTime interestAccrualDate = DateUtils.getUTCDate(2011, 8, 15);
    ZonedDateTime settlementDate = DateUtils.getUTCDate(2014, 8, 17);
    Expiry lastTradeDate = new Expiry(maturityDate);

    double issuancePrice = 100;
    double totalAmountIssued = 32980000000.0;
    double minimumAmount = 100;
    double minimumIncrement = 100;
    double parAmount = 100;
    double redemptionValue = 100;

    GovernmentBondSecurity bond =
        new GovernmentBondSecurity(issuerName, issuerType, issuerDomicile, issuerType, currency, yieldConvention,
                                   lastTradeDate, couponType, couponRate, couponFrequency, dayCountConvention,
                                   interestAccrualDate, settlementDate, firstCouponDate, issuancePrice,
                                   totalAmountIssued, minimumAmount, minimumIncrement, parAmount, redemptionValue);

    ExternalId isinId = ExternalSchemes.isinSecurityId("US912828RB87");
    ExternalId bloombergId = ExternalSchemes.bloombergTickerSecurityId("T 0.5 08/15/14 Govt");
    bond.setExternalIdBundle(ExternalIdBundle.of(isinId, bloombergId));
    return bond;
  }

}
