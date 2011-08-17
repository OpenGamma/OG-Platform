/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Test MasterSecuritySource.
 */
@Test
public class MasterSecuritySourceTest {

  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID1 = ExternalId.of("C", "D");
  private static final ExternalId ID2 = ExternalId.of("E", "F");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID1, ID2);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterSecuritySource(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterSecuritySource(null, null);
  }

  //-------------------------------------------------------------------------
  public void test_getSecurity_UniqueId_noOverride_found() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterSecuritySource test = new MasterSecuritySource(mock);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  public void test_getSecurity_UniqueId_found() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(OID, VC);
    
    assertEquals(example(), testResult);
  }

  public void test_getSecurity_UniqueId_notFound() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(OID, VC);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSecurity_ObjectId_found() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(OID, VC);
    verify(mock, times(1)).get(OID, VC);
    
    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSecuritiesByExternalIdBundle() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    ManageableSecurity security = example();
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Collection<Security> testResult = test.getSecurities(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  //-------------------------------------------------------------------------
  public void test_getSecurity_ExternalId() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addExternalId(ID1);
    request.addExternalId(ID2);
    request.setVersionCorrection(VC);
    ManageableSecurity security = example();
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  //-------------------------------------------------------------------------
  protected ManageableSecurity example() {
    return new ManageableSecurity(UID, "Test", "EQUITY", ExternalIdBundle.EMPTY);
  }

}
