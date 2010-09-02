/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.opengamma.financial.security.db.BusinessDayConventionBean;
import com.opengamma.financial.security.db.CurrencyBean;
import com.opengamma.financial.security.db.DayCountBean;
import com.opengamma.financial.security.db.ExchangeBean;
import com.opengamma.financial.security.db.FrequencyBean;
import com.opengamma.financial.security.db.IdentifierAssociationBean;
import com.opengamma.financial.security.db.SecurityBean;
import com.opengamma.financial.security.db.bond.BondSecurityBean;
import com.opengamma.financial.security.db.bond.CouponTypeBean;
import com.opengamma.financial.security.db.bond.GuaranteeTypeBean;
import com.opengamma.financial.security.db.bond.IssuerTypeBean;
import com.opengamma.financial.security.db.bond.MarketBean;
import com.opengamma.financial.security.db.bond.YieldConventionBean;
import com.opengamma.financial.security.db.cash.CashSecurityBean;
import com.opengamma.financial.security.db.equity.EquitySecurityBean;
import com.opengamma.financial.security.db.equity.GICSCodeBean;
import com.opengamma.financial.security.db.fra.FRASecurityBean;
import com.opengamma.financial.security.db.future.BondFutureTypeBean;
import com.opengamma.financial.security.db.future.CashRateTypeBean;
import com.opengamma.financial.security.db.future.CommodityFutureTypeBean;
import com.opengamma.financial.security.db.future.FutureBundleBean;
import com.opengamma.financial.security.db.future.FutureSecurityBean;
import com.opengamma.financial.security.db.future.UnitBean;
import com.opengamma.financial.security.db.option.OptionSecurityBean;
import com.opengamma.financial.security.db.swap.SwapSecurityBean;
import com.opengamma.financial.security.master.SecurityMasterTestCase;
import com.opengamma.financial.security.master.SecurityTestCaseMethods;
import com.opengamma.util.test.HibernateTest;

/**
 * Test DbSecurityMaster.
 */
public class DbSecurityMasterRandomTest extends HibernateTest implements SecurityTestCaseMethods {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterRandomTest.class);

  private SecurityMasterTestCase _testCase;

  /**
   * @param databaseType
   * @param databaseVersion
   */
  public DbSecurityMasterRandomTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running test for database={} version={}", databaseType, databaseVersion);
  }

  @Override
  public Class<?>[] getHibernateMappingClasses() {
    return new Class<?>[] {BondFutureTypeBean.class, BondSecurityBean.class, BusinessDayConventionBean.class, CashRateTypeBean.class, CommodityFutureTypeBean.class, CouponTypeBean.class,
        CurrencyBean.class, DayCountBean.class, EquitySecurityBean.class, ExchangeBean.class, FrequencyBean.class, FutureBundleBean.class, FutureSecurityBean.class, GICSCodeBean.class,
        GuaranteeTypeBean.class, IdentifierAssociationBean.class, IssuerTypeBean.class, MarketBean.class, OptionSecurityBean.class, SecurityBean.class, UnitBean.class, YieldConventionBean.class,
        CashSecurityBean.class, SwapSecurityBean.class, FRASecurityBean.class };
  }
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    super.setUp();
//    final RegionMaster regionRepository = new InMemoryRegionRepository();
//    RegionFileReader.populateMaster(regionRepository, new File(RegionFileReader.REGIONS_FILE_PATH));
    
    final String contextLocation = "config/test-master-context.xml";
    final ApplicationContext context = new FileSystemXmlApplicationContext(contextLocation);
    
    DbSecurityMaster secMaster = (DbSecurityMaster) context.getBean(getDatabaseType()+"DbSecurityMaster");
    s_logger.debug("SecMaster initialization complete {}", secMaster);
    _testCase = new SecurityMasterTestCase(secMaster);
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  // SecurityMasterTestCaseMethods

  @Override
  @Test
  public void testAgricultureFutureSecurity() {
    _testCase.testAgricultureFutureSecurity();
  }

  @Override
  @Test
  public void testBondFutureSecurity() {
    _testCase.testBondFutureSecurity();
  }

  @Override
  @Test
  public void testBondOptionSecurity() {
    _testCase.testBondOptionSecurity();
  }

  @Override
  @Test
  public void testCashSecurity() {
    _testCase.testCashSecurity();
  }

  @Override
  @Test
  public void testCorporateBondSecurity() {
    _testCase.testCorporateBondSecurity();
  }

  @Override
  @Test
  public void testEnergyFutureSecurity() {
    _testCase.testEnergyFutureSecurity();
  }

  @Override
  @Test
  public void testEquityOptionSecurity() {
    _testCase.testEquityOptionSecurity();
  }

  @Override
  @Test
  public void testEquitySecurity() {
    _testCase.testEquitySecurity();
  }

  @Override
  @Test
  public void testFRASecurity() {
    _testCase.testFRASecurity();
  }

  @Override
  @Test
  public void testFXFutureSecurity() {
    _testCase.testFXFutureSecurity();
  }

  @Override
  @Test
  public void testFXOptionSecurity() {
    _testCase.testFXOptionSecurity();
  }

  @Override
  @Test
  public void testForwardSwapSecurity() {
    _testCase.testForwardSwapSecurity();
  }

  @Override
  @Test
  public void testFutureOptionSecurity() {
    _testCase.testFutureOptionSecurity();
  }

  @Override
  @Test
  public void testGovernmentBondSecurity() {
    _testCase.testGovernmentBondSecurity();
  }

  @Override
  @Test
  public void testIndexFutureSecurity() {
    _testCase.testIndexFutureSecurity();
  }

  @Override
  @Test
  public void testInterestRateFutureSecurity() {
    _testCase.testInterestRateFutureSecurity();
  }

  @Override
  @Test
  public void testMetalFutureSecurity() {
    _testCase.testMetalFutureSecurity();
  }

  @Override
  @Test
  public void testMunicipalBondSecurity() {
    _testCase.testMunicipalBondSecurity();
  }

  @Override
  @Test
  public void testOptionOptionSecurity() {
    _testCase.testOptionOptionSecurity();
  }

  @Override
  @Test
  public void testStockFutureSecurity() {
    _testCase.testStockFutureSecurity();
  }

  @Override
  @Test
  public void testSwapOptionSecurity() {
    _testCase.testSwapOptionSecurity();
  }

  @Override
  @Test
  public void testSwapSecurity() {
    _testCase.testSwapSecurity();
  }

}
