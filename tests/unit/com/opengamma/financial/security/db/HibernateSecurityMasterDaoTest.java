/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;


import static com.opengamma.financial.security.db.Converters.identifierToIdentifierBean;
import static com.opengamma.financial.security.db.HibernateSecurityMasterTestUtils.makeAAPLEquitySecurityBean;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.test.HibernateTest;

/**
 * Testcases for dao methods  for HibernateSecurityMasterDao
 * 
 */
public class HibernateSecurityMasterDaoTest  extends HibernateTest {
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterDaoTest.class);
  
  private static final String TEST_STR = "TEST-STR";
  private Random _random = new Random();
  private StringBuffer _debugMsg = new StringBuffer();
  private SynchronousQueue<String> _q = new SynchronousQueue<String>();
  
  class Watchdog implements Runnable {
    private SynchronousQueue<String> _queue;
    public Watchdog(SynchronousQueue<String> queue) {
      _queue = queue;
    }
    
    private void sendMail() {
      final String smtpServer = "mail.opengamma.com";
      final String to = "jim@opengamma.com";
      final String from = "root@bamboo.opengamma.com";
      final String subject = "Test failure";
      Properties props = new Properties();
      props.put("mail.smtp.host", smtpServer);
      try {
        Session session = Session.getDefaultInstance(props, null);
        // -- Create a new message --
        Message msg = new MimeMessage(session);
        // -- Set the FROM and TO fields --
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO,
          InternetAddress.parse(to, false));
        // -- We could include CC recipients too --
        // if (cc != null)
        // msg.setRecipients(Message.RecipientType.CC
        // ,InternetAddress.parse(cc, false));
        // -- Set the subject and body text --
        msg.setSubject(subject);
        msg.setText(_debugMsg.toString());
        // -- Set some other header information --
        msg.setHeader("X-Mailer", "LOTONtechEmail");
        msg.setSentDate(new Date());
        // -- Send the message --
        Transport.send(msg);
        System.out.println("Message sent OK.");
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    
    @Override
    public void run() {
      try {
        while (true) {
          String message = _queue.poll(60, TimeUnit.SECONDS);
          if (message != null) {
            if (message.length() == 0) {
              return;
            } else {
              _debugMsg.append(message + "\n");
            }
          } else {
            Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
            for (Thread thread : allStackTraces.keySet()) {
              StackTraceElement[] stackTraceElements = allStackTraces.get(thread);
              _debugMsg.append("Thread:"+thread.getName()+"("+thread.getId()+")\n");
              for (StackTraceElement stackTraceElement : stackTraceElements) {
                _debugMsg.append("  "+stackTraceElement.toString()+'n');
              }
            }
            sendMail();
          }
        }
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  private void log(String message) {
    try {
      _q.put(message);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * @param databaseType
   * @param databaseVersion
   */
  public HibernateSecurityMasterDaoTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    Thread watchDog = new Thread(new Watchdog(_q));
    watchDog.start();
    s_logger.info("running test for databaseType={} databaseVersion={}", databaseType, databaseVersion);
    log("running test for databaseType=" + databaseType + " databaseVersion=" + databaseVersion);
  }

  private HibernateSecurityMasterDao _hibernateSecurityMasterDao;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    SessionFactory sessionFactory = getSessionFactory();
    _hibernateSecurityMasterDao  = new HibernateSecurityMasterSession(sessionFactory.openSession());
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _hibernateSecurityMasterDao = null;
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] { BondFutureTypeBean.class, BondSecurityBean.class, BusinessDayConventionBean.class, CashRateTypeBean.class, CommodityFutureTypeBean.class,
        CouponTypeBean.class, CurrencyBean.class, DayCountBean.class, EquitySecurityBean.class, ExchangeBean.class, FrequencyBean.class, FutureBundleBean.class,
        FutureSecurityBean.class, GICSCodeBean.class, GuaranteeTypeBean.class, IdentifierAssociationBean.class, IssuerTypeBean.class, MarketBean.class, OptionSecurityBean.class,
        SecurityBean.class, UnitBean.class, YieldConventionBean.class };
  }
  
  @Test
  public void getOrCreateExchangeBean() throws Exception {
    log("getOrCreateExchangeBean: started");
    List<ExchangeBean> exchangeBeans = _hibernateSecurityMasterDao.getExchangeBeans();
    assertNotNull(exchangeBeans);
    assertTrue(exchangeBeans.isEmpty());
    final ExchangeBean ukxBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("UKX", "FTSE 100");
    exchangeBeans = _hibernateSecurityMasterDao.getExchangeBeans();
    assertEquals(1, exchangeBeans.size());
    final ExchangeBean ExchangeBean = exchangeBeans.get(0);
    assertEquals("UKX", ExchangeBean.getName());
    assertEquals("FTSE 100", ExchangeBean.getDescription());
    assertEquals(ukxBean.getId(), ExchangeBean.getId());
    final Long ukxId = ExchangeBean.getId();
    final ExchangeBean tpxBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("TPX", "Topix");
    final ExchangeBean spxBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("SPX", "S&P 500");
    final ExchangeBean djxBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("DJX", "Dow Jones");
    final ExchangeBean ruyBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("RUY", "Russell 2000");
    exchangeBeans = _hibernateSecurityMasterDao.getExchangeBeans();
    assertEquals(5, exchangeBeans.size());
    assertTrue(exchangeBeans.contains(ukxBean));
    assertTrue(exchangeBeans.contains(tpxBean));
    assertTrue(exchangeBeans.contains(spxBean));
    assertTrue(exchangeBeans.contains(djxBean));
    assertTrue(exchangeBeans.contains(ruyBean));
    final ExchangeBean usdBean2 = _hibernateSecurityMasterDao.getOrCreateExchangeBean("UKX", "FTSE 100");
    assertEquals(ukxId, usdBean2.getId());
    log("getOrCreateExchangeBean: finished");
  }
  
  @Test
  public void getOrCreateCurrencyBean() throws Exception {
    log("getOrCreateCurrencyBean: started");
    List<CurrencyBean> currencyBeans = _hibernateSecurityMasterDao.getCurrencyBeans();
    assertNotNull(currencyBeans);
    assertTrue(currencyBeans.isEmpty());
    final CurrencyBean usdBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("USD");
    currencyBeans = _hibernateSecurityMasterDao.getCurrencyBeans();
    assertEquals(1, currencyBeans.size());
    final CurrencyBean currencyBean = currencyBeans.get(0);
    assertEquals("USD", currencyBean.getName());
    assertEquals(usdBean.getId(), currencyBean.getId());
    final Long usdId = currencyBean.getId();
    final CurrencyBean gbpBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("GBP");
    final CurrencyBean eurBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("EUR");
    final CurrencyBean jpyBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("JPY");
    final CurrencyBean nzdBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("NZD");
    currencyBeans = _hibernateSecurityMasterDao.getCurrencyBeans();
    assertEquals(5, currencyBeans.size());
    assertTrue(currencyBeans.contains(usdBean));
    assertTrue(currencyBeans.contains(gbpBean));
    assertTrue(currencyBeans.contains(eurBean));
    assertTrue(currencyBeans.contains(jpyBean));
    assertTrue(currencyBeans.contains(nzdBean));
    final CurrencyBean usdBean2 = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("USD");
    assertEquals(usdId, usdBean2.getId());
    log("getOrCreateCurrencyBean: finished");
  }
  
  @Test
  public void getOrCreateGICSCodeBean() throws Exception {
    log("getOrCreateGICSCodeBean: started");
    List<GICSCodeBean> gicsCodeBeans = _hibernateSecurityMasterDao.getGICSCodeBeans();
    assertNotNull(gicsCodeBeans);
    assertTrue(gicsCodeBeans.isEmpty());
    
    final GICSCodeBean energyBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("10", "Energy");
    List<GICSCodeBean> gicsBeans = _hibernateSecurityMasterDao.getGICSCodeBeans();
    assertEquals(1, gicsBeans.size());
    GICSCodeBean gicsBean = gicsBeans.get(0);
    assertEquals("10", gicsBean.getName());
    assertEquals("Energy", gicsBean.getDescription());
    assertEquals(energyBean.getId(), gicsBean.getId());
    final Long energyId = gicsBean.getId();
    final GICSCodeBean materialBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("15", "Materials");
    final GICSCodeBean chemicalsBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("151010", "Chemicals");
    final GICSCodeBean commodityChemsBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("15101010", "Commodity Chemicals");
    final GICSCodeBean diversifiedChemsBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("15101020", "Diversified Chemicals");
    gicsBeans = _hibernateSecurityMasterDao.getGICSCodeBeans();
    assertEquals(5, gicsBeans.size());
    assertTrue(gicsBeans.contains(materialBean));
    assertTrue(gicsBeans.contains(chemicalsBean));
    assertTrue(gicsBeans.contains(commodityChemsBean));
    assertTrue(gicsBeans.contains(diversifiedChemsBean));
    
    //test it doesnt create another bean
    gicsBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("10", "Energy");
    assertEquals(energyId, gicsBean.getId());
    log("getOrCreateGICSBean: finished");
  }
  
  public void getOrCreateDayCountBean() throws Exception {
    log("getOrCreateDayCountBean: started");
    List<DayCountBean> dayCountBeans = _hibernateSecurityMasterDao.getDayCountBeans();
    assertNotNull(dayCountBeans);
    assertTrue(dayCountBeans.isEmpty());
    
    DayCountBean act360Bean = _hibernateSecurityMasterDao.getOrCreateDayCountBean("Act/360");
    assertNotNull(act360Bean);
    assertTrue(act360Bean.getId() > 0);
    assertEquals("Act/360", act360Bean.getName());
    
    dayCountBeans = _hibernateSecurityMasterDao.getDayCountBeans();
    assertNotNull(dayCountBeans);
    assertTrue(dayCountBeans.contains(act360Bean));
    assertEquals(1, dayCountBeans.size());
    Long actual360Id = act360Bean.getId();
    
    DayCountBean act365Bean = _hibernateSecurityMasterDao.getOrCreateDayCountBean("Act/365");
    DayCountBean act365LBean = _hibernateSecurityMasterDao.getOrCreateDayCountBean("Act/365L");
    
    dayCountBeans = _hibernateSecurityMasterDao.getDayCountBeans();
    assertNotNull(dayCountBeans);
    assertEquals(3, dayCountBeans.size());
    assertTrue(dayCountBeans.contains(act360Bean));
    assertTrue(dayCountBeans.contains(act365Bean));
    assertTrue(dayCountBeans.contains(act365LBean));
    
    //test it doesnt create another bean
    DayCountBean dayCountBean = _hibernateSecurityMasterDao.getOrCreateDayCountBean("Act/360");
    assertEquals(actual360Id, dayCountBean.getId());
    log("getOrCreateDaycountBean: finished");
  }
  
  @Test
  public void getOrCreateBusinessDayConventionBean() throws Exception {
    log("getOrCreateBusinessDayConventionBean: started");
    List<BusinessDayConventionBean> businessDayConventionBeans = _hibernateSecurityMasterDao.getBusinessDayConventionBeans();
    assertNotNull(businessDayConventionBeans);
    assertTrue(businessDayConventionBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateBusinessDayConventionBean(TEST_STR);
    
    String dayCountConvention = "Actual/360";
    BusinessDayConventionBean businessDayConventionBean = _hibernateSecurityMasterDao.getOrCreateBusinessDayConventionBean(dayCountConvention);
    assertNotNull(businessDayConventionBean);
    assertTrue(businessDayConventionBean.getId() > 0);
    assertEquals(dayCountConvention, businessDayConventionBean.getName());
    
    businessDayConventionBeans = _hibernateSecurityMasterDao.getBusinessDayConventionBeans();
    assertNotNull(businessDayConventionBeans);
    assertTrue(businessDayConventionBeans.contains(businessDayConventionBean));
    assertEquals(2, businessDayConventionBeans.size());
    
    int previousSize = businessDayConventionBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateBusinessDayConventionBean(dayCountConvention);
    businessDayConventionBeans = _hibernateSecurityMasterDao.getBusinessDayConventionBeans();
    assertNotNull(businessDayConventionBeans);
    assertEquals(previousSize, businessDayConventionBeans.size());
    log("getOrCreateBusinessDayConventionBean: finished");
  }
  
  @Test
  public void getOrCreateFrequencyBean() throws Exception {
    log("getOrCreateFrequencyBean: started");
    List<FrequencyBean> frequencyBeans = _hibernateSecurityMasterDao.getFrequencyBeans();
    assertNotNull(frequencyBeans);
    assertTrue(frequencyBeans.isEmpty());
    
    Frequency annual = SimpleFrequency.ANNUAL;
    FrequencyBean annualBean = _hibernateSecurityMasterDao.getOrCreateFrequencyBean(annual.getConventionName());
    
    frequencyBeans = _hibernateSecurityMasterDao.getFrequencyBeans();
    assertNotNull(frequencyBeans);
    assertTrue(frequencyBeans.size() == 1);
    FrequencyBean frequencyBean = frequencyBeans.get(0);
    assertNotNull(frequencyBean);
    assertEquals(annualBean.getId(), frequencyBean.getId());
    assertEquals(annualBean, frequencyBean);
    Frequency frequency = annualBean.toFrequency();
    assertNotNull(frequency);
    assertEquals(annual.getConventionName(), frequency.getConventionName());
    
    FrequencyBean monthlyBean = _hibernateSecurityMasterDao.getOrCreateFrequencyBean(SimpleFrequency.MONTHLY.getConventionName());
    FrequencyBean quarterlyBean = _hibernateSecurityMasterDao.getOrCreateFrequencyBean(SimpleFrequency.QUARTERLY.getConventionName());
    
    frequencyBeans = _hibernateSecurityMasterDao.getFrequencyBeans();
    assertNotNull(frequencyBeans);
    assertEquals(3, frequencyBeans.size());
    assertTrue(frequencyBeans.contains(annualBean));
    assertTrue(frequencyBeans.contains(monthlyBean));
    assertTrue(frequencyBeans.contains(quarterlyBean));
    
    FrequencyBean bean = _hibernateSecurityMasterDao.getOrCreateFrequencyBean(annual.getConventionName());
    assertEquals(annualBean.getId(), bean.getId());
    
    FrequencyBean invalidBean = _hibernateSecurityMasterDao.getOrCreateFrequencyBean("INVALID");
    assertNotNull(invalidBean);
    try {
      invalidBean.toFrequency();
      fail();
    } catch (Throwable ex) {
      //ok
    }
    log("getOrCreateFrequencyBean: finished");
  }
  
  @Test
  public void getOrCreateCommodityFutureTypeBean() throws Exception {
    log("getOrCreateCommdityFutureTypeBean: started");
    List<CommodityFutureTypeBean> commodityFutureTypeBeans = _hibernateSecurityMasterDao.getCommodityFutureTypeBeans();
    assertNotNull(commodityFutureTypeBeans);
    assertTrue(commodityFutureTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateCommodityFutureTypeBean(TEST_STR);
    
    String commodityFutureType = "Metal";
    CommodityFutureTypeBean commodityFutureBean = _hibernateSecurityMasterDao.getOrCreateCommodityFutureTypeBean(commodityFutureType);
    assertNotNull(commodityFutureBean);
    assertTrue(commodityFutureBean.getId() > 0);
    assertEquals(commodityFutureType, commodityFutureBean.getName());
    
    commodityFutureTypeBeans = _hibernateSecurityMasterDao.getCommodityFutureTypeBeans();
    assertNotNull(commodityFutureTypeBeans);
    assertTrue(commodityFutureTypeBeans.contains(commodityFutureBean));
    assertEquals(2, commodityFutureTypeBeans.size());
    
    int previousSize = commodityFutureTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateCommodityFutureTypeBean(commodityFutureType);
    commodityFutureTypeBeans = _hibernateSecurityMasterDao.getCommodityFutureTypeBeans();
    assertNotNull(commodityFutureTypeBeans);
    assertEquals(previousSize, commodityFutureTypeBeans.size());
    log("getOrCreateCommodityFutureTypeBean: finished");
  }
  
  @Test
  public void getOrCreateBondFutureTypeBean() throws Exception {
    log("getOrCreateBondFutureTypeBean: started");
    List<BondFutureTypeBean> bondFutureTypeBeans = _hibernateSecurityMasterDao.getBondFutureTypeBeans();
    assertNotNull(bondFutureTypeBeans);
    assertTrue(bondFutureTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateBondFutureTypeBean(TEST_STR);
    
    String bondFutureType = "Bond";
    BondFutureTypeBean bondFutureBean = _hibernateSecurityMasterDao.getOrCreateBondFutureTypeBean(bondFutureType);
    assertNotNull(bondFutureBean);
    assertTrue(bondFutureBean.getId() > 0);
    assertEquals(bondFutureType, bondFutureBean.getName());
    
    bondFutureTypeBeans = _hibernateSecurityMasterDao.getBondFutureTypeBeans();
    assertNotNull(bondFutureTypeBeans);
    assertTrue(bondFutureTypeBeans.contains(bondFutureBean));
    assertEquals(2, bondFutureTypeBeans.size());
    
    int previousSize = bondFutureTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateBondFutureTypeBean(bondFutureType);
    bondFutureTypeBeans = _hibernateSecurityMasterDao.getBondFutureTypeBeans();
    assertNotNull(bondFutureTypeBeans);
    assertEquals(previousSize, bondFutureTypeBeans.size());
    log("getOrCreateBondFutureTypeBean: finished");
  }
  
  @Test
  public void getOrCreateUnitNameBean() throws Exception {
    log("getOrCreateUnitNameBean: started");
    List<UnitBean> unitNameBeans = _hibernateSecurityMasterDao.getUnitNameBeans();
    assertNotNull(unitNameBeans);
    assertTrue(unitNameBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateUnitNameBean(TEST_STR);
    
    String unitName = "Unit";
    UnitBean unitNameBean = _hibernateSecurityMasterDao.getOrCreateUnitNameBean(unitName);
    assertNotNull(unitNameBean);
    assertTrue(unitNameBean.getId() > 0);
    assertEquals(unitName, unitNameBean.getName());
    
    unitNameBeans = _hibernateSecurityMasterDao.getUnitNameBeans();
    assertNotNull(unitNameBeans);
    assertTrue(unitNameBeans.contains(unitNameBean));
    assertEquals(2, unitNameBeans.size());
    
    int previousSize = unitNameBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateUnitNameBean(unitName);
    unitNameBeans = _hibernateSecurityMasterDao.getUnitNameBeans();
    assertNotNull(unitNameBeans);
    assertEquals(previousSize, unitNameBeans.size());
    log("getOrCreateUnitNameBean: finished");
  }
  
  @Test
  public void getOrCreateCashRateTypeBean() throws Exception {
    log("getOrCreateCashRateTypeBean: started");
    List<CashRateTypeBean> cashRateTypeBeans = _hibernateSecurityMasterDao.getCashRateTypeBeans();
    assertNotNull(cashRateTypeBeans);
    assertTrue(cashRateTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateCashRateTypeBean(TEST_STR);
    
    String cashRateType = "cash";
    CashRateTypeBean cashRateTypeBean = _hibernateSecurityMasterDao.getOrCreateCashRateTypeBean(cashRateType);
    assertNotNull(cashRateTypeBean);
    assertTrue(cashRateTypeBean.getId() > 0);
    assertEquals(cashRateType, cashRateTypeBean.getName());
    
    cashRateTypeBeans = _hibernateSecurityMasterDao.getCashRateTypeBeans();
    assertNotNull(cashRateTypeBeans);
    assertTrue(cashRateTypeBeans.contains(cashRateTypeBean));
    assertEquals(2, cashRateTypeBeans.size());
    
    int previousSize = cashRateTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateCashRateTypeBean(cashRateType);
    cashRateTypeBeans = _hibernateSecurityMasterDao.getCashRateTypeBeans();
    assertNotNull(cashRateTypeBeans);
    assertEquals(previousSize, cashRateTypeBeans.size());
    log("getOrCreateCashRateTypeBean: finished");
  }
  
  @Test
  public void getOrCreateIssuerTypeBean() throws Exception {
    log("getOrCreateIssuerTypeBean: started");
    List<IssuerTypeBean> issuerTypeBeans = _hibernateSecurityMasterDao.getIssuerTypeBeans();
    assertNotNull(issuerTypeBeans);
    assertTrue(issuerTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateIssuerTypeBean(TEST_STR);
    
    String issuerType = "issuerType";
    IssuerTypeBean issuerTypeBean = _hibernateSecurityMasterDao.getOrCreateIssuerTypeBean(issuerType);
    assertNotNull(issuerTypeBean);
    assertTrue(issuerTypeBean.getId() > 0);
    assertEquals(issuerType, issuerTypeBean.getName());
    
    issuerTypeBeans = _hibernateSecurityMasterDao.getIssuerTypeBeans();
    assertNotNull(issuerTypeBeans);
    assertTrue(issuerTypeBeans.contains(issuerTypeBean));
    assertEquals(2, issuerTypeBeans.size());
    
    int previousSize = issuerTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateIssuerTypeBean(issuerType);
    issuerTypeBeans = _hibernateSecurityMasterDao.getIssuerTypeBeans();
    assertNotNull(issuerTypeBeans);
    assertEquals(previousSize, issuerTypeBeans.size());
    log("getOrCreateIssuerTypeBean: finished");
  }
  
  @Test
  public void getOrCreateMarketBean() throws Exception {
    log("getOrCreateMarketBean: started");
    List<MarketBean> marketBeans = _hibernateSecurityMasterDao.getMarketBeans();
    assertNotNull(marketBeans);
    assertTrue(marketBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateMarketBean(TEST_STR);
    
    String market = "market";
    MarketBean marketBean = _hibernateSecurityMasterDao.getOrCreateMarketBean(market);
    assertNotNull(marketBean);
    assertTrue(marketBean.getId() > 0);
    assertEquals(market, marketBean.getName());
    
    marketBeans = _hibernateSecurityMasterDao.getMarketBeans();
    assertNotNull(marketBeans);
    assertTrue(marketBeans.contains(marketBean));
    assertEquals(2, marketBeans.size());
    
    int previousSize = marketBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateMarketBean(market);
    marketBeans = _hibernateSecurityMasterDao.getMarketBeans();
    assertNotNull(marketBeans);
    assertEquals(previousSize, marketBeans.size());
    log("getOrCreateMarketBean: finished");
  }
  
  @Test
  public void getOrCreateYieldConventionBean() throws Exception {
    log("getOrCreateYieldConventionBean: started");
    List<YieldConventionBean> yieldConventionBeans = _hibernateSecurityMasterDao.getYieldConventionBeans();
    assertNotNull(yieldConventionBeans);
    assertTrue(yieldConventionBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateYieldConventionBean(TEST_STR);
    
    String yieldConvention = "yieldConvention";
    YieldConventionBean yieldConventionBean = _hibernateSecurityMasterDao.getOrCreateYieldConventionBean(yieldConvention);
    assertNotNull(yieldConventionBean);
    assertTrue(yieldConventionBean.getId() > 0);
    assertEquals(yieldConvention, yieldConventionBean.getName());
    
    yieldConventionBeans = _hibernateSecurityMasterDao.getYieldConventionBeans();
    assertNotNull(yieldConventionBeans);
    assertTrue(yieldConventionBeans.contains(yieldConventionBean));
    assertEquals(2, yieldConventionBeans.size());
    
    int previousSize = yieldConventionBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateYieldConventionBean(yieldConvention);
    yieldConventionBeans = _hibernateSecurityMasterDao.getYieldConventionBeans();
    assertNotNull(yieldConventionBeans);
    assertEquals(previousSize, yieldConventionBeans.size());
    log("getOrCreateYieldConventionBean: finished");
  }
  
  @Test
  public void getOrCreateGuaranteeTypeBean() throws Exception {
    log("getOrCreateGuaranteeTypeBean: started");
    List<GuaranteeTypeBean> guaranteeTypeBeans = _hibernateSecurityMasterDao.getGuaranteeTypeBeans();
    assertNotNull(guaranteeTypeBeans);
    assertTrue(guaranteeTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateGuaranteeTypeBean(TEST_STR);
    
    String guaranteeType = "guaranteeType";
    GuaranteeTypeBean guaranteeTypeBean = _hibernateSecurityMasterDao.getOrCreateGuaranteeTypeBean(guaranteeType);
    assertNotNull(guaranteeTypeBean);
    assertTrue(guaranteeTypeBean.getId() > 0);
    assertEquals(guaranteeType, guaranteeTypeBean.getName());
    
    guaranteeTypeBeans = _hibernateSecurityMasterDao.getGuaranteeTypeBeans();
    assertNotNull(guaranteeTypeBeans);
    assertTrue(guaranteeTypeBeans.contains(guaranteeTypeBean));
    assertEquals(2, guaranteeTypeBeans.size());
    
    int previousSize = guaranteeTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateGuaranteeTypeBean(guaranteeType);
    guaranteeTypeBeans = _hibernateSecurityMasterDao.getGuaranteeTypeBeans();
    assertNotNull(guaranteeTypeBeans);
    assertEquals(previousSize, guaranteeTypeBeans.size());
    log("getOrCreateGuaranteeTypeBean: finished");
  }
  
  @Test
  public void getOrCreateCouponTypeBean() throws Exception {
    log("getOrCreateCouponTypeBean: started");
    List<CouponTypeBean> couponTypeBeans = _hibernateSecurityMasterDao.getCouponTypeBeans();
    assertNotNull(couponTypeBeans);
    assertTrue(couponTypeBeans.isEmpty());
    
    _hibernateSecurityMasterDao.getOrCreateCouponTypeBean(TEST_STR);
    
    String couponType = "coupon";
    CouponTypeBean couponTypeBean = _hibernateSecurityMasterDao.getOrCreateCouponTypeBean(couponType);
    assertNotNull(couponTypeBean);
    assertTrue(couponTypeBean.getId() > 0);
    assertEquals(couponType, couponTypeBean.getName());
    
    couponTypeBeans = _hibernateSecurityMasterDao.getCouponTypeBeans();
    assertNotNull(couponTypeBeans);
    assertTrue(couponTypeBeans.contains(couponTypeBean));
    assertEquals(2, couponTypeBeans.size());
    
    int previousSize = couponTypeBeans.size();
    //test it doesnt create another bean
    _hibernateSecurityMasterDao.getOrCreateCouponTypeBean(couponType);
    couponTypeBeans = _hibernateSecurityMasterDao.getCouponTypeBeans();
    assertNotNull(couponTypeBeans);
    assertEquals(previousSize, couponTypeBeans.size());
    log("getOrCreateCouponTypeBean: finished");
  }
  
//  @Test
//  @Ignore
//  public void persistSecurityBean() throws Exception {
//    testEquitySecurityBean();
//    testBondSecurityBean();
//    testFutureSecurityBean();
//    testOptionSecurityBean();
//  }
  
  /**
   * 
   */
  @Test
  public void optionSecurityBean() throws Exception {
    log("optionSecurityBean: started");
    assertNoPersistedOptions();
    
    OptionSecurityType[] optionSecurityTypes = OptionSecurityType.values();
    for (OptionSecurityType optionSecurityType : optionSecurityTypes) {
      final Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, 2010);
      cal.set(Calendar.MONTH, Calendar.OCTOBER);
      cal.set(Calendar.DAY_OF_MONTH, 16);
      Date expiry = cal.getTime();
      
      OptionSecurityBean equityOptionBean = new OptionSecurityBean();
      equityOptionBean.setOptionSecurityType(optionSecurityType);
      equityOptionBean.setOptionType(OptionType.CALL);
      equityOptionBean.setStrike(250.0);
      equityOptionBean.setExpiry(expiry);
      IdentifierBean underlyingIdentifier = identifierToIdentifierBean(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"));
      equityOptionBean.setUnderlying(underlyingIdentifier);
      CurrencyBean usdBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("USD");
      equityOptionBean.setCurrency(usdBean);
      ExchangeBean exchangeBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("XNGS", "NASDAQ/NGS (GLOBAL SELECT MARKET)");
      equityOptionBean.setExchange(exchangeBean);
      equityOptionBean.setPointValue(1.0);
      String displayName = "AAPL 2010-10-16 C 250.0";
      equityOptionBean.setDisplayName(displayName);
      
      cal.set(Calendar.YEAR, 2001);
      Date now = cal.getTime();
      equityOptionBean.setFirstVersion(null);
      equityOptionBean.setLastModifiedBy(null);
      equityOptionBean.setEffectiveDateTime(now);
      equityOptionBean.setDeleted(false);
      equityOptionBean.setLastModifiedDateTime(now);

      SecurityBean bean = _hibernateSecurityMasterDao.persistSecurityBean(equityOptionBean);
            
      //test saved bean has same properties
      assertNotNull(bean);
      assertTrue(bean.getId() > 0);
      assertEquals(displayName, bean.getDisplayName());
      assertEquals(now, bean.getEffectiveDateTime());
      assertEquals(bean, bean.getFirstVersion());
      assertNull(bean.getLastModifiedBy());
      assertEquals(now, bean.getLastModifiedDateTime());
      assertTrue(bean instanceof OptionSecurityBean);
      OptionSecurityBean persistedFirstVersion = (OptionSecurityBean) bean;
      assertEquals(optionSecurityType, persistedFirstVersion.getOptionSecurityType());
      assertEquals(OptionType.CALL, persistedFirstVersion.getOptionType());
      assertTrue(persistedFirstVersion.getStrike() == 250.0);
      assertEquals(expiry, persistedFirstVersion.getExpiry());
      assertTrue(persistedFirstVersion.getPointValue() == 1.0);
      assertEquals(underlyingIdentifier, persistedFirstVersion.getUnderlying());
      assertEquals(usdBean, persistedFirstVersion.getCurrency());
      assertEquals(exchangeBean, persistedFirstVersion.getExchange());
      
      //add another version of AAPL Option
      cal.set(Calendar.YEAR, 2002);
      Date later = cal.getTime();
      equityOptionBean = new OptionSecurityBean();
      equityOptionBean.setOptionSecurityType(optionSecurityType);
      equityOptionBean.setOptionType(OptionType.CALL);
      equityOptionBean.setStrike(250.0);
      equityOptionBean.setExpiry(expiry);
      underlyingIdentifier = identifierToIdentifierBean(Identifier.of("BLOOMBERG_TICKER", "AAPL US Equity"));
      equityOptionBean.setUnderlying(underlyingIdentifier);
      //change currency
      CurrencyBean gbpBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("GBP");
      equityOptionBean.setCurrency(gbpBean);
      equityOptionBean.setExchange(exchangeBean);
      equityOptionBean.setPointValue(1.0);
      equityOptionBean.setDisplayName(displayName);
     
      equityOptionBean.setFirstVersion(persistedFirstVersion);
      equityOptionBean.setLastModifiedBy(null);
      equityOptionBean.setEffectiveDateTime(later);
      equityOptionBean.setDeleted(false);
      equityOptionBean.setLastModifiedDateTime(later);
      
      bean = _hibernateSecurityMasterDao.persistSecurityBean(equityOptionBean);
      
      //test saved 2nd version bean has same properties
      assertNotNull(bean);
      assertTrue(bean.getId() > 0);
      assertEquals(displayName, bean.getDisplayName());
      assertEquals(later, bean.getEffectiveDateTime());
      assertEquals(persistedFirstVersion, bean.getFirstVersion());
      assertNull(bean.getLastModifiedBy());
      assertEquals(later, bean.getLastModifiedDateTime());

      assertTrue(bean instanceof OptionSecurityBean);
      OptionSecurityBean persistedSecondVersion = (OptionSecurityBean) bean;
      
      assertEquals(optionSecurityType, persistedSecondVersion.getOptionSecurityType());
      assertEquals(OptionType.CALL, persistedSecondVersion.getOptionType());
      assertTrue(persistedSecondVersion.getStrike() == 250.0);
      assertEquals(expiry, persistedSecondVersion.getExpiry());
      assertTrue(persistedSecondVersion.getPointValue() == 1.0);
      assertEquals(underlyingIdentifier, persistedSecondVersion.getUnderlying());
      assertEquals(gbpBean, persistedSecondVersion.getCurrency());
      assertEquals(exchangeBean, persistedSecondVersion.getExchange());
      
      List<OptionSecurityBean> optionSecurityBeans = _hibernateSecurityMasterDao.getOptionSecurityBeans();
      assertNotNull(optionSecurityBeans);
      assertTrue(optionSecurityBeans.contains(persistedFirstVersion));
      assertTrue(optionSecurityBeans.contains(persistedSecondVersion));
    }
    
    //get all beans so far and test
    List<OptionSecurityBean> optionSecurityBeans = _hibernateSecurityMasterDao.getOptionSecurityBeans();
    assertNotNull(optionSecurityBeans);
    assertTrue(optionSecurityBeans.size() == optionSecurityTypes.length * 2);
    log("optionSecurityBean: finished");
  }

  @Test
  public void futureSecurityBean() throws Exception {
    log("futureSecurityBean: started");
    // TODO Auto-generated method stub
    log("futureSecurityBean: finished");
  }

  @Test
  public void bondSecurityBean() {
//    Assert.fail();
  }

  @Test
  public void equitySecurityBean() throws Exception {
    log("equitySecurityBean: started");
    assertNoPersistedEquites();
    
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2000);
    Date now = cal.getTime();
   
    //unsaved aaplEquityBean
    EquitySecurityBean equityBean = makeAAPLEquitySecurityBean(_hibernateSecurityMasterDao, null, null, now, false, now);
    ExchangeBean exchangeBean = equityBean.getExchange();
    CurrencyBean usdBean = equityBean.getCurrency();
    GICSCodeBean gicsCodeBean = equityBean.getGICSCode();
    String companyName = equityBean.getCompanyName();
    String displayName = equityBean.getDisplayName();
    
    _hibernateSecurityMasterDao.persistSecurityBean(equityBean);
    List<EquitySecurityBean> equitySecurityBeans = _hibernateSecurityMasterDao.getEquitySecurityBeans();
    assertNotNull(equitySecurityBeans);
    assertEquals(1, equitySecurityBeans.size());
    EquitySecurityBean persistedFirstVersion = equitySecurityBeans.get(0);
    
    //test saved bean has same properties
    assertNotNull(persistedFirstVersion);
    assertTrue(persistedFirstVersion.getId() > 0);
    assertEquals(displayName, persistedFirstVersion.getDisplayName());
    assertEquals(now, persistedFirstVersion.getEffectiveDateTime());
    assertEquals(persistedFirstVersion, persistedFirstVersion.getFirstVersion());
    assertNull(persistedFirstVersion.getLastModifiedBy());
    assertEquals(now, persistedFirstVersion.getLastModifiedDateTime());
    assertEquals(exchangeBean, persistedFirstVersion.getExchange());
    assertEquals(companyName, persistedFirstVersion.getCompanyName());
    assertEquals(usdBean, persistedFirstVersion.getCurrency());
    assertEquals(gicsCodeBean, persistedFirstVersion.getGICSCode());
    
    //saved another version of AAPL
    cal.set(Calendar.YEAR, 2002);
    Date later = cal.getTime();
    equityBean = makeAAPLEquitySecurityBean(_hibernateSecurityMasterDao, persistedFirstVersion, null, later, false, later);
    String appleNewName = "APPLE 2";
    equityBean.setCompanyName(appleNewName);
    equityBean.setDisplayName(appleNewName);
    //change the currency
    CurrencyBean gbpBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("GBP");
    equityBean.setCurrency(gbpBean);
    
    SecurityBean bean = _hibernateSecurityMasterDao.persistSecurityBean(equityBean);
    
    //test saved bean has same properties
    assertNotNull(bean);
    assertTrue(bean.getId() > 0);
    
    assertEquals(appleNewName, bean.getDisplayName());
    assertEquals(later, bean.getEffectiveDateTime());
    assertEquals(persistedFirstVersion, bean.getFirstVersion());
    assertNull(bean.getLastModifiedBy());
    assertEquals(later, bean.getLastModifiedDateTime());
    assertTrue(bean instanceof EquitySecurityBean);
    EquitySecurityBean persistedSecondVersion = (EquitySecurityBean) bean;
    assertEquals(exchangeBean, persistedSecondVersion.getExchange());
    assertEquals(appleNewName, persistedSecondVersion.getCompanyName());
    assertEquals(gbpBean, persistedSecondVersion.getCurrency());
    assertEquals(gicsCodeBean, persistedSecondVersion.getGICSCode());
       
    //test get all equity beans
    equitySecurityBeans = _hibernateSecurityMasterDao.getEquitySecurityBeans();
    assertNotNull(equitySecurityBeans);
    assertEquals(2, equitySecurityBeans.size());
    assertTrue(equitySecurityBeans.contains(persistedFirstVersion));
    assertTrue(equitySecurityBeans.contains(persistedSecondVersion));
    
    //test get all version
    List<EquitySecurityBean> allVersions = _hibernateSecurityMasterDao.getAllVersionsOfEquitySecurityBean((EquitySecurityBean) persistedFirstVersion);
    assertNotNull(allVersions);
    assertTrue(allVersions.size() == 2);
    assertTrue(allVersions.contains(persistedFirstVersion));
    assertTrue(allVersions.contains(persistedSecondVersion));
    
    //test get current bean
    EquitySecurityBean firstVersion = (EquitySecurityBean) persistedFirstVersion;
    EquitySecurityBean secondVersion = (EquitySecurityBean) persistedSecondVersion;
    cal.set(Calendar.YEAR, 1990);
    EquitySecurityBean result = _hibernateSecurityMasterDao.getCurrentLiveEquitySecurityBean(cal.getTime(), firstVersion);
    assertNull(result);
    
    
    cal.set(Calendar.YEAR, 2001);
    result = _hibernateSecurityMasterDao.getCurrentLiveEquitySecurityBean(cal.getTime(), firstVersion);
    assertNotNull(result);
    assertEquals(firstVersion, result);
    result = _hibernateSecurityMasterDao.getCurrentLiveEquitySecurityBean(cal.getTime(), firstVersion.getExchange(), firstVersion.getCompanyName(), firstVersion.getCurrency());
    assertEquals(firstVersion, result);
    
    result = _hibernateSecurityMasterDao.getCurrentEquitySecurityBean(cal.getTime(), firstVersion);
    assertNotNull(result);
    assertEquals(firstVersion, result);
    result = _hibernateSecurityMasterDao.getCurrentEquitySecurityBean(cal.getTime(), firstVersion.getExchange(), firstVersion.getCompanyName(), firstVersion.getCurrency());
    assertEquals(firstVersion, result);
    
    cal.set(Calendar.YEAR, 2003);
    result = _hibernateSecurityMasterDao.getCurrentLiveEquitySecurityBean(cal.getTime(), firstVersion);
    assertNotNull(result);
    assertEquals(secondVersion, result);
    result = _hibernateSecurityMasterDao.getCurrentLiveEquitySecurityBean(cal.getTime(), secondVersion.getExchange(), secondVersion.getCompanyName(), secondVersion.getCurrency());
    assertEquals(secondVersion, result);
    
    result = _hibernateSecurityMasterDao.getCurrentEquitySecurityBean(cal.getTime(), firstVersion);
    assertNotNull(result);
    assertEquals(secondVersion, result);
    result = _hibernateSecurityMasterDao.getCurrentEquitySecurityBean(cal.getTime(), secondVersion.getExchange(), secondVersion.getCompanyName(), secondVersion.getCurrency());
    assertEquals(secondVersion, result);
    log("equitySecurityBean: finished");
  }

  @Test
  public void associateOrUpdateIdentifierWithSecurity() {
    log("associateOrUpdateIdentifierWithSecurity: started");
    //test associating security with identifiers
    assertNoPersistedEquites();
    int randomSize = 2;
    addRandomEquityBean(randomSize);
    
    final Calendar cal = Calendar.getInstance();
    cal.set(Calendar.YEAR, 2000);
    Date now = cal.getTime();
   
    //unsaved aaplEquityBean
    EquitySecurityBean equityBean = makeAAPLEquitySecurityBean(_hibernateSecurityMasterDao, null, null, now, false, now);
    ExchangeBean exchangeBean = equityBean.getExchange();
    CurrencyBean usdBean = equityBean.getCurrency();
    GICSCodeBean gicsCodeBean = equityBean.getGICSCode();
    String companyName = equityBean.getCompanyName();
    String displayName = equityBean.getDisplayName();
    
    SecurityBean persistSecurityBean = _hibernateSecurityMasterDao.persistSecurityBean(equityBean);
    
    //test saved bean has same properties
    assertNotNull(persistSecurityBean);
    assertTrue(persistSecurityBean.getId() > 0);
    assertEquals(displayName, persistSecurityBean.getDisplayName());
    assertEquals(now, persistSecurityBean.getEffectiveDateTime());
    assertEquals(persistSecurityBean, persistSecurityBean.getFirstVersion());
    assertNull(persistSecurityBean.getLastModifiedBy());
    assertEquals(now, persistSecurityBean.getLastModifiedDateTime());
    assertTrue(persistSecurityBean instanceof EquitySecurityBean);
    EquitySecurityBean persistedAAPLBean = (EquitySecurityBean) persistSecurityBean;
    assertEquals(exchangeBean, persistedAAPLBean.getExchange());
    assertEquals(companyName, persistedAAPLBean.getCompanyName());
    assertEquals(usdBean, persistedAAPLBean.getCurrency());
    assertEquals(gicsCodeBean, persistedAAPLBean.getGICSCode());
    
    //add identifiers
    Identifier aaplTicker = new Identifier(IdentificationScheme.BLOOMBERG_TICKER, "AAPL US Equity");
    Identifier aaplBuid = new Identifier(IdentificationScheme.BLOOMBERG_BUID, "EQ0010169500001000");
    Identifier cusip = new Identifier(IdentificationScheme.CUSIP, "037833100");
    Identifier isin = new Identifier(IdentificationScheme.ISIN, "US0378331005");
    Identifier sedol1 = new Identifier(IdentificationScheme.SEDOL1, "2046251");
    
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(now, aaplTicker, persistSecurityBean);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(now, aaplBuid, persistSecurityBean);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(now, cusip, persistSecurityBean);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(now, isin, persistSecurityBean);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(now, sedol1, persistSecurityBean);
    
    //search using identifiers
    IdentifierBundle aaplIdentifierBundle = new IdentifierBundle(aaplTicker, aaplBuid, cusip, isin, sedol1);
    SecurityBean securityBean = _hibernateSecurityMasterDao.getSecurityBean(now, aaplIdentifierBundle);
    assertNotNull(securityBean);
    assertEquals(persistedAAPLBean, securityBean);
    //search using individual identifier
    Set<Identifier> identifiers = aaplIdentifierBundle.getIdentifiers();
    for (Identifier identifier : identifiers) {
      SecurityBean securityBeanByIdentifier = _hibernateSecurityMasterDao.getSecurityBean(now, new IdentifierBundle(identifier));
      assertNotNull(securityBeanByIdentifier);
      assertEquals(persistedAAPLBean, securityBeanByIdentifier);
    }
    
    //saved another version of AAPL
    cal.set(Calendar.YEAR, 2001);
    Date later = cal.getTime();
    EquitySecurityBean secondVersionBean = makeAAPLEquitySecurityBean(_hibernateSecurityMasterDao, persistedAAPLBean, null, later, false, later);
    secondVersionBean.setCompanyName("APPLE 2");
    secondVersionBean.setDisplayName("APPLE 2");
    secondVersionBean.setCurrency(persistedAAPLBean.getCurrency());
    secondVersionBean.setExchange(persistedAAPLBean.getExchange());
    secondVersionBean.setGICSCode(persistedAAPLBean.getGICSCode());
    
    SecurityBean persistedSecondVersion = _hibernateSecurityMasterDao.persistSecurityBean(secondVersionBean);
    
    //test saved bean has same properties
    assertNotNull(persistedSecondVersion);
    assertTrue(persistedSecondVersion.getId() > 0);
    assertEquals("APPLE 2", persistedSecondVersion.getDisplayName());
    assertEquals(later, persistedSecondVersion.getEffectiveDateTime());
    assertEquals(persistSecurityBean, persistedSecondVersion.getFirstVersion());
    assertNull(persistedSecondVersion.getLastModifiedBy());
    assertEquals(later, persistedSecondVersion.getLastModifiedDateTime());
    assertTrue(persistedSecondVersion instanceof EquitySecurityBean);
    persistedAAPLBean = (EquitySecurityBean) persistedSecondVersion;
    assertEquals(exchangeBean, persistedAAPLBean.getExchange());
    assertEquals("APPLE 2", persistedAAPLBean.getCompanyName());
    assertEquals(usdBean, persistedAAPLBean.getCurrency());
    assertEquals(gicsCodeBean, persistedAAPLBean.getGICSCode());
    
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(later, aaplTicker, persistedSecondVersion);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(later, aaplBuid, persistedSecondVersion);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(later, cusip, persistedSecondVersion);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(later, isin, persistedSecondVersion);
    _hibernateSecurityMasterDao.associateOrUpdateIdentifierWithSecurity(later, sedol1, persistedSecondVersion);
       
    //get all equity beans
    List<EquitySecurityBean> equitySecurityBeans = _hibernateSecurityMasterDao.getEquitySecurityBeans();
    assertNotNull(equitySecurityBeans);
    assertTrue(equitySecurityBeans.contains(persistSecurityBean));
    assertTrue(equitySecurityBeans.contains(persistedSecondVersion));
    log("associateOrUpdateIdentifierWithSecurity: finished");
  }
  
  @Test
  public void testDomainSpecificIdentifierAssociationDateRanges() {
    log("testDomainSpecificIdentifierAssociationDateRanges: started");
    final ExchangeBean tpxBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("TPX", "Topix");
    final CurrencyBean jpyBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("JPY");
    final GICSCodeBean banksBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean("4010", "Banks");
    final Calendar cal2000 = Calendar.getInstance();
    cal2000.set(Calendar.YEAR, 2000);
    final Calendar cal2001 = Calendar.getInstance();
    cal2001.set(Calendar.YEAR, 2001);
    final Calendar cal2002 = Calendar.getInstance();
    cal2002.set(Calendar.YEAR, 2002);
    final Calendar cal2003 = Calendar.getInstance();
    cal2003.set(Calendar.YEAR, 2003);
    final Calendar cal2004 = Calendar.getInstance();
    cal2004.set(Calendar.YEAR, 2004);
    final EquitySecurityBean nomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean(_hibernateSecurityMasterDao, cal2000.getTime(), false, cal2000.getTime(), null, null, "Nomura",
        tpxBean, "Nomura", jpyBean, banksBean);
    final EquitySecurityBean notNomuraBean = EquitySecurityBeanOperation.INSTANCE.createBean(_hibernateSecurityMasterDao, cal2003.getTime(), false, cal2003.getTime(), null, null,
        "Something else", tpxBean, "Not Nomura", jpyBean, banksBean);
    final IdentifierAssociationBean dsiab1 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2001.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
    IdentifierAssociationBean dsiab2 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
    assertEquals(dsiab1.getId(), dsiab2.getId());
    IdentifierAssociationBean dsiab3 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime(), "BLOOMBERG", "1311 Equity", notNomuraBean);
    if (dsiab1.getId() == dsiab3.getId())
      fail("different association should have been created");
    assertNotNull(dsiab3.getValidStartDate());
    assertNull(dsiab3.getValidEndDate());
    final IdentifierAssociationBean dsiab4 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2004.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
    if (dsiab1.getId() == dsiab4.getId())
      fail("different association should have been created");
    if (dsiab3.getId() == dsiab4.getId())
      fail("different association should have been created");
    assertNotNull(dsiab4.getValidStartDate());
    assertNull(dsiab4.getValidEndDate());

    dsiab2 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2002.getTime(), "BLOOMBERG", "1311 Equity", nomuraBean);
    dsiab3 = _hibernateSecurityMasterDao.getCreateOrUpdateIdentifierAssociationBean(cal2003.getTime(), "BLOOMBERG", "1311 Equity", notNomuraBean);
    assertEquals(dsiab1.getId(), dsiab2.getId());
    assertNull(dsiab2.getValidStartDate());
    assertNotNull(dsiab2.getValidEndDate());
    assertNotNull(dsiab3.getValidStartDate());
    assertNotNull(dsiab3.getValidEndDate());
    assertNotNull(dsiab4.getValidStartDate());
    assertNull(dsiab4.getValidEndDate());
    assertEquals(dsiab2.getValidEndDate(), dsiab3.getValidStartDate());
    assertEquals(dsiab3.getValidEndDate(), dsiab4.getValidStartDate());
    log("testDomainSpecificIdentifierAssociationDateRanges: finished");
    log(""); // tell watchDog thread to quit.
  }
  

  /**
   * @param randomSize 
   * 
   */
  private List<SecurityBean> addRandomEquityBean(int randomSize) {
    List<SecurityBean> result = new ArrayList<SecurityBean>();
    for (int i = 0; i < randomSize; i++) {
      final Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, 2001);
      Date date = cal.getTime();
      
      EquitySecurityBean firstVersion = new EquitySecurityBean();
      firstVersion.setCompanyName("CN" + _random.nextInt());
      ExchangeBean exchangeBean = _hibernateSecurityMasterDao.getOrCreateExchangeBean("EXC" + nextRandomInt(), "EXN" + nextRandomInt());
      firstVersion.setExchange(exchangeBean);
      CurrencyBean currencyBean = _hibernateSecurityMasterDao.getOrCreateCurrencyBean("CUR" + nextRandomInt());
      firstVersion.setCurrency(currencyBean);
      String gics = String.valueOf(nextRandomInt());
      if (gics.length() > 7) {
        gics = gics.substring(0, 7);
      }
      GICSCodeBean gicsCodeBean = _hibernateSecurityMasterDao.getOrCreateGICSCodeBean(gics, "GICSD" + nextRandomInt());
      firstVersion.setGICSCode(gicsCodeBean);
      firstVersion.setDisplayName("DSP" + nextRandomInt());

      firstVersion.setFirstVersion(null);
      firstVersion.setLastModifiedBy(null);
      firstVersion.setEffectiveDateTime(date);
      firstVersion.setDeleted(false);
      firstVersion.setLastModifiedDateTime(date);
      
      SecurityBean persistedFirstVersion = _hibernateSecurityMasterDao.persistSecurityBean(firstVersion);
      result.add(persistedFirstVersion);
      
      cal.set(Calendar.YEAR, 2002);
      Date later = cal.getTime();
      EquitySecurityBean secondVersion = new EquitySecurityBean();
      //change company name
      secondVersion.setCompanyName("CN" + _random.nextInt());
      secondVersion.setExchange(exchangeBean);
      secondVersion.setCurrency(currencyBean);
      secondVersion.setGICSCode(gicsCodeBean);
      secondVersion.setDisplayName("DSP" + nextRandomInt());
      
      secondVersion.setFirstVersion(persistedFirstVersion);
      secondVersion.setLastModifiedBy(null);
      secondVersion.setEffectiveDateTime(later);
      secondVersion.setDeleted(false);
      secondVersion.setLastModifiedDateTime(later);

      SecurityBean persistedSecondVersion = _hibernateSecurityMasterDao.persistSecurityBean(secondVersion);
      result.add(persistedSecondVersion);
    }
    return result;
  }

  /**
   * @return
   */
  private int nextRandomInt() {
    return Math.abs(_random.nextInt());
  }

  /**
   * 
   */
  private void assertNoPersistedEquites() {
    List<EquitySecurityBean> equitySecurityBeans = _hibernateSecurityMasterDao.getEquitySecurityBeans();
    assertNotNull(equitySecurityBeans);
    assertTrue(equitySecurityBeans.isEmpty());
    assertNoPersistedDependentBeans();
  }

  /**
   * 
   */
  private void assertNoPersistedDependentBeans() {
    List<ExchangeBean> exchangeBeans = _hibernateSecurityMasterDao.getExchangeBeans();
    assertNotNull(exchangeBeans);
    assertTrue(exchangeBeans.isEmpty());
    
    List<CurrencyBean> currencyBeans = _hibernateSecurityMasterDao.getCurrencyBeans();
    assertNotNull(currencyBeans);
    assertTrue(currencyBeans.isEmpty());
    
    List<GICSCodeBean> gicsCodeBeans = _hibernateSecurityMasterDao.getGICSCodeBeans();
    assertNotNull(gicsCodeBeans);
    assertTrue(gicsCodeBeans.isEmpty());
  }
  
  /**
   * 
   */
  private void assertNoPersistedOptions() {
    List<OptionSecurityBean> optionSecurityBeans = _hibernateSecurityMasterDao.getOptionSecurityBeans();
    assertNotNull(optionSecurityBeans);
    assertTrue(optionSecurityBeans.isEmpty());
    assertNoPersistedDependentBeans();
  }
  

}
