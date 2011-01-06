/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.time.Instant;

import org.junit.Test;

import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.impl.MasterSecuritySource;

/**
 * Test MasterSecuritySource.
 */
public class MasterSecuritySourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID1 = Identifier.of("C", "D");
  private static final Identifier ID2 = Identifier.of("E", "F");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID1, ID2);

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterSecuritySource(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterSecuritySource(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_constructor3arg_nullMaster() throws Exception {
    new MasterSecuritySource(null, null, null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecurityByUID() throws Exception {
    Instant now = Instant.now();
    SecurityMaster mock = mock(SecurityMaster.class);
    SecurityHistoryRequest request = new SecurityHistoryRequest(UID, now.minusSeconds(2), now.minusSeconds(1));
    request.setFullDetail(true);
    ManageableSecurity security = new ManageableSecurity(UID, "Test", "EQUITY", IdentifierBundle.EMPTY);
    SecurityHistoryResult result = new SecurityHistoryResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.history(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).history(request);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecuritiesByIdentifierBundle() throws Exception {
    Instant now = Instant.now();
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(ID1);
    request.addSecurityKey(ID2);
    request.setFullDetail(true);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    ManageableSecurity security = new ManageableSecurity(UID, "Test", "EQUITY", IdentifierBundle.EMPTY);
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Collection<Security> testResult = test.getSecurities(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getSecurityByIdentifier() throws Exception {
    Instant now = Instant.now();
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(ID1);
    request.addSecurityKey(ID2);
    request.setFullDetail(true);
    request.setVersionAsOfInstant(now.minusSeconds(2));
    request.setCorrectedToInstant(now.minusSeconds(1));
    ManageableSecurity security = new ManageableSecurity(UID, "Test", "EQUITY", IdentifierBundle.EMPTY);
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, now.minusSeconds(2), now.minusSeconds(1));
    Security testResult = test.getSecurity(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

}
