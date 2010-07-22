/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.InMemoryRegionRepository;
import com.opengamma.financial.Region;
import com.opengamma.financial.RegionRepository;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.VanillaPayoffStyle;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Utility class that creates Test Objects 
 *
 */
public class SecurityTestUtils {

  public static final Currency USD = Currency.getInstance("USD");
  public static final Currency AUD = Currency.getInstance("AUD");
  public static final Currency EUR = Currency.getInstance("EUR");
  public static final Currency GBP = Currency.getInstance("GBP");

  public static final String ATT_BUID = "EQ0010137600001000";
  public static final String AAPL_BUID = "EQ0010169500001000";
  public static final String APV_EQUITY_OPTION_TICKER = "APV US 01/16/10 C190 Equity";
  public static final String SPX_INDEX_OPTION_TICKER = "SPX US 12/18/10 C1100 Index";
  public static final String AAPL_EQUITY_TICKER = "AAPL US Equity";
  public static final String ATT_EQUITY_TICKER = "T US Equity";

  protected SecurityTestUtils() {
  }

  public static IdentifierBundle makeBloombergTickerIdentifier(String secDes) {
    return new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, secDes));
  }

  public static EquitySecurity makeExpectedAAPLEquitySecurity() {
    EquitySecurity equitySecurity = new EquitySecurity("NASDAQ/NGS (GLOBAL SELECT MARKET)", "XNGS", "APPLE INC", USD);
    equitySecurity.setGicsCode(GICSCode.getInstance(45202010));
    equitySecurity.setTicker("AAPL");
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, AAPL_EQUITY_TICKER));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, AAPL_BUID));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "037833100"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US0378331005"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "2046251"));
    return equitySecurity;
  }

  public static EquityOptionSecurity makeAPVLEquityOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 190.0;
    Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 01, 16), ExpiryAccuracy.DAY_MONTH_YEAR);
    Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, AAPL_BUID);
    EquityOptionSecurity security = new EquityOptionSecurity(new AmericanExerciseType(), new VanillaPayoffStyle(), optionType, strike, expiry, underlyingUniqueID, USD, 1, // TODO change when point
        // value is properly added
        "US");

    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, APV_EQUITY_OPTION_TICKER));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EO1016952010010397C00001"));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    security.setName("APV 2010-01-16 C 190.0");

    return security;
  }

  public static EquityOptionSecurity makeSPXIndexOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 1100.0;
    Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 12, 18), ExpiryAccuracy.DAY_MONTH_YEAR);
    Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "EI09SPX");
    EquityOptionSecurity security = new EquityOptionSecurity(new EuropeanExerciseType(), new VanillaPayoffStyle(), optionType, strike, expiry, underlyingUniqueID, USD, 1, // TODO change when the point
        // value is properly added
        "US");

    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX5801809-0-8980"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    security.setName("SPX 2010-12-18 C 1100.0");
    return security;
  }

  public static AgricultureFutureSecurity makeWheatFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    AgricultureFutureSecurity sec = new AgricultureFutureSecurity(expiry, "XMTB", "XMTB", USD, "Wheat");
    sec.setUnitNumber(100.0);
    sec.setUnitName("tonnes");
    sec.setName("WHEAT FUT (ING) Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX8114863-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "VKM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "VKM0 Comdty"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static IndexFutureSecurity makeIndexFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    Identifier underlying = new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "SPX Index");
    IndexFutureSecurity sec = new IndexFutureSecurity(expiry, "XCME", "XCME", USD);
    sec.setUnderlyingIdentifier(underlying);
    sec.setName("S&P 500 FUTURE Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX6835907-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "SPM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "SPM0 Index"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static FXFutureSecurity makeAUDUSDCurrencyFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    FXFutureSecurity security = new FXFutureSecurity(expiry, "XCME", "XCME", USD, AUD, USD);
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "LNM0 Curncy"));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    return security;
  }

  public static BondFutureSecurity makeEuroBondFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.SEPTEMBER, 8).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>();
    IdentifierBundle ids = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3 07/04/20 Corp"));
    basket.add(new BondFutureDeliverable(ids, 0.781866));
    ids = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3.25 01/04/20 Corp"));
    basket.add(new BondFutureDeliverable(ids, 0.807685));
    ids = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3.5 07/04/19 Corp"));
    basket.add(new BondFutureDeliverable(ids, 0.832496));
    BondFutureSecurity security = new BondFutureSecurity(expiry, "XEUR", "XEUR", EUR, basket, "bond");
    security.setName("EURO-BUND FUTURE Sep10");
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "IX9822660-0"));
    identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "RXU0 Comdty"));
    identifiers.add(Identifier.of(IdentificationScheme.CUSIP, "RXU0"));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    return security;
  }

  public static MetalFutureSecurity makeSilverFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    MetalFutureSecurity sec = new MetalFutureSecurity(expiry, "XCEC", "XCEC", USD, "Precious Metal");
    sec.setUnitNumber(5000.00);
    sec.setUnitName("troy oz.");
    sec.setName("SILVER FUTURE Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX10217289-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "SIM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "SIM0 Comdty"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static EnergyFutureSecurity makeEthanolFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    EnergyFutureSecurity sec = new EnergyFutureSecurity(expiry, "XCBT", "XCBT", USD, "Refined Products");
    sec.setUnitNumber(29000.00);
    sec.setUnitName("U.S. Gallons");
    sec.setName("DENATURED ETHANOL Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX6054783-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "DLM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "DLM0 Comdty"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static InterestRateFutureSecurity makeInterestRateFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    InterestRateFutureSecurity sec = new InterestRateFutureSecurity(expiry, "XCME", "XCME", USD, "LIBOR");
    sec.setName("90DAY EURO$ FUTR  Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX166549-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "EDM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "EDM0 Comdty"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static CashSecurity makeCashSecurity() {
    // TODO 2010-07-22 Andrew - Create a more realistic security
    final CashSecurity sec = new CashSecurity();
    sec.setName("Test cash security");
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier("TEST", "CASH"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static FRASecurity makeFRASecurity() {
    // TODO 2010-07-22 Andrew - create a more realistic security
    final FRASecurity sec = new FRASecurity(LocalDate.of(2010, MonthOfYear.JULY, 1).atMidnight(), LocalDate.of(2010, MonthOfYear.SEPTEMBER, 30).atMidnight());
    sec.setName("Test FRA security");
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier("TEST", "FRA"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static SwapSecurity makeSwapSecurity(final RegionRepository regionSource) {
    // TODO 2010-07-22 Andrew - create a more realistic security
    final Region us = regionSource.getHierarchyNodes(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.ISO_COUNTRY_2, "US").first ();
    final SwapLeg fixed = new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/365 (Fixed)"), SimpleFrequency.SEMI_ANNUAL, us, BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Following"), new InterestRateNotional(USD, 100.0), 0.01);
    final SwapLeg floating = new FloatingInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/365 (Fixed)"), SimpleFrequency.SEMI_ANNUAL, us, BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Following"), new InterestRateNotional(USD, 100.0), UniqueIdentifier.of("Ref", "LIBOR 6m"), 0.01, 0);
    final SwapSecurity sec = new SwapSecurity (LocalDate.of (2010, MonthOfYear.JULY, 15).atMidnight (), LocalDate.of (2010,MonthOfYear.JULY, 15).atMidnight (), LocalDate.of (2030, MonthOfYear.JULY, 15).atMidnight (), "OpenGamma", fixed, floating);
    sec.setName("Test swap security");
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier("TEST", "SWAP"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

  public static ForwardSwapSecurity makeForwardSwapSecurity(final RegionRepository regionSource) {
    // TODO 2010-07-22 Andrew - create a more realistic security
    final Region us = regionSource.getHierarchyNodes(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.ISO_COUNTRY_2, "US")
        .first();
    final SwapLeg fixed = new FixedInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/365 (Fixed)"), SimpleFrequency.SEMI_ANNUAL, us, BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Following"), new InterestRateNotional(USD, 100.0), 0.01);
    final SwapLeg floating = new FloatingInterestRateLeg(DayCountFactory.INSTANCE.getDayCount("Actual/365 (Fixed)"), SimpleFrequency.SEMI_ANNUAL, us, BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Following"), new InterestRateNotional(USD, 100.0), UniqueIdentifier.of("Ref", "LIBOR 6m"), 0.01, 0);
    final ForwardSwapSecurity sec = new ForwardSwapSecurity(LocalDate.of(2010, MonthOfYear.JULY, 15).atMidnight(), LocalDate.of(2010, MonthOfYear.JULY, 15).atMidnight(), LocalDate.of(2030,
        MonthOfYear.JULY, 15).atMidnight(), "OpenGamma", fixed, floating, LocalDate.of(2010, MonthOfYear.AUGUST, 22).atMidnight());
    sec.setName("Test forward swap security");
    final Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier("TEST", "FORWARDSWAP"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }

}
