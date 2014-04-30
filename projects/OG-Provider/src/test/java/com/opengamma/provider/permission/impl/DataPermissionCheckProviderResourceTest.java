/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.permission.impl;

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
import com.opengamma.provider.permission.PermissionCheckProvider;
import com.opengamma.provider.permission.PermissionCheckProviderRequest;
import com.opengamma.provider.permission.PermissionCheckProviderResult;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DataPermissionCheckProviderResourceTest {

  private PermissionCheckProvider _underlying;
  private UriInfo _uriInfo;
  private DataPermissionCheckProviderResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PermissionCheckProvider.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPermissionCheckProviderResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGet() {
    final PermissionCheckProviderRequest request = PermissionCheckProviderRequest.createGet(ExternalIdBundle.of("A", "B"), "127.0.0.1", "A", "B", "C");
    final PermissionCheckProviderResult result = new PermissionCheckProviderResult();
    result.getCheckedPermissions().put("A", true);
    result.getCheckedPermissions().put("B", true);
    result.getCheckedPermissions().put("C", true);
    
    when(_underlying.isPermitted(same(request))).thenReturn(result);
    
    Response test = _resource.getPermissionCheck(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

}
