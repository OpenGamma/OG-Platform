/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataSecurityResource.
 */
public class DataSecurityResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private SecurityMaster _underlying;
  private DataSecurityResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(SecurityMaster.class);
    _resource = new DataSecurityResource(new DataSecuritiesResource(_underlying), OID.getObjectId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetSecurity() {
    final ManageableSecurity security = new ManageableSecurity(null, "Name", "Type", ExternalIdBundle.of("C", "D"));
    final SecurityDocument result = new SecurityDocument(security);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateSecurity() {
    final ManageableSecurity security = new ManageableSecurity(null, "Name", "Type", ExternalIdBundle.of("C", "D"));
    final SecurityDocument request = new SecurityDocument(security);
    request.setUniqueId(OID.atLatestVersion());
    
    final SecurityDocument result = new SecurityDocument(security);
    result.setUniqueId(OID.atLatestVersion());
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteSecurity() {
    Response test = _resource.delete();
    verify(_underlying).remove(OID.atLatestVersion());
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
