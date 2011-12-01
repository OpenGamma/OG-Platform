/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.masterdb.security.test.SecurityTestCaseMethods;
import com.opengamma.util.test.DbTest;

/**
 * Test DbSecurityMaster.
 */
public class DbSecurityMasterDetailProviderRandomTest extends DbTest implements SecurityTestCaseMethods {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterDetailProviderRandomTest.class);

  private SecurityMasterTestCase _testCase;

  /**
   * @param databaseType
   * @param databaseVersion
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbSecurityMasterDetailProviderRandomTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running test for database={} version={}", databaseType, databaseVersion);
  }

  /**
   * @throws java.lang.Exception
   */
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    DbSecurityMaster secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
    s_logger.debug("SecMaster initialization complete {}", secMaster);
    _testCase = new SecurityMasterTestCase(secMaster);
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterMethod
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
  public void testEquityBarrierOptionSecurity() {
    _testCase.testEquityBarrierOptionSecurity();
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
  public void testNonDeliverableFXOptionSecurity() {
    _testCase.testNonDeliverableFXOptionSecurity();
  } 
  
  @Override
  @Test
  public void testFXBarrierOptionSecurity() {
    _testCase.testFXBarrierOptionSecurity();
  }

  @Override
  @Test
  public void testForwardSwapSecurity() {
    _testCase.testForwardSwapSecurity();
  }

  @Override
  @Test
  public void testIRFutureOptionSecurity() {
    _testCase.testIRFutureOptionSecurity();
  }

  @Override
  @Test
  public void testEquityIndexDividendFutureOptionSecurity() {
    _testCase.testEquityIndexDividendFutureOptionSecurity();
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
  public void testStockFutureSecurity() {
    _testCase.testStockFutureSecurity();
  }

  @Override
  @Test
  public void testSwaptionSecurity() {
    _testCase.testSwaptionSecurity();
  }

  @Override
  @Test
  public void testSwapSecurity() {
    _testCase.testSwapSecurity();
  }

  @Override
  @Test
  public void testEquityIndexOptionSecurity() {
    _testCase.testEquityIndexOptionSecurity();
  }

  @Override
  @Test
  public void testFXSecurity() {
    _testCase.testFXSecurity();
  }

  @Override
  @Test
  public void testFXForwardSecurity() {
    _testCase.testFXForwardSecurity();
  }

  @Override
  @Test
  public void testNonDeliverableFXForwardSecurity() {
    _testCase.testNonDeliverableFXOptionSecurity();
  }
  
  @Override
  @Test
  public void testCapFloorSecurity() {
    _testCase.testCapFloorSecurity();
  }

  @Override
  @Test
  public void testCapFloorCMSSpreadSecurity() {
    _testCase.testCapFloorCMSSpreadSecurity();
  }
  
  @Override
  @Test
  public void testRawSecurity() {
    _testCase.testRawSecurity();
  }

  @Override
  @Test
  public void testEquityVarianceSwapSecurity() {
    _testCase.testEquityVarianceSwapSecurity();
  }

}
