/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataUserSourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataUserSourceResourceTest {

  private static final ExternalId EID = ExternalId.of("A", "B");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(EID);
  private UserSource _underlying;
  private UriInfo _uriInfo;
  private DataUserSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(UserSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataUserSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetUserByName() {
    final SimpleUserAccount target = new SimpleUserAccount("bob");
    target.setAlternateIds(BUNDLE);
    target.getProfile().setDisplayName("Test");
    
    when(_underlying.getAccount("bob")).thenReturn(target);
    
    Response test = _resource.getAccountByName("bob");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

}
