/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.bbg.BloombergSecuritySource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.GICSCode;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Bloomberg Security utility class to aid testing
 */
public final class BloombergSecurityUtils {

  /**
   * USD Currency
   */
  public static final Currency USD = Currency.USD;
  /**
   * AUD Currency
   */
  public static final Currency AUD = Currency.AUD;
  /**
   * EUR Currency
   */
  public static final Currency EUR = Currency.EUR;
  /**
   * GBP Currency
   */
  public static final Currency GBP = Currency.GBP;

  /**
   * ATT BUID
   */
  public static final String ATT_BUID = "EQ0010137600001000";
  /**
   * AAPL BUID
   */
  public static final String AAPL_BUID = "EQ0010169500001000";
  /**
   * AAPL OPTION
   */
  public static final String APV_EQUITY_OPTION_TICKER = "APV US 01/16/10 C190 Equity";
  // At times Bloomberg has changed this to SPT as the prefix in the past.
  /**
   * SPX index option
   */
  public static final String SPX_INDEX_OPTION_TICKER = "SPX US 12/18/10 C1100 Index";
  /**
   * AAPL ticker
   */
  public static final String AAPL_EQUITY_TICKER = "AAPL US Equity";
  /**
   * ATT ticker
   */
  public static final String ATT_EQUITY_TICKER = "T US Equity";

  private static int s_dst = java.util.TimeZone.getDefault().inDaylightTime(new Date()) ? 1 : 0; // this is a here until time-zones are implemented properly

  private BloombergSecurityUtils() {
  }

