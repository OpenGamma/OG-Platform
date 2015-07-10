/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

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
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataLegalEntityResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataLegalEntityResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private LegalEntityMaster _underlying;
  private DataLegalEntityResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(LegalEntityMaster.class);
    _resource = new DataLegalEntityResource(new DataLegalEntityMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetLegalEntity() {
    final ManageableLegalEntity target = new MockLegalEntity("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final LegalEntityDocument result = new LegalEntityDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);

    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateLegalEntity() {
    final ManageableLegalEntity target = new MockLegalEntity("Test", ExternalIdBundle.of("A", "B"), Currency.GBP);
    final LegalEntityDocument request = new LegalEntityDocument(target);
    request.setUniqueId(OID.atLatestVersion());

    final LegalEntityDocument result = new LegalEntityDocument(target);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);

    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteLegalEntity() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
