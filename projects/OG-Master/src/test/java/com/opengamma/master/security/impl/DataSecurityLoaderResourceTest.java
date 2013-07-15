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
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataSecurityLoaderResourceTest {

  private SecurityLoader _underlying;
  private UriInfo _uriInfo;
  private DataSecurityLoaderResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(SecurityLoader.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataSecurityLoaderResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    final SecurityLoaderRequest request = SecurityLoaderRequest.create(ExternalIdBundle.of("A", "B"));
    final SecurityLoaderResult result = new SecurityLoaderResult();
    
    when(_underlying.loadSecurities(same(request))).thenReturn(result);
    
    Response test = _resource.loadSecurities(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

}
