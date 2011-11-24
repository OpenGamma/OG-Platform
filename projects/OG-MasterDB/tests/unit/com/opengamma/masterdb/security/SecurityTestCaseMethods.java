/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

/**
 * Generic tests for all security types implementation.
 */
public interface SecurityTestCaseMethods {

  void testCorporateBondSecurity();

  void testGovernmentBondSecurity();

  void testMunicipalBondSecurity();

  void testCashSecurity();

  void testEquitySecurity();

  void testFRASecurity();

  void testAgricultureFutureSecurity();

  void testBondFutureSecurity();

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

  void testFXOptionSecurity();
  
  void testNonDeliverableFXOptionSecurity();
  
  void testFXBarrierOptionSecurity();

  void testSwaptionSecurity();

  void testForwardSwapSecurity();

  void testSwapSecurity();
  
  void testEquityIndexOptionSecurity();
  
  void testFXSecurity();
  
  void testFXForwardSecurity();
  
  void testCapFloorSecurity();
  
  void testCapFloorCMSSpreadSecurity();

  void testRawSecurity();
  
  void testEquityVarianceSwapSecurity();

}
