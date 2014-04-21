/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg.security;

import static com.opengamma.bbg.util.BloombergSecurityUtils.AAPL_EQUITY_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.APV_EQUITY_OPTION_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.ATT_EQUITY_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.EUR;
import static com.opengamma.bbg.util.BloombergSecurityUtils.SPX_INDEX_OPTION_TICKER;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAPVLEquityOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAUDUSDCurrencyFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeAgricultureFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEquityFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeEthanolFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExchangeTradedFund;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedAAPLEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeExpectedATTEquitySecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeInterestRateFuture;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSPXIndexOptionSecurity;
import static com.opengamma.bbg.util.BloombergSecurityUtils.makeSilverFuture;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.beans.Bean;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableSet;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.referencedata.impl.BloombergReferenceDataProvider;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
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
import com.opengamma.financial.timeseries.exchange.DefaultExchangeDataProvider;
import com.opengamma.financial.timeseries.exchange.ExchangeDataProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Expiry;

/**
 * Base case for testing BloombergSecuritySource.
 */
@Test(groups = TestGroup.INTEGRATION)
public class BloombergSecurityProviderTest {

  private static final EquitySecurity EXPECTED_AAPL_EQUITY_SEC = makeExpectedAAPLEquitySecurity();
  private static final EquitySecurity EXPECTED_ATT_EQUITY_SEC = makeExpectedATTEquitySecurity();
  private static final EquityOptionSecurity EXPECTED_APVL_EQUITYOPTION_SEC = makeAPVLEquityOptionSecurity();
  private static final EquityIndexOptionSecurity EXPECTED_SPX_INDEXOPTION_SEC = makeSPXIndexOptionSecurity();
  private static final FXFutureSecurity EXPECTED_AUDUSD_FUTURE_SEC = makeAUDUSDCurrencyFuture();
  private static final MetalFutureSecurity EXPECTED_SILVER_FUTURE = makeSilverFuture();
  private static final EnergyFutureSecurity EXPECTED_ETHANOL_FUTURE = makeEthanolFuture();
  private static final InterestRateFutureSecurity EXPECTED_EURODOLLAR_FUTURE = makeInterestRateFuture();
  private static final AgricultureFutureSecurity EXPECTED_WHEAT_FUTURE_SEC = makeAgricultureFuture();
  private static final EquityFutureSecurity EXPECTED_EQUITY_FUTURE_SEC = makeEquityFuture();
  private static final EquitySecurity US_NATURAL_GAS_FUND = makeExchangeTradedFund();

  private static final ExternalScheme[] EXPECTED_IDENTIFICATION_SCHEME = new ExternalScheme[] {
      ExternalSchemes.BLOOMBERG_BUID, ExternalSchemes.BLOOMBERG_TICKER, ExternalSchemes.CUSIP};

  private BloombergReferenceDataProvider _refDataProvider;
  private SecurityProvider _securityProvider;

  @BeforeClass
  public void setupSecurityProvider() throws Exception {
    _securityProvider = createSecurityProvider();
  }

  protected SecurityProvider createSecurityProvider() throws Exception {
    BloombergConnector connector = BloombergTestUtils.getBloombergConnector();
    BloombergReferenceDataProvider refDataProvider = new BloombergReferenceDataProvider(connector);
    refDataProvider.start();
    _refDataProvider = refDataProvider;
    ExchangeDataProvider exchangeProvider = DefaultExchangeDataProvider.getInstance();
    return new BloombergSecurityProvider(refDataProvider, exchangeProvider);
  }

  @AfterClass
  public void terminateSecurityProvider() throws Exception {
    stopSecurityProvider(_securityProvider);
    _securityProvider = null;
  }

  protected void stopSecurityProvider(SecurityProvider provider) throws Exception {
    if (_refDataProvider != null) {
      BloombergReferenceDataProvider dataProvider = _refDataProvider;
      _refDataProvider = null;
      dataProvider.stop();
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void aaplEquityByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    assertNotNull(bloombergIdentifier);
    Security sec = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(sec);
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
  }

  @Test
  public void aaplEquityByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    assertNotNull(bloombergIdentifier);
    Security sec = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
  }

