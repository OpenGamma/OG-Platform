/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.AAPL_EQUITY_TICKER;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.APV_EQUITY_OPTION_TICKER;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.SPX_INDEX_OPTION_TICKER;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.USD;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeAUDUSDCurrencyFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeBloombergTickerIdentifier;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeEthanolFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeEuroBondFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeIndexFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeInterestRateFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeSilverFuture;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeWheatFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZoneOffset;
import javax.time.calendar.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.WritableSecurityMaster;
import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.FrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.AgricultureFutureSecurity;
import com.opengamma.financial.security.BondFutureSecurity;
import com.opengamma.financial.security.BondSecurity;
import com.opengamma.financial.security.EnergyFutureSecurity;
import com.opengamma.financial.security.EquitySecurity;
import com.opengamma.financial.security.FXFutureSecurity;
import com.opengamma.financial.security.GovernmentBondSecurity;
import com.opengamma.financial.security.IndexFutureSecurity;
import com.opengamma.financial.security.InterestRateFutureSecurity;
import com.opengamma.financial.security.MetalFutureSecurity;
import com.opengamma.financial.security.option.AmericanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanVanillaEquityOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * Generic TestCase for  WritableSecurityMaster
 *
 * 
 */
public abstract class WritableSecurityMasterTestCase extends HibernateTest {
  
  private static final String[] TEST_CURRENCIES = {"USD", "GBP", "YEN", "CHF"};
  private static final int TEST_FILLER_SIZE = 5;
  private Random _random = new Random();
  private static final Clock s_clock = Clock.system(TimeZone.UTC);

  /**
   * @param databaseType
   * @param databaseVersion
   */
  public WritableSecurityMasterTestCase(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  private WritableSecurityMaster _secMaster = null;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    _secMaster = createSecurityMaster();
  }

