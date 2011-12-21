/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataSecuritiesResource.
 */
public class DataSecuritiesResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private SecurityMaster _underlying;
  private UriInfo _uriInfo;
  private DataSecuritiesResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(SecurityMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataSecuritiesResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddSecurity() {
    final ManageableSecurity target = new ManageableSecurity(null, "Name", "Type", ExternalIdBundle.of("C", "D"));
    final SecurityDocument request = new SecurityDocument(target);
    
    final SecurityDocument result = new SecurityDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindSecurity() {
    DataSecurityResource test = _resource.findSecurity("Test~A");
    assertSame(_resource, test.getSecuritiesResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlSecurityId());
  }

}
