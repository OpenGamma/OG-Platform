/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.security.SecurityMasterTestCase;
import com.opengamma.financial.security.SecurityMasterTestCaseMethods;
import com.opengamma.financial.security.db.bond.BondSecurityBean;
import com.opengamma.financial.security.db.bond.CouponTypeBean;
import com.opengamma.financial.security.db.bond.GuaranteeTypeBean;
import com.opengamma.financial.security.db.bond.IssuerTypeBean;
import com.opengamma.financial.security.db.bond.MarketBean;
import com.opengamma.financial.security.db.bond.YieldConventionBean;
import com.opengamma.financial.security.db.equity.EquitySecurityBean;
import com.opengamma.financial.security.db.equity.GICSCodeBean;
import com.opengamma.financial.security.db.future.BondFutureTypeBean;
import com.opengamma.financial.security.db.future.CashRateTypeBean;
import com.opengamma.financial.security.db.future.CommodityFutureTypeBean;
import com.opengamma.financial.security.db.future.FutureBundleBean;
import com.opengamma.financial.security.db.future.FutureSecurityBean;
import com.opengamma.financial.security.db.future.UnitBean;
import com.opengamma.financial.security.db.option.OptionSecurityBean;
import com.opengamma.util.test.HibernateTest;

/**
 * Test HibernateSecurityMaster.
 */
public class HibernateSecurityMasterTest extends HibernateTest implements SecurityMasterTestCaseMethods {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(HibernateSecurityMasterTest.class);

  private SecurityMasterTestCase _testCase;

  /**
   * @param databaseType
   * @param databaseVersion
   */
  public HibernateSecurityMasterTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running test for database={} version={}", databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] { BondFutureTypeBean.class, BondSecurityBean.class, BusinessDayConventionBean.class, CashRateTypeBean.class, CommodityFutureTypeBean.class,
        CouponTypeBean.class, CurrencyBean.class, DayCountBean.class, EquitySecurityBean.class, ExchangeBean.class, FrequencyBean.class, FutureBundleBean.class,
        FutureSecurityBean.class, GICSCodeBean.class, GuaranteeTypeBean.class, IdentifierAssociationBean.class, IssuerTypeBean.class, MarketBean.class, OptionSecurityBean.class,
        SecurityBean.class, UnitBean.class, YieldConventionBean.class };
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
    _testCase = new SecurityMasterTestCase(createSecurityMaster());
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  private HibernateSecurityMaster createSecurityMaster() {
    HibernateSecurityMaster secMaster = new HibernateSecurityMaster();
    secMaster.setSessionFactory(getSessionFactory());
    s_logger.debug("SecMaster initialization complete {}", secMaster);
    return secMaster;
  }

  @Override
  @Test
  public void aaplEquityByBbgTicker() throws Exception {
    _testCase.aaplEquityByBbgTicker();
  }

  @Override
  @Test
  public void aaplEquityByUniqueIdentifier() throws Exception {
    _testCase.aaplEquityByUniqueIdentifier();
  }

  @Override
  @Test
  public void agricultureFuture() throws Exception {
    _testCase.agricultureFuture();
  }

  @Override
  @Test
  public void apvEquityOptionByBbgTicker() throws Exception {
    _testCase.apvEquityOptionByBbgTicker();
  }

  @Override
  @Test
  public void currencyFuture() throws Exception {
    _testCase.currencyFuture();
  }

  @Override
  @Test
  public void energyFuture() throws Exception {
    _testCase.energyFuture();
  }

  @Override
  @Test
  public void euroBondFuture() throws Exception {
    _testCase.euroBondFuture();
  }

  @Override
  @Test
  public void governmentBondSecurityBean() {
    _testCase.governmentBondSecurityBean();
  }

  @Override
  @Test
  public void indexFuture() throws Exception {
    _testCase.indexFuture();
  }

  @Override
  @Test
  public void interestRateFuture() throws Exception {
    _testCase.interestRateFuture();
  }

  @Override
  @Test
  public void metalFuture() throws Exception {
    _testCase.metalFuture();
  }

  @Override
  @Test
  public void spxIndexOptionByBbgTicker() throws Exception {
    _testCase.spxIndexOptionByBbgTicker();
  }

  @Override
  @Test
  public void spxIndexOptionByBbgUnique() throws Exception {
    _testCase.spxIndexOptionByBbgUnique();
  }

  @Override
  @Test
  public void testGovernmentBondSecurityBean() {
    _testCase.testGovernmentBondSecurityBean();
  }

  @Override
  @Test
  public void update() throws Exception {
    _testCase.update();
  }

}
