/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.test;

/**
 * Generic tests for all security types implementation.
 */
public interface SecurityTestCaseMethods {

  void testCorporateBondSecurity();

  void testGovernmentBondSecurity();

  void testMunicipalBondSecurity();

  void testCashSecurity();

  void testEquitySecurity() throws Exception;

  void testFRASecurity();

  void testAgricultureFutureSecurity();

  void testBondFutureSecurity() throws Exception;

  void testEnergyFutureSecurity();

  void testFXFutureSecurity();

  void testNonDeliverableFXForwardSecurity();

  void testIndexFutureSecurity();

  void testInterestRateFutureSecurity();

  void testMetalFutureSecurity();

  void testStockFutureSecurity();

  void testEquityOptionSecurity();

  void testEquityBarrierOptionSecurity();

  void testIRFutureOptionSecurity();

  void testEquityIndexDividendFutureOptionSecurity();

  void testFXOptionSecurity();

  void testNonDeliverableFXOptionSecurity();

  void testFXBarrierOptionSecurity();

  void testSwaptionSecurity();

  void testForwardSwapSecurity();

  void testSwapSecurity();

  void testEquityIndexOptionSecurity();

  void testFXDigitalOptionSecurity();

  void testFXForwardSecurity();

  void testCapFloorSecurity();

  void testCapFloorCMSSpreadSecurity();

  void testRawSecurity();

  void testEquityVarianceSwapSecurity();

  void testSimpleZeroDepositSecurity();

  void testPeriodicZeroDepositSecurity();

  void testContinuousZeroDepositSecurity();
  
  void testCDSSecurity();
  
  void testStandardFixedRecoveryCDSSecurity();
  
  void testStandardRecoveryLockCDSSecurity();
  
  void testStandardVanillaCDSSecurity();
  
  void testLegacyFixedRecoveryCDSSecurity();
  
  void testLegacyRecoveryLockCDSSecurity();
  
  void testLegacyVanillaCDSSecurity();

  void testCashFlowSecurity();
  
  void testCreditDefaultSwapIndexDefinitionSecurity();

  void testCreditDefaultSwapIndexSecurity();
  
  void testCreditDefaultSwapOptionSecurity();

  void testBondIndex();
  
  void testEquityIndex();
  
  void testIborIndex();
  
  void testOvernightIndex();

  void testIndexFamily();
}
