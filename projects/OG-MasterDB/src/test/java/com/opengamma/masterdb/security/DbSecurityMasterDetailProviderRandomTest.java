/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.financial.security.test.SecurityTestCaseMethods;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDetailProvider;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Test DbSecurityMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbSecurityMasterDetailProviderRandomTest extends AbstractDbSecurityTest implements SecurityTestCaseMethods {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(DbSecurityMasterDetailProviderRandomTest.class);

  private SecurityMasterTestCase _testCase;

  /**
   * @param databaseType
   */
  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbSecurityMasterDetailProviderRandomTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running test for database={}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doSetUp() {
    DbSecurityMaster secMaster = new DbSecurityMaster(getDbConnector());
    secMaster.setDetailProvider(new HibernateSecurityMasterDetailProvider());
    s_logger.debug("SecMaster initialization complete {}", secMaster);
    _testCase = new SecurityMasterTestCase(secMaster);
  }

  //-------------------------------------------------------------------------
  // SecurityMasterTestCaseMethods

  @Override
  public void testAgricultureFutureSecurity() {
    _testCase.testAgricultureFutureSecurity();
  }

  @Override
  public void testBondFutureSecurity() {
    _testCase.testBondFutureSecurity();
  }

  @Override
  public void testCashSecurity() {
    _testCase.testCashSecurity();
  }

  @Override
  public void testCorporateBondSecurity() {
    _testCase.testCorporateBondSecurity();
  }

  @Override
  public void testEnergyFutureSecurity() {
    _testCase.testEnergyFutureSecurity();
  }

  @Override
  public void testEquityOptionSecurity() {
    _testCase.testEquityOptionSecurity();
  }
  

  @Override
  public void testEquityBarrierOptionSecurity() {
    _testCase.testEquityBarrierOptionSecurity();
  }

  @Override
  public void testEquitySecurity() {
    _testCase.testEquitySecurity();
  }

  @Override
  public void testFRASecurity() {
    _testCase.testFRASecurity();
  }

  @Override
  public void testFXFutureSecurity() {
    _testCase.testFXFutureSecurity();
  }

  @Override
  public void testFXOptionSecurity() {
    _testCase.testFXOptionSecurity();
  } 

  @Override
  public void testNonDeliverableFXOptionSecurity() {
    _testCase.testNonDeliverableFXOptionSecurity();
  } 
  
  @Override
  public void testFXBarrierOptionSecurity() {
    _testCase.testFXBarrierOptionSecurity();
  }

  @Override
  public void testForwardSwapSecurity() {
    _testCase.testForwardSwapSecurity();
  }

  @Override
  public void testIRFutureOptionSecurity() {
    _testCase.testIRFutureOptionSecurity();
  }

  @Override
  public void testEquityIndexDividendFutureOptionSecurity() {
    _testCase.testEquityIndexDividendFutureOptionSecurity();
  }

  @Override
  public void testGovernmentBondSecurity() {
    _testCase.testGovernmentBondSecurity();
  }

  @Override
  public void testIndexFutureSecurity() {
    _testCase.testIndexFutureSecurity();
  }

  @Override
  public void testInterestRateFutureSecurity() {
    _testCase.testInterestRateFutureSecurity();
  }

  @Override
  public void testMetalFutureSecurity() {
    _testCase.testMetalFutureSecurity();
  }

  @Override
  public void testMunicipalBondSecurity() {
    _testCase.testMunicipalBondSecurity();
  }

  @Override
  public void testStockFutureSecurity() {
    _testCase.testStockFutureSecurity();
  }

  @Override
  public void testSwaptionSecurity() {
    _testCase.testSwaptionSecurity();
  }

  @Override
  public void testSwapSecurity() {
    _testCase.testSwapSecurity();
  }

  @Override
  public void testEquityIndexOptionSecurity() {
    _testCase.testEquityIndexOptionSecurity();
  }

  @Override
  public void testFXDigitalOptionSecurity() {
    _testCase.testFXDigitalOptionSecurity();
  }

  @Override
  public void testFXForwardSecurity() {
    _testCase.testFXForwardSecurity();
  }

  @Override
  public void testNonDeliverableFXForwardSecurity() {
    _testCase.testNonDeliverableFXOptionSecurity();
  }
  
  @Override
  public void testCapFloorSecurity() {
    _testCase.testCapFloorSecurity();
  }

  @Override
  public void testCapFloorCMSSpreadSecurity() {
    _testCase.testCapFloorCMSSpreadSecurity();
  }
  
  @Override
  public void testRawSecurity() {
    _testCase.testRawSecurity();
  }

  @Override
  public void testEquityVarianceSwapSecurity() {
    _testCase.testEquityVarianceSwapSecurity();
  }

  @Override
  public void testSimpleZeroDepositSecurity() {
    return;
  }
  
  @Override
  public void testPeriodicZeroDepositSecurity() {
    return;
  }
  
  @Override
  public void testContinuousZeroDepositSecurity() {
    return;
  }
  
  @Override
  public void testCDSSecurity() {
    _testCase.testCDSSecurity();
  }

  @Override
  public void testStandardFixedRecoveryCDSSecurity() {
    _testCase.testStandardFixedRecoveryCDSSecurity();
  }

  @Override
  public void testStandardRecoveryLockCDSSecurity() {
    _testCase.testStandardRecoveryLockCDSSecurity();
  }

  @Override
  public void testStandardVanillaCDSSecurity() {
    _testCase.testStandardVanillaCDSSecurity();
  }

  @Override
  public void testLegacyFixedRecoveryCDSSecurity() {
    _testCase.testLegacyFixedRecoveryCDSSecurity();
  }

  @Override
  public void testLegacyRecoveryLockCDSSecurity() {
    _testCase.testLegacyRecoveryLockCDSSecurity();
  }

  @Override
  public void testLegacyVanillaCDSSecurity() {
    _testCase.testLegacyVanillaCDSSecurity();
  }

  @Override
  public void testCashFlowSecurity() {
    _testCase.testCashFlowSecurity();
  }

  @Override
  public void testCreditDefaultSwapIndexDefinitionSecurity() {
    _testCase.testCreditDefaultSwapIndexDefinitionSecurity();
  }

  @Override
  public void testCreditDefaultSwapIndexSecurity() {
    _testCase.testCreditDefaultSwapIndexSecurity();
  }

  @Override
  public void testCreditDefaultSwapOptionSecurity() {
    _testCase.testCreditDefaultSwapOptionSecurity();
  }

  @Override
  public void testBondIndex() {
    _testCase.testBondIndex();
  }

  @Override
  public void testEquityIndex() {
    _testCase.testEquityIndex();
  }

  @Override
  public void testIborIndex() {
    _testCase.testIborIndex();
  }

  @Override
  public void testOvernightIndex() {
    _testCase.testOvernightIndex();
  }

  @Override
  public void testIndexFamily() {
    _testCase.testIndexFamily();
  }
  

  
}
