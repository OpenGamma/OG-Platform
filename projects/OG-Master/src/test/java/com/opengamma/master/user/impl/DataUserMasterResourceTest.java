/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.user.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.ZoneId;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.user.ManageableUser;
import com.opengamma.master.user.UserMaster;
import com.opengamma.transport.jaxrs.FudgeResponse;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataUsersResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataUserMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "1");
  private static final ObjectId OID = ObjectId.of("Test", "A");
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
    final ManageableUser input = new ManageableUser("bob");
    input.setAlternateIds(ExternalIdBundle.of("A", "B"));
    input.setEmailAddress("bob@bob.com");
    input.getProfile().setDisplayName("Test");
    input.getProfile().setZone(ZoneId.of("Europe/London"));
    
    when(_underlying.add(input)).thenReturn(UID);
    
    Response test = _resource.add(_uriInfo, input);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertEquals(UID, FudgeResponse.unwrap(test.getEntity()));
  }

  @Test
  public void testGetUser() {
    final ManageableUser target = new ManageableUser("bob");
    target.setAlternateIds(ExternalIdBundle.of("A", "B"));
    target.setEmailAddress("bob@bob.com");
    target.getProfile().setDisplayName("Test");
    target.getProfile().setZone(ZoneId.of("Europe/London"));
    
    when(_underlying.getById(OID)).thenReturn(target);
    
    Response test = _resource.getById(OID.toString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(target, test.getEntity());
  }

  @Test
  public void testUpdateUser() {
    final ManageableUser input = new ManageableUser("bob");
    input.setAlternateIds(ExternalIdBundle.of("A", "B"));
    input.setEmailAddress("bob@bob.com");
    input.getProfile().setDisplayName("Test");
    input.getProfile().setZone(ZoneId.of("Europe/London"));
    input.setUniqueId(UID.withVersion("1"));
    final ManageableUser result = input.clone();
    result.getProfile().setDisplayName("Tester");
    result.setUniqueId(UID.withVersion("2"));
    
    when(_underlying.update(input)).thenReturn(UID.withVersion("2"));
    
    Response test = _resource.updateById(_uriInfo, OID.toString(), input);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(UID.withVersion("2"), FudgeResponse.unwrap(test.getEntity()));
  }

  @Test
  public void testRemoveUserById() {
    _resource.removeById(OID.toString());
    verify(_underlying).removeById(OID);
  }

  @Test
  public void testRemoveUserByName() {
    _resource.removeByName("bob");
    verify(_underlying).removeByName("bob");
  }

}
