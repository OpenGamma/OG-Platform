/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataLegalEntityMasterResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataLegalEntityMasterResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private LegalEntityMaster _underlying;
  private UriInfo _uriInfo;
  private DataLegalEntityMasterResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(LegalEntityMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataLegalEntityMasterResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddLegalEntity() {
    final ManageableLegalEntity target = new MockLegalEntity("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final LegalEntityDocument request = new LegalEntityDocument(target);

    final LegalEntityDocument result = new LegalEntityDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);

    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindLegalEntity() {
    DataLegalEntityResource test = _resource.findLegalEntity("Test~A");
    assertSame(_resource, test.getLegalEntitiesResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlId());
  }

}
