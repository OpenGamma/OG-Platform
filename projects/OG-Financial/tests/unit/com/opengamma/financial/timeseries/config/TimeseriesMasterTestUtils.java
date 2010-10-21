/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.security.equity.EquitySecurity;
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
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * 
 */
public final class TimeseriesMasterTestUtils {

  public static final Currency USD = Currency.getInstance("USD");
  public static final Currency AUD = Currency.getInstance("AUD");
  public static final Currency EUR = Currency.getInstance("EUR");
  public static final Currency GBP = Currency.getInstance("GBP");
  
  public static final String ATT_BUID = "EQ0010137600001000";
  public static final String AAPL_BUID = "EQ0010169500001000";
  public static final String APV_EQUITY_OPTION_TICKER = "APV US 01/16/10 C190 Equity";
  // At times Bloomberg has changed this to SPT as the prefix in the past.
  public static final String SPX_INDEX_OPTION_TICKER = "SPX US 12/18/10 C1100 Index";
  public static final String AAPL_EQUITY_TICKER = "AAPL US Equity";
  public static final String ATT_EQUITY_TICKER = "T US Equity";

 
  private TimeseriesMasterTestUtils() {
  }
  
  public static IndexFutureSecurity makeIndexFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 17).atMidnight(), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
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
  
  public static AgricultureFutureSecurity makeAgricultureFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 23).atMidnight(), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
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
  
  public static FXFutureSecurity makeAUDUSDCurrencyFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    FXFutureSecurity security = new FXFutureSecurity(expiry, "XCME", "XCME", USD, AUD, USD);
    return security;
  }
  
  public static BondFutureSecurity makeEuroBundFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 1).atMidnight(), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    Set<BondFutureDeliverable> basket = new HashSet<BondFutureDeliverable>();
    basket.add(new BondFutureDeliverable(new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3.5 07/04/19 Corp")), 0.828936d));
    basket.add(new BondFutureDeliverable(new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3.75 01/04/19 Corp")), 0.852328d));
    basket.add(new BondFutureDeliverable(new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "DBR 3.25 01/04/20 Corp")), 0.80371d));
    
    BondFutureSecurity sec = new BondFutureSecurity(expiry, "XEUR", "XEUR", EUR, basket, "Bond");
    sec.setName("EURO-BUND FUTURE Jun10");
    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX9439039-0"));
    identifiers.add(new Identifier(IdentificationScheme.CUSIP, "RXM0"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "RXM0 Comdty"));
    sec.setIdentifiers(new IdentifierBundle(identifiers));
    return sec;
  }
  

  public static MetalFutureSecurity makeSilverFuture() {
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 28).atMidnight(), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
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
    Expiry expiry = new Expiry(ZonedDateTime.of(LocalDate.of(2010, MonthOfYear.JUNE, 3).atMidnight(), TimeZone.UTC),
        ExpiryAccuracy.DAY_MONTH_YEAR);
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
  
  public static EquitySecurity makeExpectedATTEquitySecurity() {
    EquitySecurity equitySecurity = new EquitySecurity("NEW YORK STOCK EXCHANGE INC.", "XNYS", "AT&T INC", USD);
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, ATT_EQUITY_TICKER));
    
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, ATT_BUID));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "00206R102"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US00206R1023"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "2831811"));
    
    equitySecurity.setShortName("T");
    equitySecurity.setName("AT&T INC");
    equitySecurity.setGicsCode(GICSCode.getInstance(50101020));
    return equitySecurity;
  }
  
  //note this will roll over on 2010-12-18 and the expected Buid and Expiry
  // date will change
  public static EquityOptionSecurity makeSPXIndexOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 1100.0;
    Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 12, 18));
    Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "EI09SPX");
    final EquityOptionSecurity security = new EquityOptionSecurity(new EuropeanExerciseType(),
        new VanillaPayoffStyle(), optionType, strike, expiry, underlyingUniqueID, USD,
        5.0, 
        "US");

    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "IX5801809-0-8980"));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    security.setName("SPX 2010-12-18 C 1100.0");
    return security;
  }
  
  public static EquityOptionSecurity makeAPVLEquityOptionSecurity() {
    OptionType optionType = OptionType.CALL;
    double strike = 190.0;
    Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 01, 16));
    Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, AAPL_BUID);
    final EquityOptionSecurity security = new EquityOptionSecurity(new AmericanExerciseType(),
        new VanillaPayoffStyle(), optionType,
        strike, 
        expiry, 
        underlyingUniqueID, 
        USD, 
        5.0, 
        "US");

    Set<Identifier> identifiers = new HashSet<Identifier>();
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, APV_EQUITY_OPTION_TICKER));
    identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EO1016952010010397C00001"));
    security.setIdentifiers(new IdentifierBundle(identifiers));
    security.setName("APV 2010-01-16 C 190.0");

    return security;
  }
  
  public static EquitySecurity makeExpectedAAPLEquitySecurity() {
    EquitySecurity equitySecurity = new EquitySecurity("NASDAQ/NGS (GLOBAL SELECT MARKET)", "XNGS", "APPLE INC", USD);
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, AAPL_EQUITY_TICKER));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, AAPL_BUID));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "037833100"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US0378331005"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "2046251"));
    equitySecurity.setShortName("AAPL");
    equitySecurity.setName("APPLE INC");
    equitySecurity.setGicsCode(GICSCode.getInstance(45202010));
    return equitySecurity;
  }
  
  public static EquitySecurity makeExchangeTradedFund() {
    EquitySecurity equitySecurity = new EquitySecurity("NYSE ARCA", "ARCX", "US NATURAL GAS FUND LP", USD);
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "UNG US Equity"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EQ0000000003443730"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "912318102"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US9123181029"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "B1W5XX3"));
    equitySecurity.setShortName("UNG");
    equitySecurity.setName("US NATURAL GAS FUND LP");
    return equitySecurity;
  }
  
  public static Security makeAmericanGeneralEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "AMERICAN GENERAL CORP", GBP);
    equitySecurity.setName("AMERICAN GENERAL CORP");
    equitySecurity.setShortName("EQ0010006200001001");
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EQ0010006200001001"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "575258Q LN Equity"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "US0263511067"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "026351106"));
    return equitySecurity;
  }
  
  public static Security makeTHYSSENKRUPPEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "THYSSENKRUPP AEROSPACE UK LT",
        GBP);
    equitySecurity.setName("THYSSENKRUPP AEROSPACE UK LT");
    equitySecurity.setShortName("EQ0011110200001000");
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EQ0011110200001000"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "2931300Q LN Equity"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "GB0000458744"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "0045874"));
    return equitySecurity;
  }

  public static Security makePTSEquity() {
    EquitySecurity equitySecurity = new EquitySecurity("LONDON STOCK EXCHANGE", "XLON", "PTS GROUP PLC", GBP);
    equitySecurity.setName("PTS GROUP PLC");
    equitySecurity.setShortName("EQ0015697400001000");
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EQ0015697400001000"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "365092Q LN Equity"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "GB0006661457"));
    equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "0666145"));
    return equitySecurity;
  }
  
  public static IdentifierBundle makeBloombergTickerIdentifier(String secDes) {
    return new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, secDes));
  }
  
  public static IdentifierBundle makeBloombergUniqueIdentifier(String secDes) {
    return new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_BUID, secDes));
  }
}
