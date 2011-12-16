/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import static com.opengamma.web.WebResourceTestUtils.assertJSONObjectEquals;

import java.util.List;

import org.json.JSONObject;
import org.testng.annotations.Test;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.test.SecurityTestCaseMethods;

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
    assertGetSecurity(_securities.get(EquitySecurity.class));
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
    assertGetSecurity(_securities.get(BondFutureSecurity.class));
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
  
  private void assertGetSecurity(final List<FinancialSecurity> securities) throws Exception {
    for (FinancialSecurity security : securities) {
      WebSecurityResource securityResource = _webSecuritiesResource.findSecurity(security.getUniqueId().toString());
      JSONObject actualJson = new JSONObject(securityResource.getJSON());
      
      JSONObject expectedJson = security.accept(new ExpectedSecurityJsonProvider());
      assertJSONObjectEquals(expectedJson, actualJson);
    }
  }
  
}
