/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.server.SecurityMasterRequestReceiver;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.AmericanExerciseType;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EuropeanExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.transport.jms.JmsByteArrayRequestDispatcher;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class RemoteBloombergSecurityMasterWithActiveMQTest {
  private static final String APV_EQUITY_OPTION_TICKER = "APV US 1 C190 Equity";
  private static final String SPX_INDEX_OPTION_TICKER = "SPX 12 C1100 Index";
  private static final String AAPL_EQUITY_TICKER = "AAPL US Equity";
  private static final String ATT_EQUITY_TICKER = "T US Equity";
  private ConfigurableApplicationContext _clientContext;
  private ConfigurableApplicationContext _serverContext;
  private RemoteBloombergSecuritySource _remoteSecSource;

  @BeforeMethod
  public void setUp() throws Exception {
    ConfigurableApplicationContext clientContext = new ClassPathXmlApplicationContext(
        "/com/opengamma/bbg/test-client-remote-sec-master-context.xml");
    clientContext.start();
    _clientContext = clientContext;
    ConfigurableApplicationContext serverContext = new ClassPathXmlApplicationContext(
        "/com/opengamma/bbg/test-server-remote-sec-master-context.xml");
    serverContext.start();
    _serverContext = serverContext;
    setUpRemoteSecurityMaster();
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_clientContext != null) {
      ConfigurableApplicationContext clientContext = _clientContext;
      _clientContext = null;
      clientContext.close();
    }
    if (_serverContext != null) {
      ConfigurableApplicationContext serverContext = _serverContext;
      _serverContext = null;
      serverContext.close();
    }
  }

  protected void setUpRemoteSecurityMaster() throws Exception {
    assertClientSpringConfig();
    assertServerSpringConfig();
    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer) _serverContext
        .getBean("jmsContainer");
    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider) _serverContext
        .getBean("refDataProvider");
    RemoteBloombergSecuritySource remoteSecSource = (RemoteBloombergSecuritySource) _clientContext
        .getBean("remoteBloombergSecuritySource");
    assertNotNull(remoteSecSource);
    _remoteSecSource = remoteSecSource;
    while (!jmsContainer.isRunning() && !refDataProvider.isRunning()) {
      Thread.sleep(10l);
    }
  }

  /**
   * 
   */
  private void assertServerSpringConfig() {
    PropertyPlaceholderConfigurer propConfigurer = (PropertyPlaceholderConfigurer) _serverContext
        .getBean("propertyConfigurer");
    assertNotNull(propConfigurer);

    BloombergReferenceDataProvider refDataProvider = (BloombergReferenceDataProvider) _serverContext
        .getBean("refDataProvider");
    assertNotNull(refDataProvider);
    assertNotNull(refDataProvider.getSessionOptions());
    assertNotNull(refDataProvider.getSessionOptions().getServerHost());

    CachingReferenceDataProvider cachingProvider = (CachingReferenceDataProvider) _serverContext
        .getBean("cachingRefDataProvider");
    assertNotNull(cachingProvider);

    BloombergSecurityMaster secMaster = (BloombergSecurityMaster) _serverContext
        .getBean("bloombergSecurityMaster");
    assertNotNull(secMaster);

    SecurityMasterRequestReceiver requestDispatcher = (SecurityMasterRequestReceiver) _serverContext
        .getBean("requestDispatcher");
    assertNotNull(requestDispatcher);
    assertSame(secMaster, requestDispatcher.getSecuritySource());

    JmsByteArrayRequestDispatcher jmsByteArrayRequestDispatcher = (JmsByteArrayRequestDispatcher) _serverContext
        .getBean("jmsByteArrayRequestDispatcher");
    assertNotNull(jmsByteArrayRequestDispatcher);
    assertSame(requestDispatcher, jmsByteArrayRequestDispatcher.getUnderlying());

    ActiveMQConnectionFactory factory = (ActiveMQConnectionFactory) _serverContext
        .getBean("jmsConnectionFactory");
    assertNotNull(factory);

    DefaultMessageListenerContainer jmsContainer = (DefaultMessageListenerContainer) _serverContext
        .getBean("jmsContainer");
    assertNotNull(jmsContainer);
    assertSame(jmsByteArrayRequestDispatcher, jmsContainer.getMessageListener());
    assertSame(factory, jmsContainer.getConnectionFactory());
    assertEquals("refDataRequestQueue", jmsContainer.getDestinationName());
    assertFalse(jmsContainer.isPubSubDomain());

  }

  /**
   * 
   */
  private void assertClientSpringConfig() {
    PropertyPlaceholderConfigurer propConfigurer = (PropertyPlaceholderConfigurer) _serverContext
        .getBean("propertyConfigurer");
    assertNotNull(propConfigurer);

    ActiveMQConnectionFactory connFactory = (ActiveMQConnectionFactory) _serverContext
        .getBean("jmsConnectionFactory");
    assertNotNull(connFactory);

    JmsTemplate jmsTemplate = (JmsTemplate) _clientContext
        .getBean("jmsTemplate");
    assertNotNull(jmsTemplate);
    assertNotNull(jmsTemplate.getConnectionFactory());
    assertEquals(5000l, jmsTemplate.getReceiveTimeout());
    assertFalse(jmsTemplate.isPubSubDomain());

    JmsByteArrayRequestSender jmsByteArrayRequestSender = (JmsByteArrayRequestSender) _clientContext
        .getBean("jmsByteArrayRequestSender");
    assertNotNull(jmsByteArrayRequestSender);

    SecuritySource remoteSecurityMaster = (SecuritySource) _clientContext
        .getBean("remoteSecurityMaster");
    assertNotNull(remoteSecurityMaster);
  }

  // @Test
  // @Ignore("Because this contacts Bloomberg, we don't want to run all the time")
  // public void getSecurityType() throws Exception {
  // String bbgEquitySecType = BLOOMBERG_EQUITY_SECURITY_TYPE;
  // String bbgEquityOptionSecType = BLOOMBERG_EQUITY_OPTION_SECURITY_TYPE;
  // assertEquals(bbgEquitySecType,
  // _remoteSecMaster.getSecurityType(AAPL_EQUITY_TICKER));
  // assertEquals(bbgEquityOptionSecType,
  // _remoteSecMaster.getSecurityType(APV_EQUITY_OPTION_TICKER));
  // assertEquals(bbgEquitySecType,
  // _remoteSecMaster.getSecurityType("T US Equity"));
  // assertNull(_remoteSecMaster.getSecurityType("INVALID"));
  // }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplEquityByBbgTicker() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    Security sec = _remoteSecSource.getSecurity(equityKey);
    EquitySecurity expectedEquity = makeAAPLEquitySecurity(true);
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplEquityByBbgUnique() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EQ0010169500001000"));
    Security sec = _remoteSecSource.getSecurity(equityKey);
    EquitySecurity expectedEquity = makeAAPLEquitySecurity(false);
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplEquitiesByBbgTicker() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    EquitySecurity expectedEquity = makeAAPLEquitySecurity(true);
    Collection<Security> securities = _remoteSecSource.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplEquitiesByBbgUnique() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EQ0010169500001000"));
    EquitySecurity expectedEquity = makeAAPLEquitySecurity(false);
    Collection<Security> securities = _remoteSecSource.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void attEquityByBbgTicker() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(ATT_EQUITY_TICKER));
    EquitySecurity expectedEquity = makeATTEquitySecurity(true);
    Security sec = _remoteSecSource.getSecurity(equityKey);
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void attEquitiesByBbgTicker() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(ATT_EQUITY_TICKER));
    EquitySecurity expectedEquity = makeATTEquitySecurity(true);
    Collection<Security> securities = _remoteSecSource.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void attEquityByBbgUnique() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EQ0010137600001000"));
    EquitySecurity expectedEquity = makeATTEquitySecurity(false);
    Security sec = _remoteSecSource.getSecurity(equityKey);
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void attEquitiesByBbgUnique() throws Exception {
    ExternalIdBundle equityKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EQ0010137600001000"));
    EquitySecurity expectedEquity = makeATTEquitySecurity(false);
    Collection<Security> securities = _remoteSecSource.getSecurities(equityKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertEquitySecurity(expectedEquity, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void apvEquityOptionByBbgTicker() throws Exception {
    ExternalIdBundle equityOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity(true);
    Security sec = _remoteSecSource.getSecurity(equityOptionKey);
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void apvEquityOptionsByBbgTicker() throws Exception {
    ExternalIdBundle equityOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity(true);
    Collection<Security> securities = _remoteSecSource
        .getSecurities(equityOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void apvEquityOptionsByBbgUnique() throws Exception {
    ExternalIdBundle equityOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EO1016952010010397C00001"));
    EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity(false);
    Collection<Security> securities = _remoteSecSource
        .getSecurities(equityOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void apvEquityOptionByBbgUnique() throws Exception {
    ExternalIdBundle equityOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("EO1016952010010397C00001"));
    EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity(false);
    Security sec = _remoteSecSource.getSecurity(equityOptionKey);
    assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void spxIndexOptionByBbgTicker() throws Exception {
    ExternalIdBundle indexOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    EquityIndexOptionSecurity expectedOption = makeSPXIndexOptionSecurity(true);
    Security sec = _remoteSecSource.getSecurity(indexOptionKey);
    BloombergSecurityMasterTestCase.assertEuropeanVanillaEquityIndexOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void spxIndexOptionsByBbgTicker() throws Exception {
    ExternalIdBundle indexOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    EquityIndexOptionSecurity expectedOption = makeSPXIndexOptionSecurity(true);
    Collection<Security> securities = _remoteSecSource
        .getSecurities(indexOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    BloombergSecurityMasterTestCase.assertEuropeanVanillaEquityIndexOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void spxIndexOptionByBbgUnique() throws Exception {
    ExternalIdBundle indexOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("IX3961626-0-9B80"));
    EquityIndexOptionSecurity expectedOption = makeSPXIndexOptionSecurity(false);
    Security sec = _remoteSecSource.getSecurity(indexOptionKey);
    BloombergSecurityMasterTestCase.assertEuropeanVanillaEquityIndexOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void spxIndexOptionsByBbgUnique() throws Exception {
    ExternalIdBundle indexOptionKey = ExternalIdBundle.of(
        SecurityUtils.bloombergBuidSecurityId("IX3961626-0-9B80"));
    EquityIndexOptionSecurity expectedOption = makeSPXIndexOptionSecurity(false);
    Collection<Security> securities = _remoteSecSource
        .getSecurities(indexOptionKey);
    assertNotNull(securities);
    assertEquals(1, securities.size());
    Security sec = securities.iterator().next();
    BloombergSecurityMasterTestCase.assertEuropeanVanillaEquityIndexOptionSecurity(expectedOption, sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void invalidSecurity() throws Exception {
    ExternalIdBundle invalidKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId("INVALID"));
    Security sec = _remoteSecSource.getSecurity(invalidKey);
    assertNull(sec);
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void invalidSecurities() throws Exception {
    ExternalIdBundle invalidKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId("INVALID"));
    Collection<Security> securities = _remoteSecSource
        .getSecurities(invalidKey);
    assertNotNull(securities);
    assertTrue(securities.isEmpty());
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void multiThreadedSecurityRequest() throws Exception {

    ExternalIdBundle apvKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    ExternalIdBundle spxKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    ExternalIdBundle aaplKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    ExternalIdBundle attKey = ExternalIdBundle.of(
        SecurityUtils.bloombergTickerSecurityId(ATT_EQUITY_TICKER));

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
      EquityOptionSecurity expectedOption = makeAPVLEquityOptionSecurity(true);
      assertAmericanVanillaEquityOptionSecurity(expectedOption, sec);
    }

    for (Future<Security> future : spxresults) {
      // Check that each one didn't throw an exception and returns the expected
      // SPX security
      Security sec = future.get();
      EquityIndexOptionSecurity expectedOption = makeSPXIndexOptionSecurity(true);
      BloombergSecurityMasterTestCase.assertEuropeanVanillaEquityIndexOptionSecurity(expectedOption, sec);
    }

    for (Future<Security> future : aaplresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AAPL security
      Security sec = future.get();
      EquitySecurity expectedEquity = makeAAPLEquitySecurity(true);
      assertEquitySecurity(expectedEquity, sec);
    }

    for (Future<Security> future : attresults) {
      // Check that each one didn't throw an exception and returns the expected
      // AT&T security
      Security sec = future.get();
      EquitySecurity expectedEquity = makeATTEquitySecurity(true);
      assertEquitySecurity(expectedEquity, sec);
    }
  }

  @Test(enabled = false, description = "Because this contacts Bloomberg, we don't want to run all the time")
  public void aaplOptionChain() throws Exception {
    Set<String> optionChain = _remoteSecSource
        .getOptionChain(SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    assertNotNull(optionChain);
    assertFalse(optionChain.isEmpty());
  }

  /**
   * @return
   */
  private EquityIndexOptionSecurity makeSPXIndexOptionSecurity(boolean haveBbgTicker) {
    OptionType optionType = OptionType.CALL;
    double strike = 1100.0;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2009, 12, 19));
    ExternalId underlyingUniqueID = SecurityUtils.bloombergBuidSecurityId("EI09SPX");
    Currency currency = Currency.USD;
    
    final EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(optionType, strike, currency, underlyingUniqueID, new EuropeanExerciseType(), expiry, 1, "UKX");
    
    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("IX3961626-0-9B80"));
    if (haveBbgTicker) {
      identifiers.add(SecurityUtils.bloombergTickerSecurityId(SPX_INDEX_OPTION_TICKER));
    }
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecurityMaster.createUniqueId("IX3961626-0-9B80"));
    return security;
  }

  /**
   * @return
   */
  private EquityOptionSecurity makeAPVLEquityOptionSecurity(
      boolean haveBbgTicker) {
    OptionType optionType = OptionType.CALL;
    double strike = 190.0;
    Expiry expiry = new Expiry(DateUtils.getUTCDate(2010, 01, 16));
    ExternalId underlyingUniqueID = SecurityUtils.bloombergBuidSecurityId("EQ0010169500001000");
    
    Currency currency = Currency.USD;
    final EquityOptionSecurity security = new EquityOptionSecurity(optionType, strike, currency, underlyingUniqueID, 
        new AmericanExerciseType(), expiry, 1, "DJX");

    Set<ExternalId> identifiers = new HashSet<ExternalId>();
    if (haveBbgTicker) {
      identifiers.add(SecurityUtils.bloombergTickerSecurityId(APV_EQUITY_OPTION_TICKER));
    }
    identifiers.add(SecurityUtils.bloombergBuidSecurityId("EO1016952010010397C00001"));
    security.setExternalIdBundle(ExternalIdBundle.of(identifiers));
    security.setUniqueId(BloombergSecurityMaster.createUniqueId("EO1016952010010397C00001"));

    return security;
  }

  /**
   * @return
   */
  private EquitySecurity makeAAPLEquitySecurity(boolean haveBbgTicker) {
    EquitySecurity equitySecurity = new EquitySecurity("US", "US", "APPLE INC", Currency.USD);
    if (haveBbgTicker) {
      equitySecurity.addExternalId(
          SecurityUtils.bloombergTickerSecurityId(AAPL_EQUITY_TICKER));
    }
    equitySecurity.addExternalId(
        SecurityUtils.bloombergBuidSecurityId("EQ0010169500001000"));
    equitySecurity.addExternalId(
        SecurityUtils.cusipSecurityId("037833100"));
    equitySecurity.addExternalId(
        SecurityUtils.isinSecurityId("US0378331005"));
    equitySecurity.addExternalId(
        SecurityUtils.sedol1SecurityId("2046251"));

    equitySecurity.setUniqueId(BloombergSecurityMaster.createUniqueId("EQ0010169500001000"));
    equitySecurity.setShortName("AAPL");

    return equitySecurity;
  }

  /**
   * @return
   */
  private EquitySecurity makeATTEquitySecurity(boolean haveBbgTicker) {
    EquitySecurity equitySecurity = new EquitySecurity("US", "US", "AT&T INC", Currency.USD);
    
    if (haveBbgTicker) {
      equitySecurity.addExternalId(
          SecurityUtils.bloombergTickerSecurityId(ATT_EQUITY_TICKER));
    }
    equitySecurity.addExternalId(
        SecurityUtils.bloombergBuidSecurityId("EQ0010137600001000"));
    equitySecurity.addExternalId(
        SecurityUtils.cusipSecurityId("00206R102"));
    equitySecurity.addExternalId(
        SecurityUtils.isinSecurityId("US00206R1023"));
    equitySecurity.addExternalId(
        SecurityUtils.sedol1SecurityId("2831811"));
    
    equitySecurity.setUniqueId(BloombergSecurityMaster.createUniqueId("EQ0010137600001000"));
    equitySecurity.setShortName("T");
    return equitySecurity;
  }

  /**
   * @param sec
   * @param expectedEquity
   */
  public static void assertEquitySecurity(EquitySecurity expectedEquity,
      Security sec) {
    assertNotNull(sec);
    assertTrue(sec instanceof EquitySecurity);
    EquitySecurity actualEquity = (EquitySecurity) sec;
    assertEquals(expectedEquity.getSecurityType(), actualEquity
        .getSecurityType());
    assertEquals(expectedEquity.getExternalIdBundle(), actualEquity.getExternalIdBundle());
    assertEquals(expectedEquity.getUniqueId(), actualEquity.getUniqueId());
    assertEquals(expectedEquity.getShortName(), actualEquity.getShortName());
    assertEquals(expectedEquity.getExchange(), actualEquity.getExchange());
    assertEquals(expectedEquity.getCompanyName(), actualEquity.getCompanyName());
    assertEquals(expectedEquity.getCurrency(), actualEquity.getCurrency());
  }

  /**
   * @param expectedOption
   * @param sec
   */
  public static void assertAmericanVanillaEquityOptionSecurity(
      EquityOptionSecurity expectedOption, Security sec) {
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
    assertTrue(actualOption.getExerciseType() instanceof AmericanExerciseType);
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption
        .getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
  }

  /**
   * @param expectedOption
   * @param sec
   */
  public static void assertEuropeanVanillaEquityOptionSecurity(
      EquityOptionSecurity expectedOption, Security sec) {
    assertNotNull(sec);
    assertTrue(sec instanceof EquityOptionSecurity);
    EquityOptionSecurity actualOption = (EquityOptionSecurity) sec;
    assertTrue(actualOption.getExerciseType() instanceof EuropeanExerciseType);
    assertEquals(expectedOption.getExternalIdBundle(), actualOption.getExternalIdBundle());
    assertEquals(expectedOption.getUniqueId(), actualOption.getUniqueId());
    assertEquals(expectedOption.getSecurityType(), actualOption
        .getSecurityType());
    assertEquals(expectedOption.getCurrency(), actualOption.getCurrency());
    assertEquals(expectedOption.getOptionType(), actualOption.getOptionType());
    assertTrue(expectedOption.getStrike() == actualOption.getStrike());
    assertEquals(expectedOption.getExpiry(), actualOption.getExpiry());
    assertEquals(expectedOption.getUnderlyingId(), actualOption.getUnderlyingId());
  }

  private class BSMGetSecurityCallable implements Callable<Security> {
    ExternalIdBundle _secKey;

    public BSMGetSecurityCallable(ExternalIdBundle secKey) {
      _secKey = secKey;
    }

    public Security call() throws Exception {
      return _remoteSecSource.getSecurity(_secKey);
    }
  }

}
