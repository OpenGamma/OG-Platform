/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import javax.time.calendar.TimeZone;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ManageableExchange;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataExchangeResource.
 */
public class DataExchangeResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private ExchangeMaster _underlying;
  private DataExchangeResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ExchangeMaster.class);
    _resource = new DataExchangeResource(new DataExchangesResource(_underlying), OID.getObjectId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetExchange() {
    final ManageableExchange target = new ManageableExchange(ExternalIdBundle.of("A", "B"), "Test", ExternalIdBundle.EMPTY, TimeZone.of("Europe/London"));
    final ExchangeDocument result = new ExchangeDocument(target);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateExchange() {
    final ManageableExchange target = new ManageableExchange(ExternalIdBundle.of("A", "B"), "Test", ExternalIdBundle.EMPTY, TimeZone.of("Europe/London"));
    final ExchangeDocument request = new ExchangeDocument(target);
    request.setUniqueId(OID.atLatestVersion());
    
    final ExchangeDocument result = new ExchangeDocument(target);
    result.setUniqueId(OID.atLatestVersion());
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteExchange() {
    Response test = _resource.delete();
    verify(_underlying).remove(OID.atLatestVersion());
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
