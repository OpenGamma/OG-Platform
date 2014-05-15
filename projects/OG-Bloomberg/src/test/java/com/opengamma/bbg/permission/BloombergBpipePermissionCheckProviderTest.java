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
import com.opengamma.bbg.BloombergPermissions;
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

  // fill these in to perform a manual test
  private static final String EMRS_USER_ID = "";
  private static final String IP_ADDRESS = "";

  private static final String EID_27749 = BloombergPermissions.createEidPermissionString(27749);
  private static final String EID_35009 = BloombergPermissions.createEidPermissionString(35009);
  private static final String EID_39491 = BloombergPermissions.createEidPermissionString(39491);
  private static final String EID_40066 = BloombergPermissions.createEidPermissionString(40066);
  private static final String EID_41095 = BloombergPermissions.createEidPermissionString(41095);
  private static final String EID_46707 = BloombergPermissions.createEidPermissionString(46707);
  private static final String EID_1234 = BloombergPermissions.createEidPermissionString(1234);

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
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalSchemes.bloombergEmrsUserId(EMRS_USER_ID), IP_ADDRESS,
        EID_27749, EID_35009, EID_39491, EID_40066, EID_41095, EID_46707, EID_1234);

    PermissionCheckProviderResult resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    resultHolder.checkErrors();
    assertNotNull(resultHolder.getCheckedPermissions());
    Map<String, Boolean> checkPermissionResult = resultHolder.getCheckedPermissions();
    assertEquals(7, checkPermissionResult.size());
    assertTrue(checkPermissionResult.get(EID_27749));
    assertTrue(checkPermissionResult.get(EID_35009));
    assertTrue(checkPermissionResult.get(EID_39491));
    assertTrue(checkPermissionResult.get(EID_40066));
    assertTrue(checkPermissionResult.get(EID_41095));
    assertTrue(checkPermissionResult.get(EID_46707));
    assertFalse(checkPermissionResult.get(EID_1234));
  }

  @Test
  public void notPermittedEidCheckAfterEntitlementRevoked() {
    PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalSchemes.bloombergEmrsUserId(EMRS_USER_ID), IP_ADDRESS,
        EID_27749, EID_35009, EID_39491, EID_40066, EID_41095, EID_46707, EID_1234);

    PermissionCheckProviderResult resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    resultHolder.checkErrors();
    assertNotNull(resultHolder.getCheckedPermissions());
    Map<String, Boolean> checkPermissionResult = resultHolder.getCheckedPermissions();
    assertEquals(7, checkPermissionResult.size());
    assertTrue(checkPermissionResult.get(EID_27749));
    assertTrue(checkPermissionResult.get(EID_35009));
    assertTrue(checkPermissionResult.get(EID_39491));
    assertTrue(checkPermissionResult.get(EID_40066));
    assertTrue(checkPermissionResult.get(EID_41095));
    assertTrue(checkPermissionResult.get(EID_46707));
    assertFalse(checkPermissionResult.get(EID_1234));

    // sleep for a bit to allow bloomberg logon on another PC
    try {
      Thread.sleep(120000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    resultHolder = _provider.isPermitted(request);
    assertNotNull(resultHolder);
    resultHolder.checkErrors();
    assertNotNull(resultHolder.getCheckedPermissions());
    checkPermissionResult = resultHolder.getCheckedPermissions();
    assertEquals(7, checkPermissionResult.size());
    assertFalse(checkPermissionResult.get(EID_27749));
    assertFalse(checkPermissionResult.get(EID_35009));
    assertFalse(checkPermissionResult.get(EID_39491));
    assertFalse(checkPermissionResult.get(EID_40066));
    assertFalse(checkPermissionResult.get(EID_41095));
    assertFalse(checkPermissionResult.get(EID_46707));
    assertFalse(checkPermissionResult.get(EID_1234));
  }

}