  @Test
  public void attEquityByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
  }

  @Test
  public void exchangeTradedFund() throws Exception {
    Security security = _securityProvider.getSecurity(US_NATURAL_GAS_FUND.getExternalIdBundle());
    assertEquitySecurity(US_NATURAL_GAS_FUND, security);
  }

  @Test
  public void attEquitiesByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, security);
  }

  @Test
  public void attEquityByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
  }

  @Test
  public void attEquitiesByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, security);
  }

  @Test
  public void apvEquityOptionByBbgTicker() throws Exception {
    Security sec = _securityProvider.getSecurity(EXPECTED_APVL_EQUITYOPTION_SEC.getExternalIdBundle());
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
  }

  @Test
  public void apvEquityOptionsByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, security);
  }

  @Test
  public void apvEquityOptionsByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, security);
  }

  @Test
  public void apvEquityOptionByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_APVL_EQUITYOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
  }

  @Test
  public void spxIndexOptionByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
  }

  @Test
  public void spxIndexOptionsByBbgTicker() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_TICKER);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, security);
  }

  @Test
  public void spxIndexOptionByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security sec = _securityProvider.getSecurity(ExternalIdBundle.of(bloombergIdentifier));
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
  }

  @Test
  public void spxIndexOptionsByBbgUnique() throws Exception {
    ExternalId bloombergIdentifier = getBloombergIdentifier(EXPECTED_SPX_INDEXOPTION_SEC, ExternalSchemes.BLOOMBERG_BUID);
    Security security = _securityProvider.getSecurity(bloombergIdentifier.toBundle());
    assertNotNull(security);
    assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, security);
  }

  private ExternalId getBloombergIdentifier(FinancialSecurity finSec, ExternalScheme scheme) {
    ExternalIdBundle identifierBundle = finSec.getExternalIdBundle();
    return identifierBundle.getExternalId(scheme);
  }

  //-------------------------------------------------------------------------
  @Test(groups={"bbgSecurityFutureTests"})
  public void agricultureFuture() throws Exception {
    Security wheat = _securityProvider.getSecurity(EXPECTED_WHEAT_FUTURE_SEC.getExternalIdBundle());
    assertNotNull(wheat);
    assertTrue(wheat instanceof AgricultureFutureSecurity);
    assertSecurity(EXPECTED_WHEAT_FUTURE_SEC, wheat);
  }

  @Test(groups={"bbgSecurityFutureTests"})
  public void equityFuture() throws Exception {
    Security spIndex = _securityProvider.getSecurity(EXPECTED_EQUITY_FUTURE_SEC.getExternalIdBundle());
    assertNotNull(spIndex);
    assertTrue(spIndex instanceof EquityFutureSecurity);
    assertSecurity(EXPECTED_EQUITY_FUTURE_SEC, spIndex);
  }

  @Test(enabled = false)
  public void currencyFuture() throws Exception {
    ExternalIdBundle id = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("LNM0 Curncy"));
    Security audUsd = _securityProvider.getSecurity(id);
    assertNotNull(audUsd);
    assertTrue(audUsd instanceof FXFutureSecurity);
    assertSecurity(EXPECTED_AUDUSD_FUTURE_SEC, audUsd);
  }

  @Test(groups={"bbgSecurityFutureTests"})
  public void euroBondFuture() throws Exception {
    ExternalIdBundle euroBund = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("RXA Comdty"));
    Security bond = _securityProvider.getSecurity(euroBund);
    assertNotNull(bond);
    assertTrue(bond instanceof BondFutureSecurity);
    BondFutureSecurity euroBondFuture = (BondFutureSecurity) bond;
    assertEquals("FUTURE", euroBondFuture.getSecurityType());
    assertEquals("Bond", euroBondFuture.getContractCategory());
    assertEquals(EUR, euroBondFuture.getCurrency());
    String displayName = euroBondFuture.getName();
    assertNotNull(displayName);
    assertTrue(displayName.contains("EURO-BUND FUTURE"));
    Expiry expiry = euroBondFuture.getExpiry();
    assertNotNull(expiry);
    assertTrue(expiry.toInstant().isAfter(getTodayInstant()));
    assertEquals("XEUR", euroBondFuture.getTradingExchange());
    assertEquals("XEUR", euroBondFuture.getSettlementExchange());
    //assert identifiers are set
    Collection<ExternalId> identifiers = euroBondFuture.getExternalIdBundle().getExternalIds();
    assertNotNull(identifiers);
    assertTrue(identifiers.size() >= EXPECTED_IDENTIFICATION_SCHEME.length);
    ExternalIdBundle identifierBundle = ExternalIdBundle.of(identifiers);
    for (ExternalScheme expectedIDScheme : EXPECTED_IDENTIFICATION_SCHEME) {
      assertNotNull(identifierBundle.getExternalId(expectedIDScheme));
    }
    //assert deliverables are not empty
    Collection<BondFutureDeliverable> basket = euroBondFuture.getBasket();
    assertNotNull(basket);
    for (BondFutureDeliverable bondFutureDeliverable : basket) {
      ExternalIdBundle bundle = bondFutureDeliverable.getIdentifiers();
      assertNotNull(bundle);
      assertNotNull(bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID));
      assertTrue(bondFutureDeliverable.getConversionFactor() > 0);
    }
  }

  private Instant getTodayInstant() {
    Instant toDay = Clock.systemUTC().instant();
    return toDay;
  }

  @Test(groups={"bbgSecurityFutureTests"})
  public void metalFuture() throws Exception {
    Security silverFuture = _securityProvider.getSecurity(EXPECTED_SILVER_FUTURE.getExternalIdBundle());
    assertNotNull(silverFuture);
    assertTrue(silverFuture instanceof MetalFutureSecurity);
    assertSecurity(EXPECTED_SILVER_FUTURE, silverFuture);
  }

  @Test(groups={"bbgSecurityFutureTests"})
  public void energyFuture() throws Exception {
    Security ethanolFuture = _securityProvider.getSecurity(EXPECTED_ETHANOL_FUTURE.getExternalIdBundle());
    assertNotNull(ethanolFuture);
    assertTrue(ethanolFuture instanceof EnergyFutureSecurity);
    assertSecurity(EXPECTED_ETHANOL_FUTURE, ethanolFuture);
  }

  @Test(groups={"bbgSecurityFutureTests"})
  public void interestRateFuture() throws Exception {
    Security euroDollar = _securityProvider.getSecurity(EXPECTED_EURODOLLAR_FUTURE.getExternalIdBundle());
    assertNotNull(euroDollar);
    assertTrue(euroDollar instanceof InterestRateFutureSecurity);
    assertSecurity(EXPECTED_EURODOLLAR_FUTURE, euroDollar);
  }

  @Test
  public void invalidSecurity() throws Exception {
    ExternalIdBundle invalidKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("INVALID"));
    Security sec = _securityProvider.getSecurity(invalidKey);
    assertNull(sec);
  }

  @Test
  public void invalidSecurities() throws Exception {
    ExternalIdBundle invalidKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId("INVALID"));
    Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(ImmutableSet.of(invalidKey));
    assertNotNull(securities);
    assertTrue(securities.isEmpty());
  }

  @Test
  public void multiThreadedSecurityRequest() throws Exception {

    ExternalIdBundle apvKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    ExternalIdBundle spxKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    ExternalIdBundle aaplKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    ExternalIdBundle attKey = ExternalIdBundle.of(ExternalSchemes.bloombergTickerSecurityId(ATT_EQUITY_TICKER));

    ExecutorService pool = Executors.newFixedThreadPool(4);
    List<Future<Security>> apvresults = new ArrayList<Future<Security>>();
    List<Future<Security>> spxresults = new ArrayList<Future<Security>>();
    List<Future<Security>> aaplresults = new ArrayList<Future<Security>>();
    List<Future<Security>> attresults = new ArrayList<Future<Security>>();

    for (int i = 0; i < 10; i++) {
      apvresults.add(pool.submit(new BSMGetSecurityCallable(apvKey)));
      spxresults.add(pool.submit(new BSMGetSecurityCallable(spxKey)));
      aaplresults.add(pool.submit(new BSMGetSecurityCallable(aaplKey)));
      attresults.add(pool.submit(new BSMGetSecurityCallable(attKey)));
    }

    for (Future<Security> future : apvresults) {
      // Check that each one didn't throw an exception and returns the expected
      // APV security
      Security sec = future.get();
      assertAmericanVanillaEquityOptionSecurity(EXPECTED_APVL_EQUITYOPTION_SEC, sec);
    }

    for (Future<Security> future : spxresults) {
      // Check that each one didn't throw an exception and returns the expected
      // SPX security
      Security sec = future.get();
      assertEuropeanVanillaEquityIndexOptionSecurity(EXPECTED_SPX_INDEXOPTION_SEC, sec);
    }

    for (Future<Security> future : aaplresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AAPL security
      Security sec = future.get();
      assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, sec);
    }

    for (Future<Security> future : attresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AT&T security
      Security sec = future.get();
      assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, sec);
    }
  }

  static void assertSecurity(Security expected, Security actual) {
    assertNotNull(actual);
    BeanAssert.assertBeanEquals((Bean) expected, (Bean) actual);
  }

  static void assertEquitySecurity(EquitySecurity expectedEquity, final Security sec) {
    expectedEquity = expectedEquity.clone();
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    EquitySecurity actualEquity = (EquitySecurity) sec;
    assertEquals(expectedEquity.getSecurityType(), actualEquity.getSecurityType());
    
    ExternalId expectedBUID = expectedEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
    ExternalId actualBUID = actualEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_BUID);
    assertEquals(expectedBUID, actualBUID);
    
    ExternalId expectedTicker = expectedEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    ExternalId actualTicker = actualEquity.getExternalIdBundle().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    assertEquals(expectedTicker, actualTicker);
    
    assertEquals(expectedEquity.getUniqueId(), actualEquity.getUniqueId());
    assertEquals(expectedEquity.getShortName(), actualEquity.getShortName());
    assertEquals(expectedEquity.getExchange(), actualEquity.getExchange());
    assertEquals(expectedEquity.getCompanyName(), actualEquity.getCompanyName());
    assertEquals(expectedEquity.getCurrency(), actualEquity.getCurrency());
    
    // check the lot without Identifiers
    ExternalIdBundle expectedIdentifiers = expectedEquity.getExternalIdBundle();
    ExternalIdBundle actualIdentifiers = actualEquity.getExternalIdBundle();
    
    expectedEquity.setExternalIdBundle(ExternalIdBundle.EMPTY);
    actualEquity.setExternalIdBundle(ExternalIdBundle.EMPTY);
    assertEquals(expectedEquity, actualEquity);
    
    expectedEquity.setExternalIdBundle(expectedIdentifiers);
    actualEquity.setExternalIdBundle(actualIdentifiers);
  }

  static void assertAmericanVanillaEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    final EquityOptionSecurity equitySec = (EquityOptionSecurity) sec;
    assertTrue(equitySec.getExerciseType() instanceof AmericanExerciseType);
    assertEquityOptionSecurity(expectedOption, sec);
  }

  static void assertAmericanVanillaEquityIndexOptionSecurity(EquityIndexOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityIndexOptionSecurity);
    final EquityIndexOptionSecurity equityIndexOption = (EquityIndexOptionSecurity) sec;
    assertTrue(equityIndexOption.getExerciseType() instanceof AmericanExerciseType);
    assertEquityIndexOptionSecurity(expectedOption, sec);
  }

  static void assertEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption.getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
    assertEquals(expectedOption.getName(), actualOption.getName());
    // check the lot
    assertSecurity(expectedOption, sec);
  }

  static void assertEquityIndexOptionSecurity(EquityIndexOptionSecurity expectedOption, Security sec) {
    assertNotNull(expectedOption);
    assertNotNull(sec);
    EquityIndexOptionSecurity actualOption = (EquityIndexOptionSecurity) sec;
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption.getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
    assertEquals(expectedOption.getName(), actualOption.getName());
    // check the lot
    assertSecurity(expectedOption, sec);
  }

  static void assertEuropeanVanillaEquityOptionSecurity(EquityOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    final EquityOptionSecurity equitySec = (EquityOptionSecurity) sec;
    assertTrue(equitySec.getExerciseType() instanceof EuropeanExerciseType);
    assertEquityOptionSecurity(expectedOption, sec);
  }

  static void assertEuropeanVanillaEquityIndexOptionSecurity(EquityIndexOptionSecurity expectedOption, Security sec) {
    // check specific bits we want to spot failures on quickly
    assertNotNull(sec);
    assertTrue(sec instanceof EquityIndexOptionSecurity);
    final EquityIndexOptionSecurity equityIndexOption = (EquityIndexOptionSecurity) sec;
    assertTrue(equityIndexOption.getExerciseType() instanceof EuropeanExerciseType);
    assertEquityIndexOptionSecurity(expectedOption, sec);
  }

  private class BSMGetSecurityCallable implements Callable<Security> {
    ExternalIdBundle _secKey;

    public BSMGetSecurityCallable(ExternalIdBundle secKey) {
      _secKey = secKey;
    }

    @Override
    public Security call() throws Exception {
      return _securityProvider.getSecurity(_secKey);
    }
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getBulkSecurity() throws Exception {
    ExternalIdBundle aaplId = getBloombergIdentifier(EXPECTED_AAPL_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID).toBundle();
    ExternalIdBundle attId = getBloombergIdentifier(EXPECTED_ATT_EQUITY_SEC, ExternalSchemes.BLOOMBERG_BUID).toBundle();
    
    Map<ExternalIdBundle, Security> securities = _securityProvider.getSecurities(ImmutableSet.of(aaplId, attId));
    assertNotNull(securities);
    assertEquals(2, securities.size());
    assertTrue(securities.keySet().contains(aaplId));
    assertTrue(securities.keySet().contains(attId));
    assertEquitySecurity(EXPECTED_AAPL_EQUITY_SEC, securities.get(aaplId));
    assertEquitySecurity(EXPECTED_ATT_EQUITY_SEC, securities.get(attId));
    
  }

}
