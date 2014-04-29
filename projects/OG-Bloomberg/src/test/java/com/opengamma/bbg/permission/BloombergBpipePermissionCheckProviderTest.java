/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.permission;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Map;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.test.BloombergTestUtils;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION, enabled = false)
public class BloombergBpipePermissionCheckProviderTest {

  private BloombergBpipePermissionCheckProvider _provider;

  @BeforeMethod
  public void setUp() throws Exception {
    BloombergConnector connector = BloombergTestUtils.getBloombergBipeConnector();
    BloombergBpipePermissionCheckProvider provider = new BloombergBpipePermissionCheckProvider(connector);
    
    provider.start();
    _provider = provider;
  }

  @AfterMethod
  public void tearDown() throws Exception {
    if (_provider != null) {
      _provider.stop();
    }
    _provider = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void isPermittedEidCheck() {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalSchemes.bloombergEMRSUserId("og:yomi"), "10.0.2.110",
        "EID:27749", "EID:35009", "EID:39491", "EID:40066", "EID:41095", "EID:46707", "EID:1234");

    PermissionCheckProviderResult resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getPermissionCheckResult());
    Map<String, Boolean> checkPermissionResult = resultHolder.getPermissionCheckResult();
    assertEquals(7, checkPermissionResult.size());
    assertTrue(checkPermissionResult.get("EID:27749"));
    assertTrue(checkPermissionResult.get("EID:35009"));
    assertTrue(checkPermissionResult.get("EID:39491"));
    assertTrue(checkPermissionResult.get("EID:40066"));
    assertTrue(checkPermissionResult.get("EID:41095"));
    assertTrue(checkPermissionResult.get("EID:46707"));
    assertFalse(checkPermissionResult.get("EID:1234"));
  }

  @Test
  public void notPermittedEidCheckAfterEntitlementRevoked() {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalSchemes.bloombergEMRSUserId("og:yomi"), "10.0.2.110",
        "EID:27749", "EID:35009", "EID:39491", "EID:40066", "EID:41095", "EID:46707", "EID:1234");

    PermissionCheckProviderResult resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getPermissionCheckResult());
    Map<String, Boolean> checkPermissionResult = resultHolder.getPermissionCheckResult();
    assertEquals(7, checkPermissionResult.size());
    assertTrue(checkPermissionResult.get("EID:27749"));
    assertTrue(checkPermissionResult.get("EID:35009"));
    assertTrue(checkPermissionResult.get("EID:39491"));
    assertTrue(checkPermissionResult.get("EID:40066"));
    assertTrue(checkPermissionResult.get("EID:41095"));
    assertTrue(checkPermissionResult.get("EID:46707"));
    assertFalse(checkPermissionResult.get("EID:1234"));

    //sleep for a bit to allow bloomberg logon on another PC
    try {
      Thread.sleep(120000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getPermissionCheckResult());
    checkPermissionResult = resultHolder.getPermissionCheckResult();
    assertEquals(7, checkPermissionResult.size());
    assertFalse(checkPermissionResult.get("EID:27749"));
    assertFalse(checkPermissionResult.get("EID:35009"));
    assertFalse(checkPermissionResult.get("EID:39491"));
    assertFalse(checkPermissionResult.get("EID:40066"));
    assertFalse(checkPermissionResult.get("EID:41095"));
    assertFalse(checkPermissionResult.get("EID:46707"));
    assertFalse(checkPermissionResult.get("EID:1234"));
  }
  
  @Test
  public void isPermittedLiveDataCheck() {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest
        .createGet(ExternalSchemes.bloombergEMRSUserId("og:yomi"), "10.0.2.110", "LIVEDATA:IBM US Equity", "LIVEDATA:AAPL US Equity");

    PermissionCheckProviderResult resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    assertNotNull(resultHolder.getPermissionCheckResult());
    Map<String, Boolean> checkPermissionResult = resultHolder.getPermissionCheckResult();
    assertEquals(2, checkPermissionResult.size());
    assertTrue(checkPermissionResult.get("LIVEDATA:IBM US Equity"));
    assertFalse(checkPermissionResult.get("LIVEDATA:AAPL US Equity"));
  }

}
