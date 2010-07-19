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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequencyFactory;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
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
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.test.HibernateTest;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Generic TestCase for  WritableSecurityMaster
 *
 * 
 */
public abstract class WritableSecurityMasterTestCase extends HibernateTest {

  private static final String[] TEST_CURRENCIES = {"USD", "GBP", "YEN", "CHF"};
  private static final int TEST_FILLER_SIZE = 5;
  private final Random _random = new Random();
  private static final Clock s_clock = Clock.system(TimeZone.UTC);

  /**
   * @param databaseType
   * @param databaseVersion
   */
  public WritableSecurityMasterTestCase(final String databaseType, final String databaseVersion) {
    super(databaseType, databaseVersion);
  }

  private WritableSecurityMaster _secMaster = null;

  /**
   * @throws java.lang.Exception
   */
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    _secMaster = createSecurityMaster();
  }

  protected abstract WritableSecurityMaster createSecurityMaster();

  /**
   * @throws java.lang.Exception
   */
  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _secMaster = null;
  }

  @Test
  public void aaplEquityByBbgTicker() throws Exception {
    addRandomEquities();
    final EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    final IdentifierBundle equityKey = makeBloombergTickerIdentifier(AAPL_EQUITY_TICKER);
    final Security security = _secMaster.getSecurity(equityKey);
    assertEquitySecurity(aaplEquitySecurity, security);
  }

  @Test
  public void aaplEquityByUniqueIdentifier() throws Exception {
    addRandomEquities();
    final EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    final UniqueIdentifier uniqueIdentifier = _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    final Security sec = _secMaster.getSecurity(uniqueIdentifier);
    assertEquitySecurity(aaplEquitySecurity, sec);
  }

  @Test
  public void aaplEquitiesByBbgTicker() throws Exception {
    addRandomEquities();
    final EquitySecurity aaplEquitySecurity = makeExpectedAAPLEquitySecurity();
    _secMaster.putSecurity(new Date(), aaplEquitySecurity);
    final IdentifierBundle equityKey = makeBloombergTickerIdentifier(AAPL_EQUITY_TICKER);
    final Collection<Security> securities = _secMaster.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    final Security sec = securities.iterator().next();
    assertEquitySecurity(aaplEquitySecurity, sec);
  }

  @Test
  public void apvEquityOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity();
    _secMaster.putSecurity(new Date(), expectedOption);
    final IdentifierBundle equityOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, APV_EQUITY_OPTION_TICKER));
    final Security sec = _secMaster.getSecurity(equityOptionKey);
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test
  public void spxIndexOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    final IdentifierBundle indexOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    final Security sec = _secMaster.getSecurity(indexOptionKey);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  public void spxIndexOptionByBbgUnique() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    final UniqueIdentifier uniqueIdentifier = _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    final Security sec = _secMaster.getSecurity(uniqueIdentifier);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  // note that this code will roll over in 18-12-2010, the test values need to change
  public void spxIndexOptionsByBbgTicker() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity spxIndexOptionSecurity = makeSPXIndexOptionSecurity();
    _secMaster.putSecurity(new Date(), spxIndexOptionSecurity);
    final IdentifierBundle indexOptionKey = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, SPX_INDEX_OPTION_TICKER));
    final Collection<Security> securities = _secMaster.getSecurities(indexOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    final Security sec = securities.iterator().next();
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  public void agricultureFuture() throws Exception {
    addRandomAgricultureFutures();
    final AgricultureFutureSecurity wheat = makeWheatFuture();
    _secMaster.putSecurity(new Date(), wheat);
    final IdentifierBundle identifiers = wheat.getIdentifiers();
    final Security security = _secMaster.getSecurity(identifiers);
    assertNotNull(security);
    assertTrue(security instanceof AgricultureFutureSecurity);
    assertEquals(wheat, security);
  }

  @Test
  public void indexFuture() throws Exception {
    final IndexFutureSecurity indexFutureSecurity = makeIndexFuture();
    _secMaster.putSecurity(new Date(), indexFutureSecurity);
    final IdentifierBundle id = new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "SPM0 Index"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof IndexFutureSecurity);
    assertEquals(indexFutureSecurity, security);
  }

  @Test
  public void governmentBondSecurityBean() {
    final Date now = new Date();
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    final Currency dollar = Currency.getInstance("USD");
    final YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final SimpleFrequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency("annual");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("following");
    final LocalDate announcementDate = LocalDate.of(2008, 1, 1);
    final LocalDate interestAccrualDate = LocalDate.of(2010, 3, 4);
    final LocalDate settlementDate = LocalDate.of(2012, 11, 1);
    final LocalDate firstCouponDate = LocalDate.of(2009, 1, 1);
    final Identifier governmentId = Identifier.of("BLOOMBERG", "government bond");
    final BondSecurity bond = new GovernmentBondSecurity("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5, annual, act360,
        following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
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
    final Expiry expiry = new Expiry(ZonedDateTime.ofInstant(OffsetDateTime.ofMidnight(2012, 10, 30, ZoneOffset.UTC), TimeZone.UTC), ExpiryAccuracy.DAY_MONTH_YEAR);
    final Currency dollar = Currency.getInstance("USD");
    final YieldConvention usStreet = YieldConventionFactory.INSTANCE.getYieldConvention("US street");
    final SimpleFrequency annual = SimpleFrequencyFactory.INSTANCE.getFrequency("annual");
    final DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    final BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("following");
    final LocalDate announcementDate = LocalDate.of(2008, 1, 1);
    final LocalDate interestAccrualDate = LocalDate.of(2010, 3, 4);
    final LocalDate settlementDate = LocalDate.of(2012, 11, 1);
    final LocalDate firstCouponDate = LocalDate.of(2009, 1, 1);
    final Identifier governmentId = Identifier.of("BLOOMBERG", "government bond");
    final BondSecurity bond = new GovernmentBondSecurity("issuer name", "issuer type", "issuer domicile", "market", dollar, usStreet, "guarantee type", expiry, "coupon type", 0.5, annual, act360,
        following, announcementDate, interestAccrualDate, settlementDate, firstCouponDate, 10.0, 100d, 10d, 1d, 10d, 15d);
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
    final FXFutureSecurity currencyFuture = makeAUDUSDCurrencyFuture();
    _secMaster.putSecurity(new Date(), currencyFuture);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "LNM0 Curncy"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof FXFutureSecurity);
    assertEquals(currencyFuture, security);
  }

  private void sortBondFutureDeliverable (final BondFutureSecurity security) {
    List<BondFutureDeliverable> basket = new ArrayList<BondFutureDeliverable>(security.getBasket());
    Collections.sort(basket, new Comparator<BondFutureDeliverable>() {
      @Override
      public int compare(BondFutureDeliverable o1, BondFutureDeliverable o2) {
        return o1.getIdentifiers().compareTo(o2.getIdentifiers());
      }
    });
    security.setBasket(basket);
  }

  @Test
  public void euroBondFuture() throws Exception {
    final BondFutureSecurity euroBondFuture = makeEuroBondFuture();
    _secMaster.putSecurity(new Date(), euroBondFuture);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "RXU0 Comdty"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof BondFutureSecurity);
    sortBondFutureDeliverable(euroBondFuture);
    sortBondFutureDeliverable((BondFutureSecurity) security);
    assertEquals(euroBondFuture, security);
  }

  @Test
  public void metalFuture() throws Exception {
    final MetalFutureSecurity silverFuture = makeSilverFuture();
    _secMaster.putSecurity(new Date(), silverFuture);
    final IdentifierBundle id = new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "SIM0 Comdty"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof MetalFutureSecurity);
    assertEquals(silverFuture, security);
  }

  @Test
  public void energyFuture() throws Exception {
    final EnergyFutureSecurity ethanolFuture = makeEthanolFuture();
    _secMaster.putSecurity(new Date(), ethanolFuture);
    final IdentifierBundle id = new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "DLM0 Comdty"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof EnergyFutureSecurity);
    assertEquals(ethanolFuture, security);
  }

  @Test
  public void interestRateFuture() throws Exception {
    final InterestRateFutureSecurity euroDollarFuture = makeInterestRateFuture();
    _secMaster.putSecurity(new Date(), euroDollarFuture);
    final IdentifierBundle id = new IdentifierBundle(new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "EDM0 Comdty"));
    final Security security = _secMaster.getSecurity(id);
    assertNotNull(security);
    assertTrue(security instanceof InterestRateFutureSecurity);
    assertEquals(euroDollarFuture, security);
  }

  @Test
  public void update() {
    final Date date1 = new Date(System.currentTimeMillis() - 2000L);
    final Date date2 = new Date(date1.getTime() + 2000L);

    final EquitySecurity sec = new EquitySecurity("NEW YORK STOCK EXCHANGE", "NYSE", "General Motors", USD);
    sec.setGicsCode(GICSCode.getInstance(25102010));
    sec.setTicker("GM US Equity");
    sec.setName("General Motors");
    sec.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    final UniqueIdentifier uid1 = _secMaster.putSecurity(date1, sec);

    sec.setCompanyName("Big Motors");
    final UniqueIdentifier uid2 = _secMaster.putSecurity(date2, sec);

    assertEquals(uid1.toLatest(), uid2.toLatest());

    final EquitySecurity loaded1 = (EquitySecurity) _secMaster.getSecurity(uid1);
    assertEquals("General Motors", loaded1.getCompanyName());

    final EquitySecurity loaded2 = (EquitySecurity) _secMaster.getSecurity(uid2);
    assertEquals("Big Motors", loaded2.getCompanyName());
  }

  /**
   * 
   */
  private void addRandomEquityOptions() {
    // add some americanvanilla
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      final OptionType optionType = OptionType.CALL;
      final double strike = _random.nextDouble() * 100;
      final Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 01, 16));
      final Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt()));
      final int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      final EquityOptionSecurity security = new EquityOptionSecurity(new AmericanExerciseType(), new VanillaPayoffStyle(), optionType, strike, expiry, underlyingUniqueID, Currency
          .getInstance(TEST_CURRENCIES[currIndex]), 1, // TODO change when point value is properly added
          "EXCHN" + String.valueOf(_random.nextInt()));

      final Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      security.setIdentifiers(new IdentifierBundle(identifiers));
      security.setName("NAME" + String.valueOf(_random.nextInt()));
      _secMaster.putSecurity(new Date(), security);
    }
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      final OptionType optionType = OptionType.PUT;
      final double strike = _random.nextDouble() * 100;
      final Expiry expiry = new Expiry(DateUtil.getUTCDate(2010, 12, 18));
      final Identifier underlyingUniqueID = Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt()));
      final int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      final EquityOptionSecurity security = new EquityOptionSecurity(new EuropeanExerciseType(), new VanillaPayoffStyle(), optionType, strike, expiry, underlyingUniqueID, Currency
          .getInstance(TEST_CURRENCIES[currIndex]), 1, // TODO change when the point value is properly added
          "EXCHN" + String.valueOf(_random.nextInt()));

      final Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      security.setIdentifiers(new IdentifierBundle(identifiers));
      security.setName("NAME" + String.valueOf(_random.nextInt()));
      _secMaster.putSecurity(new Date(), security);
    }
  }

  private void addRandomAgricultureFutures() {
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      final int year = (int) (100 * _random.nextDouble() + 1900);
      final int month = 1 + _random.nextInt(11);
      final int day = 1 + _random.nextInt(27);
      final Expiry expiry = new Expiry(s_clock.zonedDateTime().withDate(year, month, day).withTime(17, 00));
      final int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      final Currency currency = Currency.getInstance(TEST_CURRENCIES[currIndex]);
      final String exchange = "EXCHN" + String.valueOf(_random.nextInt());
      final String category = "CAT" + String.valueOf(_random.nextInt());
      final AgricultureFutureSecurity sec = new AgricultureFutureSecurity(expiry, exchange, exchange, currency, category);
      sec.setUnitNumber(100.0);
      sec.setUnitName("tonnes");
      sec.setName("NAME" + String.valueOf(_random.nextInt()));
      final Set<Identifier> identifiers = new HashSet<Identifier>();
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      identifiers.add(Identifier.of(IdentificationScheme.CUSIP, "CUSIP" + String.valueOf(_random.nextInt())));
      identifiers.add(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      sec.setIdentifiers(new IdentifierBundle(identifiers));
      _secMaster.putSecurity(new Date(), sec);
    }
  }

  private void addRandomEquities() {
    for (int i = 0; i < TEST_FILLER_SIZE; i++) {
      final int currIndex = _random.nextInt(TEST_CURRENCIES.length);
      final EquitySecurity equitySecurity = new EquitySecurity("EXCHN" + String.valueOf(_random.nextInt()), "EXCHC" + String.valueOf(_random.nextInt()), "CNAME" + String.valueOf(_random.nextInt()),
          Currency.getInstance(TEST_CURRENCIES[currIndex]));
      equitySecurity.setGicsCode(GICSCode.getInstance(String.valueOf(_random.nextInt(98) + 2)));
      equitySecurity.setTicker("TICKER" + String.valueOf(_random.nextInt()));
      equitySecurity.addIdentifier(Identifier.of(IdentificationScheme.BLOOMBERG_TICKER, "TICKER" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(Identifier.of(IdentificationScheme.BLOOMBERG_BUID, "BUID" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(Identifier.of(IdentificationScheme.CUSIP, "CUSIP" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(Identifier.of(IdentificationScheme.ISIN, "ISIN" + String.valueOf(_random.nextInt())));
      equitySecurity.addIdentifier(Identifier.of(IdentificationScheme.SEDOL1, "SEDOL1" + String.valueOf(_random.nextInt())));
      equitySecurity.setName("NAME" + String.valueOf(_random.nextInt()));
      _secMaster.putSecurity(new Date(), equitySecurity);
    }
  }

  private static void assertEquitySecurity(final EquitySecurity expectedEquity, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    final EquitySecurity actualEquity = (EquitySecurity) sec;
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
  private static void assertAmericanVanillaEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    assertTrue(((EquityOptionSecurity) sec).getExerciseType() instanceof AmericanExerciseType);
    assertTrue(((EquityOptionSecurity) sec).getPayoffStyle() instanceof VanillaPayoffStyle);
    assertEquityOptionSecurity(expectedOption, sec);
  }

  private static void assertEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    final EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
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

  private static void assertEuropeanVanillaEquityOptionSecurity(final EquityOptionSecurity expectedOption, final Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    assertTrue(((EquityOptionSecurity) sec).getExerciseType() instanceof EuropeanExerciseType);
    assertTrue(((EquityOptionSecurity) sec).getPayoffStyle() instanceof VanillaPayoffStyle);
    assertEquityOptionSecurity(expectedOption, sec);
  }

}
