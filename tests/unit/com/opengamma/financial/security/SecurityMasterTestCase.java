/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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

import org.junit.Ignore;
import org.junit.Test;

import com.opengamma.engine.security.Security;
import com.opengamma.financial.Currency;
import com.opengamma.financial.GICSCode;
import com.opengamma.financial.RegionRepository;
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
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Generic TestCase for a SecurityMaster implementation. Either inherit from it, or
 * delegate through the SecurityMasterTestCaseMethods interface.
 */
public class SecurityMasterTestCase implements SecurityMasterTestCaseMethods {

  private static final String[] TEST_CURRENCIES = {"USD", "GBP", "YEN", "CHF"};
  private static final int TEST_FILLER_SIZE = 5;
  private final Random _random = new Random();
  private static final Clock s_clock = Clock.system(TimeZone.UTC);

  private final SecurityMaster _secMaster;
  private final RegionRepository _regionSource;

  public SecurityMasterTestCase(final SecurityMaster secMaster, final RegionRepository regionSource) {
    _secMaster = secMaster;
    _regionSource = regionSource;
  }

  private final Security getSecurity(final IdentifierBundle identifiers) {
    final SecuritySearchRequest request = new SecuritySearchRequest();
    request.setIdentifiers(identifiers);
    final SecuritySearchResult result = _secMaster.search(request);
    assertNotNull(result);
    final List<SecurityDocument> documents = result.getDocuments();
    assertNotNull(documents);
    assertEquals(1, documents.size());
    final SecurityDocument document = documents.get(0);
    assertNotNull(document);
    final Security security = document.getSecurity();
    assertNotNull(security);
    return security;
  }

  private final Security getSecurity(final UniqueIdentifier uniqueIdentifier) {
    final SecurityDocument document = _secMaster.get(uniqueIdentifier);
    assertNotNull(document);
    final Security security = document.getSecurity();
    assertNotNull(security);
    return security;
  }

  private final UniqueIdentifier putSecurity(final Security security) {
    final SecurityDocument document = _secMaster.add(new SecurityDocument(security));
    assertNotNull(document);
    return document.getUniqueIdentifier();
  }

  @Test
  @Override
  public void aaplEquityByBbgTicker() throws Exception {
    addRandomEquities();
    final EquitySecurity aaplEquitySecurity = SecurityTestUtils.makeExpectedAAPLEquitySecurity();
    putSecurity(aaplEquitySecurity);
    final IdentifierBundle equityKey = SecurityTestUtils.makeBloombergTickerIdentifier(SecurityTestUtils.AAPL_EQUITY_TICKER);
    final Security security = getSecurity(equityKey);
    assertEquitySecurity(aaplEquitySecurity, security);
  }

  @Test
  @Override
  public void aaplEquityByUniqueIdentifier() throws Exception {
    addRandomEquities();
    final EquitySecurity aaplEquitySecurity = SecurityTestUtils.makeExpectedAAPLEquitySecurity();
    final UniqueIdentifier uniqueIdentifier = putSecurity(aaplEquitySecurity);
    final Security sec = getSecurity(uniqueIdentifier);
    assertEquitySecurity(aaplEquitySecurity, sec);
  }

  @Test
  @Override
  public void apvEquityOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity expectedOption = SecurityTestUtils.makeAPVLEquityOptionSecurity();
    putSecurity(expectedOption);
    final IdentifierBundle equityOptionKey = SecurityTestUtils.makeBloombergTickerIdentifier(SecurityTestUtils.APV_EQUITY_OPTION_TICKER);
    final Security sec = getSecurity(equityOptionKey);
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test
  @Override
  public void spxIndexOptionByBbgTicker() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity spxIndexOptionSecurity = SecurityTestUtils.makeSPXIndexOptionSecurity();
    putSecurity(spxIndexOptionSecurity);
    final IdentifierBundle indexOptionKey = SecurityTestUtils.makeBloombergTickerIdentifier(SecurityTestUtils.SPX_INDEX_OPTION_TICKER);
    final Security sec = getSecurity(indexOptionKey);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  @Override
  public void spxIndexOptionByBbgUnique() throws Exception {
    addRandomEquityOptions();
    final EquityOptionSecurity spxIndexOptionSecurity = SecurityTestUtils.makeSPXIndexOptionSecurity();
    final UniqueIdentifier uniqueIdentifier = putSecurity(spxIndexOptionSecurity);
    final Security sec = getSecurity(uniqueIdentifier);
    assertEuropeanVanillaEquityOptionSecurity(spxIndexOptionSecurity, sec);
  }

  @Test
  @Override
  public void agricultureFuture() throws Exception {
    addRandomAgricultureFutures();
    final AgricultureFutureSecurity wheat = SecurityTestUtils.makeWheatFuture();
    putSecurity(wheat);
    final IdentifierBundle identifiers = wheat.getIdentifiers();
    final Security security = getSecurity(identifiers);
    assertTrue(security instanceof AgricultureFutureSecurity);
    assertEquals(wheat, security);
  }

  @Test
  @Override
  public void indexFuture() throws Exception {
    final IndexFutureSecurity indexFutureSecurity = SecurityTestUtils.makeIndexFuture();
    putSecurity(indexFutureSecurity);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("SPM0 Index");
    final Security security = getSecurity(id);
    assertTrue(security instanceof IndexFutureSecurity);
    assertEquals(indexFutureSecurity, security);
  }

