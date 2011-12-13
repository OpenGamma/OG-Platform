/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.test.SecurityTestCaseMethods;
import com.opengamma.id.UniqueId;

/**
 * Test {@link WebSecurityResource}.
 */
public class WebSecurityResourceTest extends AbstractWebSecurityResourceTestCase implements SecurityTestCaseMethods {
 
  @Override
  public void testCorporateBondSecurity() {
  }

  @Override
  public void testGovernmentBondSecurity() {
  }

  @Override
  public void testMunicipalBondSecurity() {
  }

  @Override
  public void testCashSecurity() {
  }

  @Test
  @Override
  public void testEquitySecurity() throws Exception {
    assertGetSecurity(WebSecuritiesResourceTestUtils.getEquitySecurity());
  }

  @Override
  public void testFRASecurity() {
  }

  @Override
  public void testAgricultureFutureSecurity() {
  }

  @Test
  @Override
  public void testBondFutureSecurity() throws Exception {
    assertGetSecurity(WebSecuritiesResourceTestUtils.getBondFutureSecurity());
  }

  @Override
  public void testEnergyFutureSecurity() {
  }

  @Override
  public void testFXFutureSecurity() {
  }

  @Override
  public void testNonDeliverableFXForwardSecurity() {
  }

  @Override
  public void testIndexFutureSecurity() {
  }

  @Override
  public void testInterestRateFutureSecurity() {
  }

  @Override
  public void testMetalFutureSecurity() {
  }

  @Override
  public void testStockFutureSecurity() {
  }

  @Override
  public void testEquityOptionSecurity() {
  }

  @Override
  public void testEquityBarrierOptionSecurity() {
  }

  @Override
  public void testIRFutureOptionSecurity() {
  }

  @Override
  public void testEquityIndexDividendFutureOptionSecurity() {
  }

  @Override
  public void testFXOptionSecurity() {
  }

  @Override
  public void testNonDeliverableFXOptionSecurity() {
  }

  @Override
  public void testFXBarrierOptionSecurity() {
  }

  @Override
  public void testSwaptionSecurity() {
  }

  @Override
  public void testForwardSwapSecurity() {
  }

  @Override
  public void testSwapSecurity() {
  }

  @Override
  public void testEquityIndexOptionSecurity() {
  }

  @Override
  public void testFXSecurity() {
  }

  @Override
  public void testFXForwardSecurity() {
  }

  @Override
  public void testCapFloorSecurity() {
  }

  @Override
  public void testCapFloorCMSSpreadSecurity() {
  }

  @Override
  public void testRawSecurity() {
  }

  @Override
  public void testEquityVarianceSwapSecurity() {
  }
  
  private void assertGetSecurity(FinancialSecurity finSecurity) throws Exception {
    assertNotNull(finSecurity);
    UniqueId uniqueId = _sec2UniqueId.get(finSecurity);
    assertNotNull(uniqueId);
    
    WebSecurityResource securityResource = _webSecuritiesResource.findSecurity(uniqueId.toString());
    assertNotNull(securityResource);
    String json = securityResource.getJSON();
    assertNotNull(json);
    JSONObject actualJson = new JSONObject(json); 
    
    JSONObject expectedJson = finSecurity.accept(new ExpectedSecurityJsonProvider());
    assertNotNull(expectedJson);
    assertEquals(expectedJson.toString(), actualJson.toString());
  }
  
}