  protected abstract WritableSecurityMaster createSecurityMaster();

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _secMaster = null;
  }
  
  @Test
  public void aaplEquityByBbgTicker() throws Exception {
    addRandomEquities();
    EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    IdentifierBundle equityKey = makeBloombergTickerIdentifier(AAPL_EQUITY_TICKER);
    Security security = _secMaster.getSecurity(equityKey);
    assertEquitySecurity(aaplEquitySecurity, security);
  }
  
  @Test
  public void aaplEquityByUniqueIdentifier() throws Exception {
    addRandomEquities();
    EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    UniqueIdentifier uniqueIdentifier = _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    Security sec = _secMaster.getSecurity(uniqueIdentifier);
    assertEquitySecurity(aaplEquitySecurity, sec);
  }
  
  @Test
  public void aaplEquitiesByBbgTicker() throws Exception {
    addRandomEquities();
    EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    IdentifierBundle equityKey = makeBloombergTickerIdentifier(AAPL_EQUITY_TICKER);
    Collection<Security> securities = _secMaster.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEquitySecurity(aaplEquitySecurity, sec);
  }
  
  @Test
  public void apvEquityOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity();
    _secMaster.putSecurity(new Date(), expectedOption);
    IdentifierBundle equityOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, APV_EQUITY_OPTION_TICKER));
    Security sec = _secMaster.getSecurity(equityOptionKey);
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }
  
  @Test
  public void spxIndexOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    IdentifierBundle indexOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    Security sec = _secMaster.getSecurity(indexOptionKey);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  public void spxIndexOptionByBbgUnique() throws Exception {
    addRandomEquityOptions();
    EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    UniqueIdentifier uniqueIdentifier = _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    Security sec = _secMaster.getSecurity(uniqueIdentifier);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  // note that this code will roll over in 18-12-2010, the test values need to change
  public void spxIndexOptionsByBbgTicker() throws Exception {
    addRandomEquityOptions();
    EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    IdentifierBundle indexOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    Collection<Security> securities = _secMaster.getSecurities(indexOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }
  
  @Test
  public void agricultureFuture() throws Exception {
    addRandomAgricultureFutures();
    AgricultureFutureSecurity wheat = makeWheatFuture();
    _secMaster.putSecurity(new Date(), wheat);
    IdentifierBundle identifiers = wheat.getIdentifiers();
    Security security = _secMaster.getSecurity(identifiers);
    assertNotNull(security);
    assertTrue(security instanceof AgricultureFutureSecurity);
    assertEquals(wheat, security);
  }
  
  

  @Test
  public void indexFuture() throws Exception {
    IndexFutureSecurity indexFutureSecurity = makeIndexFuture();
    _secMaster.putSecurity(new Date(), indexFutureSecurity);
    IdentifierBundle id = new IdentifierBundle (new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "SPM0 Index"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof IndexFutureSecurity);
    assertEquals(indexFutureSecurity, security);
  }
  
  @Test
  public void governmentBondSecurityBean() {
    final Date now = new Date();
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Currency dollar = Currency.getInstance("USD");
    final YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final Frequency annual = FrequencyFactory.INSTANCE.getFrequency("annual");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("following");
    final LocalDate announcementDate = LocalDate.of(2008, 1, 1);
    final LocalDate interestAccrualDate = LocalDate.of(2010, 3, 4);
    final LocalDate settlementDate = LocalDate.of(2012, 11, 1);
    final LocalDate firstCouponDate = LocalDate.of(2009, 1, 1);
    final Identifier governmentId = new Identifier("BLOOMBERG", "government bond");
    final BondSecurity bond = new GovernmentBondSecurity("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5,
        annual, act360, following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
    bond.setIdentifiers(new IdentifierBundle(governmentId));
    final UniqueIdentifier bondUID = _secMaster.putSecurity(now, bond);
    final Security security = _secMaster.getSecurity(bondUID);
    assertNotNull(security);
    assertTrue(security instanceof GovernmentBondSecurity);
    final GovernmentBondSecurity government = (GovernmentBondSecurity) security;
    assertEquals("issuer name", government.getIssuerName());
    assertEquals("issuer type", government.getIssuerType());
    assertEquals("issuer domicile", government.getIssuerDomicile());
    assertEquals("market", government.getMarket());
    assertEquals(dollar, government.getCurrency());
    assertEquals(usStreet, government.getYieldConvention());
    assertEquals("guarantee type", government.getGuaranteeType());
    assertEquals(expiry, government.getMaturity());
    assertEquals("coupon type", government.getCouponType());
    assertEquals(0.5, government.getCouponRate(), 0);
    assertEquals(annual, government.getCouponFrequency());
    assertEquals(act360, government.getDayCountConvention());
    assertEquals(following, government.getBusinessDayConvention());
    assertEquals(announcementDate, government.getAnnouncementDate());
    assertEquals(interestAccrualDate, government.getInterestAccrualDate());
    assertEquals(settlementDate, government.getSettlementDate());
    assertEquals(firstCouponDate, government.getFirstCouponDate());
    assertEquals(10.0, government.getIssuancePrice(), 0);
    assertEquals(100.0, government.getTotalAmountIssued(), 0);
    assertEquals(10.0, government.getMinimumAmount(), 0);
    assertEquals(1.0, government.getMinimumIncrement(), 0);
    assertEquals(10.0, government.getParAmount(), 0);
    assertEquals(15.0, government.getRedemptionValue(), 0);
  }
  
  @Test
  public void testGovernmentBondSecurityBean() {
    final Date now = new Date();
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC));
    final Currency dollar = Currency.getInstance("USD");
    final YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final Frequency annual = FrequencyFactory.INSTANCE.getFrequency("annual");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("following");
    final LocalDate announcementDate = LocalDate.of(2008, 1, 1);
    final LocalDate interestAccrualDate = LocalDate.of(2010, 3, 4);
    final LocalDate settlementDate = LocalDate.of(2012, 11, 1);
    final LocalDate firstCouponDate = LocalDate.of(2009, 1, 1);
    final Identifier governmentId = new Identifier("BLOOMBERG", "government bond");
    final BondSecurity bond = new GovernmentBondSecurity("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5,
        annual, act360, following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
    bond.setIdentifiers(new IdentifierBundle(governmentId));
    final UniqueIdentifier bondUID = _secMaster.putSecurity(now, bond);
    final Security security = _secMaster.getSecurity(bondUID);
    assertNotNull(security);
    assertTrue(security instanceof GovernmentBondSecurity);
    final GovernmentBondSecurity government = (GovernmentBondSecurity) security;
    assertEquals("issuer name", government.getIssuerName());
    assertEquals("issuer type", government.getIssuerType());
    assertEquals("issuer domicile", government.getIssuerDomicile());
    assertEquals("market", government.getMarket());
    assertEquals(dollar, government.getCurrency());
    assertEquals(usStreet, government.getYieldConvention());
    assertEquals("guarantee type", government.getGuaranteeType());
    assertEquals(expiry, government.getMaturity());
    assertEquals("coupon type", government.getCouponType());
    assertEquals(0.5, government.getCouponRate(), 0);
    assertEquals(annual, government.getCouponFrequency());
    assertEquals(act360, government.getDayCountConvention());
    assertEquals(following, government.getBusinessDayConvention());
    assertEquals(announcementDate, government.getAnnouncementDate());
    assertEquals(interestAccrualDate, government.getInterestAccrualDate());
    assertEquals(settlementDate, government.getSettlementDate());
    assertEquals(firstCouponDate, government.getFirstCouponDate());
    assertEquals(10.0, government.getIssuancePrice(), 0);
    assertEquals(100.0, government.getTotalAmountIssued(), 0);
    assertEquals(10.0, government.getMinimumAmount(), 0);
    assertEquals(1.0, government.getMinimumIncrement(), 0);
    assertEquals(10.0, government.getParAmount(), 0);
    assertEquals(15.0, government.getRedemptionValue(), 0);
  }
  
  @Test
  @Ignore
  public void currencyFuture() throws Exception {
    FXFutureSecurity currencyFuture = makeAUDUSDCurrencyFuture();
    _secMaster.putSecurity(new Date(), currencyFuture);
    IdentifierBundle  id = new IdentifierBundle (new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "LNM0 Curncy"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof FXFutureSecurity);
    assertEquals(currencyFuture, security);
  }
  
  @Test
  public void euroBondFuture() throws Exception {
    BondFutureSecurity euroBondFuture = makeEuroBondFuture();
    _secMaster.putSecurity(new Date(), euroBondFuture);
    IdentifierBundle  id = new IdentifierBundle (new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "RXU0 Comdty"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof BondFutureSecurity);
    assertEquals(euroBondFuture, security);
  }
  
  @Test
  public void metalFuture() throws Exception {
    MetalFutureSecurity silverFuture = makeSilverFuture();
    _secMaster.putSecurity(new Date(), silverFuture);
    IdentifierBundle  id = new IdentifierBundle (new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "SIM0 Comdty"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof MetalFutureSecurity);
    assertEquals(silverFuture, security);
  }
  
  @Test
  public void energyFuture() throws Exception {
    EnergyFutureSecurity ethanolFuture = makeEthanolFuture();
    _secMaster.putSecurity(new Date(), ethanolFuture);
    IdentifierBundle  id = new IdentifierBundle (new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "DLM0 Comdty"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof EnergyFutureSecurity);
    assertEquals(ethanolFuture, security);
  }
  
  @Test
  public void interestRateFuture() throws Exception {
    InterestRateFutureSecurity euroDollarFuture = makeInterestRateFuture();
    _secMaster.putSecurity(new Date(), euroDollarFuture);
    IdentifierBundle  id = new IdentifierBundle (new Identifier (IdentificationScheme.BLOOMBERG_TICKER, "EDM0 Comdty"));
    Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof InterestRateFutureSecurity);
    assertEquals(euroDollarFuture, security);
  }
  
  @Test
  public void update() {
    Date date1 = new Date(System.currentTimeMillis() - 2000L);
    Date date2 = new Date(date1.getTime() + 2000L);
    
    final EquitySecurity sec = new EquitySecurity();
    sec.setCompanyName("General Motors");
    sec.setName("General Motors");
    sec.setCurrency(USD);
    sec.setExchangeCode("NYSE");
    sec.setExchange("NEW YORK STOCK EXCHANGE");
    sec.setTicker("GM US Equity");
    sec.setGICSCode(GICSCode.getInstance(25102010));
    sec.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    UniqueIdentifier uid1 = _secMaster.putSecurity(date1, sec);
    
    sec.setCompanyName("Big Motors");
    UniqueIdentifier uid2 = _secMaster.putSecurity(date2, sec);
    
    assertEquals(uid1.toLatest(), uid2.toLatest());
    
    EquitySecurity loaded1 = (EquitySecurity) _secMaster.getSecurity(uid1);
    assertEquals("General Motors", loaded1.getCompanyName());
    
    EquitySecurity loaded2 = (EquitySecurity) _secMaster.getSecurity(uid2);
    assertEquals("Big Motors", loaded2.getCompanyName());
  }
  
  /**
   * 
   */
  private void addRandomEquityOptions() {
    //add some americanvanilla
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      OptionType optionType = OptionType.CALL;
      double strike = _random.nextDouble() * 100;
      Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 01, 16));
      Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt()));
      int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      AmericanVanillaEquityOptionSecurity security = new AmericanVanillaEquityOptionSecurity(
          optionType, 
          strike, 
          expiry, 
          underlyingUniqueID, 
          Currency.getInstance(TEST_CURRENCIES[currIndex]), 
          1, //TODO change when point value is properly added
          "EXCHN" + String.valueOf(_random.nextInt()));

      Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      security.setIdentifiers(new IdentifierBundle(identifiers));
      security.setName("NAME" + String.valueOf(_random.nextInt()));
      _secMaster.putSecurity(new Date(), security);
    }
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      OptionType optionType = OptionType.PUT;
      double strike = _random.nextDouble() * 100;
      Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 12, 18));
      Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt()));
      int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      EuropeanVanillaEquityOptionSecurity security = new EuropeanVanillaEquityOptionSecurity(
          optionType, strike, expiry, underlyingUniqueID, Currency.getInstance(TEST_CURRENCIES[currIndex]),
          1, //TODO change when the point value is properly added
          "EXCHN" + String.valueOf(_random.nextInt()));

      Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      security.setIdentifiers(new IdentifierBundle(identifiers));
      security.setName("NAME" + String.valueOf(_random.nextInt()));
      _secMaster.putSecurity(new Date(), security);
    }
  }
  
  private void addRandomAgricultureFutures() {
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      int year = (int)(100 * _random.nextDouble() + 1900);  
      int month = 1 + _random.nextInt(11);
      int day = 1 + _random.nextInt(27);
      Expiry expiry = new Expiry(s_clock.zonedDateTime().withDate(year, month, day).withTime(17, 00));
      int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      Currency currency = Currency.getInstance(TEST_CURRENCIES[currIndex]);
      String exchange = "EXCHN" + String.valueOf(_random.nextInt());
      String category = "CAT" + String.valueOf(_random.nextInt());
      AgricultureFutureSecurity sec = new AgricultureFutureSecurity(expiry, exchange, exchange, currency, category, 100.0, "tonnes");
      sec.setName("NAME" + String.valueOf(_random.nextInt()));
      Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      identifiers.add(new Identifier(IdentificationScheme.CUSIP, "CUSIP" + String.valueOf(_random.nextInt())));
      identifiers.add(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      sec.setIdentifiers(new IdentifierBundle(identifiers));
      _secMaster.putSecurity(new Date(), sec);
    }
  }

  private void addRandomEquities() {
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      EquitySecurity equitySecurity = new EquitySecurity();
      equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(new Identifier(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(new Identifier(IdentificationScheme.CUSIP, "CUSIP" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(new Identifier(IdentificationScheme.ISIN, "ISIN" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(new Identifier(IdentificationScheme.SEDOL1, "SEDOL1" + String.valueOf(_random.nextInt())));
      equitySecurity.setCompanyName("CNAME" + String.valueOf(_random.nextInt()));
      equitySecurity.setTicker("TICKER" + String.valueOf(_random.nextInt()));
      equitySecurity.setExchange("EXCHN" + String.valueOf(_random.nextInt()));
      equitySecurity.setExchangeCode("EXCHC" + String.valueOf(_random.nextInt()));
      int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      equitySecurity.setCurrency(Currency.getInstance(TEST_CURRENCIES[currIndex]));
      equitySecurity.setName("NAME" + String.valueOf(_random.nextInt()));
      equitySecurity.setGICSCode(GICSCode.getInstance(String.valueOf(_random.nextInt(98) + 2)));
      _secMaster.putSecurity(new Date(), equitySecurity);
    }
  }
  
  private static void assertEquitySecurity(EquitySecurity expectedEquity, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    EquitySecurity actualEquity = (EquitySecurity) sec;
    assertEquals(expectedEquity.getSecurityType(), actualEquity.getSecurityType());
    assertEquals(expectedEquity.getIdentifiers(), actualEquity.getIdentifiers());
    assertEquals(expectedEquity.getUniqueIdentifier(), actualEquity.getUniqueIdentifier());
    assertEquals(expectedEquity.getExchange(), actualEquity.getExchange());
    assertEquals(expectedEquity.getCompanyName(), actualEquity.getCompanyName());
    assertEquals(expectedEquity.getCurrency(), actualEquity.getCurrency());
    // we dont store the ticker, so set before checking the lot
    actualEquity.setTicker(expectedEquity.getTicker());
    assertEquals(expectedEquity, sec);
  }
  
  /**
   * @param expectedOption
   * @param sec
   */
  private static void assertAmericanVanillaEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof AmericanVanillaEquityOptionSecurity);
    assertEquityOptionSecurity(expectedOption, sec);
  }
  
  private static void assertEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
    assertEquals(expectedOption.getIdentifiers(), actualOption.getIdentifiers());
    assertEquals(expectedOption.getUniqueIdentifier(), actualOption.getUniqueIdentifier());
    assertEquals(expectedOption.getSecurityType(), actualOption.getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingIdentifier(), actualOption.getUnderlyingIdentifier());
    assertEquals(expectedOption.getName(), actualOption.getName());
    // check the lot
    assertEquals(expectedOption, sec);
  }
  
  private static void assertEuropeanVanillaEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EuropeanVanillaEquityOptionSecurity);
    assertEquityOptionSecurity(expectedOption, sec);
  }

}