  @Test
  @Override
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
    final UniqueIdentifier bondUID = putSecurity(bond);
    final Security security = getSecurity(bondUID);
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
  @Override
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
    final UniqueIdentifier bondUID = putSecurity(bond);
    final Security security = getSecurity(bondUID);
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
  @Override
  public void currencyFuture() throws Exception {
    final FXFutureSecurity currencyFuture = SecurityTestUtils.makeAUDUSDCurrencyFuture();
    putSecurity(currencyFuture);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("LNM0 Curncy");
    final Security security = getSecurity(id);
    assertTrue(security instanceof FXFutureSecurity);
    assertEquals(currencyFuture, security);
  }

  private void sortBondFutureDeliverable(final BondFutureSecurity security) {
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
  @Override
  public void euroBondFuture() throws Exception {
    final BondFutureSecurity euroBondFuture = SecurityTestUtils.makeEuroBondFuture();
    putSecurity(euroBondFuture);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("RXU0 Comdty");
    final Security security = getSecurity(id);
    assertTrue(security instanceof BondFutureSecurity);
    sortBondFutureDeliverable(euroBondFuture);
    sortBondFutureDeliverable((BondFutureSecurity) security);
    assertEquals(euroBondFuture, security);
  }

  @Test
  @Override
  public void metalFuture() throws Exception {
    final MetalFutureSecurity silverFuture = SecurityTestUtils.makeSilverFuture();
    putSecurity(silverFuture);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("SIM0 Comdty");
    final Security security = getSecurity(id);
    assertTrue(security instanceof MetalFutureSecurity);
    assertEquals(silverFuture, security);
  }

  @Test
  @Override
  public void energyFuture() throws Exception {
    final EnergyFutureSecurity ethanolFuture = SecurityTestUtils.makeEthanolFuture();
    putSecurity(ethanolFuture);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("DLM0 Comdty");
    final Security security = getSecurity(id);
    assertTrue(security instanceof EnergyFutureSecurity);
    assertEquals(ethanolFuture, security);
  }

  @Test
  @Override
  public void interestRateFuture() throws Exception {
    final InterestRateFutureSecurity euroDollarFuture = SecurityTestUtils.makeInterestRateFuture();
    putSecurity(euroDollarFuture);
    final IdentifierBundle id = SecurityTestUtils.makeBloombergTickerIdentifier("EDM0 Comdty");
    final Security security = getSecurity(id);
    assertTrue(security instanceof InterestRateFutureSecurity);
    assertEquals(euroDollarFuture, security);
  }

  @Test
  @Override
  public void update() {
    final EquitySecurity sec = new EquitySecurity("NEW YORK STOCK EXCHANGE", "NYSE", "General Motors", SecurityTestUtils.USD);
    sec.setGicsCode(GICSCode.getInstance(25102010));
    sec.setTicker("GM US Equity");
    sec.setName("General Motors");
    sec.setIdentifiers(new IdentifierBundle(Identifier.of("BLOOMBERG", "GM US Equity")));
    final UniqueIdentifier uid1 = putSecurity(sec);
    sec.setCompanyName("Big Motors");
    final SecurityDocument update = new SecurityDocument();
    update.setSecurity(sec);
    update.setUniqueIdentifier(uid1);
    final UniqueIdentifier uid2 = _secMaster.update(update).getUniqueIdentifier();
    assertEquals(uid1.toLatest(), uid2.toLatest());
    final EquitySecurity loaded1 = (EquitySecurity) getSecurity(uid1);
    assertEquals("General Motors", loaded1.getCompanyName());

    final EquitySecurity loaded2 = (EquitySecurity) getSecurity(uid2);
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
      putSecurity(security);
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
      putSecurity(security);
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
      putSecurity(sec);
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
      putSecurity(equitySecurity);
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
    assertEquals(expectedOption.getStrike(), actualOption.getStrike(), 0);
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

  @Override
  @Test
  public void cashSecurity() throws Exception {
    final CashSecurity expected = SecurityTestUtils.makeCashSecurity();
    putSecurity(expected);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of("TEST", "CASH"));
    final Security security = getSecurity(id);
    assertEquals(expected, security);
  }

  @Override
  @Test
  public void forwardSwapSecurity() throws Exception {
    final ForwardSwapSecurity expected = SecurityTestUtils.makeForwardSwapSecurity(_regionSource);
    putSecurity(expected);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of("TEST", "FORWARDSWAP"));
    final Security security = getSecurity(id);
    assertEquals(expected, security);
  }

  @Override
  @Test
  public void fraSecurity() throws Exception {
    final FRASecurity expected = SecurityTestUtils.makeFRASecurity();
    putSecurity(expected);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of("TEST", "FRA"));
    final Security security = getSecurity(id);
    assertEquals(expected, security);
  }

  @Override
  @Test
  public void swapSecurity() throws Exception {
    final SwapSecurity expected = SecurityTestUtils.makeSwapSecurity(_regionSource);
    putSecurity(expected);
    final IdentifierBundle id = new IdentifierBundle(Identifier.of("TEST", "SWAP"));
    final Security security = getSecurity(id);
    assertEquals(expected, security);
  }

}