  public static EquityFutureSecurity makeEquityFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 17, 20 + s_dst, 15),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    EquityFutureSecurity sec = new EquityFutureSecurity(expiry, "XCME", "XCME", USD, 250, 
        ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 17, 20 + s_dst, 15), TimeZone.UTC), SecurityUtils.bloombergTickerSecurityId("SPX Index"));
    sec.setUnderlyingId(SecurityUtils.bloombergTickerSecurityId("SPX Index"));
    sec.setName("S&P 500 FUTURE Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX6835907-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("SPM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("SPM10 Index"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    sec.setUniqueId(BloombergSecuritySource.createUniqueId("IX6835907-0"));
    return sec;
  }

  public static AgricultureFutureSecurity makeAgricultureFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 23, 20 + s_dst, 30),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    AgricultureFutureSecurity sec = new AgricultureFutureSecurity(expiry, "XMTB", "XMTB", USD, 100, "Wheat");
    sec.setUnitNumber(100.0);
    sec.setUnitName("tonnes");
    sec.setName("WHEAT FUT (ING) Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX8114863-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("VKM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("VKM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    sec.setUniqueId(BloombergSecuritySource.createUniqueId("IX8114863-0"));
    return sec;
  }

  public static FXFutureSecurity makeAUDUSDCurrencyFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
    FXFutureSecurity security = new FXFutureSecurity(expiry, "XCME", "XCME", USD, 1000, AUD, USD);
    return security;
  }

  public static BondFutureSecurity makeEuroBundFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 8, 20 + s_dst, 0),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>();
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("COEH8262261")), 0.828936d));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("COEI0354262")), 0.80371d));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("COEI2292098")), 0.777869d));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("COEH6142705")), 0.852328d));
    
    BondFutureSecurity sec = new BondFutureSecurity(expiry, "XEUR", "XEUR", EUR, 1000, basket, "Bond",
                                                    ZonedDateTime.of(2010, 6, 10, 0, 0, 0, 0, TimeZone.UTC), 
                                                    ZonedDateTime.of(2010, 6, 10, 0, 0, 0, 0, TimeZone.UTC));
    sec.setName("EURO-BUND FUTURE Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX9439039-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("RXM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("RXM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    return sec;
  }
  
  public static BondFutureSecurity makeUSBondFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 21, 19 + s_dst, 0),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>();
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810EV6")), 1.0858));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FB9")), 1.0132));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810PX0")), 0.7984));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FG8")), 0.9169));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QD3")), 0.7771));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FF0")), 0.9174));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810PW2")), 0.7825));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FE3")), 0.9454));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QH4")), 0.7757));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810PU6")), 0.8675));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810EX2")), 1.0765));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FT0")), 0.8054));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FJ2")), 1.0141));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810PT9")), 0.8352));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QE1")), 0.8109));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FP8")), 0.9268));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QA9")), 0.6606));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FM5")), 1.0286));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810EY0")), 1.0513));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QB7")), 0.7616));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810QC5")), 0.795));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810EZ7")), 1.0649));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810EW4")), 1.0));
    basket.add(new BondFutureDeliverable(ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("GV912810FA1")), 1.0396));
    
    BondFutureSecurity sec = new BondFutureSecurity(expiry, "XCBT", "XCBT", USD, 1000, basket, "Bond",
                                                    ZonedDateTime.of(2010, 6, 01, 0, 0, 0, 0, TimeZone.UTC), 
                                                    ZonedDateTime.of(2010, 6, 01, 0, 0, 0, 0, TimeZone.UTC));
    sec.setName("US LONG BOND(CBT) Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX8530684-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("USM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("USM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    return sec;
  }

  public static MetalFutureSecurity makeSilverFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 28, 17 + s_dst, 25),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    MetalFutureSecurity sec = new MetalFutureSecurity(expiry, "XCEC", "XCEC", USD, 5000, "Precious Metal");
    sec.setUnitNumber(5000.00);
    sec.setUnitName("troy oz.");
    sec.setName("SILVER FUTURE Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX10217289-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("SIM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("SIM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    sec.setUniqueId(BloombergSecuritySource.createUniqueId("IX10217289-0"));
    return sec;
  }

  public static EnergyFutureSecurity makeEthanolFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 3, 18 + s_dst, 15),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    EnergyFutureSecurity sec = new EnergyFutureSecurity(expiry, "XCBT", "XCBT", USD, 29000, "Refined Products");
    sec.setUnitNumber(29000.00);
    sec.setUnitName("U.S. Gallons");
    sec.setName("DENATURED ETHANOL Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX6054783-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("DLM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("DLM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    sec.setUniqueId(BloombergSecuritySource.createUniqueId("IX6054783-0"));
    return sec;
  }

  public static InterestRateFutureSecurity makeInterestRateFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDateTime.of(2010, MonthOfYear.JUNE, 14, 19 + s_dst, 0),
        TimeZone.UTC), ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR);
    InterestRateFutureSecurity sec = new InterestRateFutureSecurity(expiry, "XCME", "XCME", USD, 2500.0, ExternalId.of(SecurityUtils.BLOOMBERG_TICKER, "US0003M Index"));
    sec.setName("90DAY EURO$ FUTR Jun10");
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX166549-0"));
    identifiers.add(SecurityUtils.cusipSecurityId("EDM10"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("EDM10 Comdty"));
    sec.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    sec.setUniqueId(BloombergSecuritySource.createUniqueId("IX166549-0"));
    return sec;
  }

  public static EquitySecurity makeExpectedATTEquitySecurity() {
    EquitySecurity equitySecurity = new EquitySecurity("NEW YORK STOCK EXCHANGE, INC.", "XNYS", "AT&T INC", USD);
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId(ATT_EQUITY_TICKER));

    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId(ATT_BUID));
    equitySecurity.addExternalId(SecurityUtils.cusipSecurityId("00206R102"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("US00206R1023"));
    equitySecurity.addExternalId(SecurityUtils.sedol1SecurityId("2831811"));

    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId(ATT_BUID));
    equitySecurity.setShortName("T");
    equitySecurity.setName("AT&T INC");
    equitySecurity.setGicsCode(GICSCode.of("50101020"));
    addSecurityAttribute(equitySecurity, "preferred", "false");
    return equitySecurity;
  }

  private static void addSecurityAttribute(final Security security, final String attrName, final String attrValue) {
    security.addAttribute(attrName, attrValue);
  }

  //note this will roll over on 2010-12-18 and the expected Buid and Expiry
  // date will change
  public static EquityIndexOptionSecurity makeSPXIndexOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 1100.0;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2010, 12, 18));
    ExternalId underlyingUniqueID = SecurityUtils.bloombergBuidSecurityId("EI09SPX");
    
    final EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType, strike, USD, underlyingUniqueID, new EuropeanExerciseType(), expiry, 5.0, "US");

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX5801809-0-8980"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId("IX5801809-0-8980"));
    security.setName("SPX 2010-12-18 C 1100.0");
    return security;
  }

  public static EquityOptionSecurity makeAPVLEquityOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 190.0;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2010, 01, 16));
    ExternalId underlyingIdentifier = SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER);
    final EquityOptionSecurity security = new EquityOptionSecurity(optionType, strike, USD, underlyingIdentifier, new AmericanExerciseType(), expiry, 5.0, "US");

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("EO1016952010010397C00001"));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId("EO1016952010010397C00001"));
    security.setName("APV 2010-01-16 C 190.0");

    return security;
  }

  public static EquitySecurity makeExpectedAAPLEquitySecurity() {
    EquitySecurity equitySecurity = new EquitySecurity("NASDAQ/NGS (GLOBAL SELECT MARKET)", "XNGS", "APPLE INC", USD);
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId(AAPL_BUID));
    equitySecurity.addExternalId(SecurityUtils.cusipSecurityId("037833100"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("US0378331005"));
    equitySecurity.addExternalId(SecurityUtils.sedol1SecurityId("2046251"));
    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId(AAPL_BUID));
    equitySecurity.setShortName("AAPL");
    equitySecurity.setName("APPLE INC");
    equitySecurity.setGicsCode(GICSCode.of("45202010"));
    addSecurityAttribute(equitySecurity, "preferred", "false");
    return equitySecurity;
  }

  public static EquitySecurity makeExchangeTradedFund() {
    EquitySecurity equitySecurity = new EquitySecurity("NYSE ARCA", "ARCX", "US NATURAL GAS FUND LP", USD);
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId("UNG US Equity"));
    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId("EQ0000000003443730"));
    equitySecurity.addExternalId(SecurityUtils.cusipSecurityId("912318102"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("US9123181029"));
    equitySecurity.addExternalId(SecurityUtils.sedol1SecurityId("B1W5XX3"));
    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId("EQ0000000003443730"));
    equitySecurity.setShortName("UNG");
    equitySecurity.setName("US NATURAL GAS FUND LP");
    addSecurityAttribute(equitySecurity, "preferred", "false");
    return equitySecurity;
  }

  public static Security makeAmericanGeneralEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "AMERICAN GENERAL CORP", GBP);
    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId("EQ0010006200001001"));
    equitySecurity.setName("AMERICAN GENERAL CORP");
    equitySecurity.setShortName("EQ0010006200001001");
    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId("EQ0010006200001001"));
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId("575258Q LN Equity"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("US0263511067"));
    equitySecurity.addExternalId(SecurityUtils.cusipSecurityId("026351106"));
    return equitySecurity;
  }

  public static Security makeTHYSSENKRUPPEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "THYSSENKRUPP AEROSPACE UK LT",
        GBP);
    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId("EQ0011110200001000"));
    equitySecurity.setName("THYSSENKRUPP AEROSPACE UK LT");
    equitySecurity.setShortName("EQ0011110200001000");
    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId("EQ0011110200001000"));
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId("2931300Q LN Equity"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("GB0000458744"));
    equitySecurity.addExternalId(SecurityUtils.sedol1SecurityId("0045874"));
    return equitySecurity;
  }

  public static Security makePTSEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "PTS GROUP PLC", GBP);
    equitySecurity.setUniqueId(BloombergSecuritySource.createUniqueId("EQ0015697400001000"));
    equitySecurity.setName("PTS GROUP PLC");
    equitySecurity.setShortName("EQ0015697400001000");
    equitySecurity.addExternalId(SecurityUtils.bloombergBuidSecurityId("EQ0015697400001000"));
    equitySecurity.addExternalId(SecurityUtils.bloombergTickerSecurityId("365092Q LN Equity"));
    equitySecurity.addExternalId(SecurityUtils.isinSecurityId("GB0006661457"));
    equitySecurity.addExternalId(SecurityUtils.sedol1SecurityId("0666145"));
    return equitySecurity;
  }

  public static ExternalIdBundle makeBloombergTickerIdentifier(String secDes) {
    return ExternalIdBundle.of(SecurityUtils.bloombergTickerSecurityId(secDes));
  }

  public static ExternalIdBundle makeBloombergBuid(String secDes) {
    return ExternalIdBundle.of(SecurityUtils.bloombergBuidSecurityId(secDes));
  }
  
  public static IRFutureOptionSecurity makeEURODOLLARFutureOptionSecurity() {
    OptionType optionType = OptionType.PUT;
    double strike = 99.5;
    double pointValue = 6.25;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2012, 12, 17));
    ExternalId underlyingID = SecurityUtils.bloombergTickerSecurityId("EDZ2 Comdty");
    final String exchange = "CME";
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, new AmericanExerciseType(), underlyingID, pointValue, false, USD, strike, optionType);

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX11675985-0-8C70"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("EDZ2C 99.500 Comdty"));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId("IX11675985-0-8C70"));
    security.setName("EDZ2C 2012-12-17 P 99.5");
    return security;
  }
  
  public static IRFutureOptionSecurity makeLIBORFutureOptionSecurity() {
    OptionType optionType = OptionType.PUT;
    double strike = 91.0;
    double pointValue = 6.25;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2011, 9, 21));
    ExternalId underlyingID = SecurityUtils.bloombergTickerSecurityId("L U1 Comdty");
    final String exchange = "LIF";
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, new AmericanExerciseType(), underlyingID, pointValue, true, GBP, strike, optionType);

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX9494155-0-8B60"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("L U1C 91.000 Comdty"));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId("IX9494155-0-8B60"));
    security.setName("L U1C 2011-09-21 P 91.0");
    return security;
  }
  
  public static IRFutureOptionSecurity makeEURIBORFutureOptionSecurity() {
    OptionType optionType = OptionType.PUT;
    double strike = 92.875;
    double pointValue = 12.5;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2011, 9, 19));
    ExternalId underlyingID = SecurityUtils.bloombergTickerSecurityId("FPU1 Comdty");
    final String exchange = "EUX";
    final IRFutureOptionSecurity security = new IRFutureOptionSecurity(exchange, expiry, new AmericanExerciseType(), underlyingID, pointValue, true, EUR, strike, optionType);

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX10090132-0-8B9C"));
    identifiers.add(SecurityUtils.bloombergTickerSecurityId("FPU1C 92.875 Comdty"));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecuritySource.createUniqueId("IX10090132-0-8B9C"));
    security.setName("FPU1C 2011-09-19 P 92.875");
    return security;
  }
}
