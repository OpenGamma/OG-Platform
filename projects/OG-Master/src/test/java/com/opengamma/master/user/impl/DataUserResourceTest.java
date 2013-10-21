/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.user.ManageableOGUser;
import com.opengamma.master.user.UserDocument;
import com.opengamma.master.user.UserMaster;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataUserResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataUserResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private UserMaster _underlying;
  private DataUserResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(UserMaster.class);
    _resource = new DataUserResource(new DataUserMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetUser() {
    final ManageableOGUser target = new ManageableOGUser("bob");
    target.setExternalIdBundle(ExternalIdBundle.of("A", "B"));
    target.setName("Test");
    target.setTimeZone(ZoneId.of("Europe/London"));
    target.setEmailAddress("bob@bob.com");
    final UserDocument result = new UserDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateUser() {
    final ManageableOGUser target = new ManageableOGUser("bob");
    target.setExternalIdBundle(ExternalIdBundle.of("A", "B"));
    target.setName("Test");
    target.setTimeZone(ZoneId.of("Europe/London"));
    target.setEmailAddress("bob@bob.com");
    final UserDocument request = new UserDocument(target);
    request.setUniqueId(OID.atLatestVersion());
    
    final UserDocument result = new UserDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteUser() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
