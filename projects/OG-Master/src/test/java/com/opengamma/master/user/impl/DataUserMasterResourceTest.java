/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

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
import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataUsersResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataUserMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private UserMaster _underlying;
  private UriInfo _uriInfo;
  private DataUserMasterResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(UserMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataUserMasterResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddUser() {
    final ManageableOGUser target = new ManageableOGUser("bob");
    target.setExternalIdBundle(ExternalIdBundle.of("A", "B"));
    target.setName("Test");
    target.setTimeZone(ZoneId.of("Europe/London"));
    target.setEmailAddress("bob@bob.com");
    final UserDocument request = new UserDocument(target);
    
    final UserDocument result = new UserDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindUser() {
    DataUserResource test = _resource.findUser("Test~A");
    assertSame(_resource, test.getUsersResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlId());
  }

}
