/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

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

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataConventionResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataConventionResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private ConventionMaster _underlying;
  private DataConventionResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConventionMaster.class);
    _resource = new DataConventionResource(new DataConventionMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetConvention() {
    final ManageableConvention target = new MockConvention("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final ConventionDocument result = new ConventionDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);

    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateConvention() {
    final ManageableConvention target = new MockConvention("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final ConventionDocument request = new ConventionDocument(target);
    request.setUniqueId(OID.atLatestVersion());

    final ConventionDocument result = new ConventionDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);

    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteConvention() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
