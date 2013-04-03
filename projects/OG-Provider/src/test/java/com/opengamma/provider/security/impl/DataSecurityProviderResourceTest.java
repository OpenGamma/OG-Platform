/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.security.impl;

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
import com.opengamma.provider.security.SecurityProvider;
import com.opengamma.provider.security.SecurityProviderRequest;
import com.opengamma.provider.security.SecurityProviderResult;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataSecurityProviderResourceTest {

  private SecurityProvider _underlying;
  private UriInfo _uriInfo;
  private DataSecurityProviderResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(SecurityProvider.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataSecurityProviderResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    final SecurityProviderRequest request = SecurityProviderRequest.createGet(
        ExternalIdBundle.of("A", "B"), "S");
    final SecurityProviderResult result = new SecurityProviderResult();
    
    when(_underlying.getSecurities(same(request))).thenReturn(result);
    
    Response test = _resource.getSecurity(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

}
