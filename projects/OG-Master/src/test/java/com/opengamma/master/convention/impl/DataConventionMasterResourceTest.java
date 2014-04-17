/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

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
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataConventionMasterResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataConventionMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private ConventionMaster _underlying;
  private UriInfo _uriInfo;
  private DataConventionMasterResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConventionMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConventionMasterResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddConvention() {
    final ManageableConvention target = new MockConvention("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final ConventionDocument request = new ConventionDocument(target);

    final ConventionDocument result = new ConventionDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);

    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindConvention() {
    DataConventionResource test = _resource.findConvention("Test~A");
    assertSame(_resource, test.getConventionsResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlId());
  }

}
