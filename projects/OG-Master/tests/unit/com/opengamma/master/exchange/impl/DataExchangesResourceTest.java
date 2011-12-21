/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.time.calendar.TimeZone;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ManageableExchange;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataExchangesResource.
 */
public class DataExchangesResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private ExchangeMaster _underlying;
  private UriInfo _uriInfo;
  private DataExchangesResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ExchangeMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataExchangesResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddExchange() {
    final ManageableExchange target = new ManageableExchange(ExternalIdBundle.of("A", "B"), "Test", ExternalIdBundle.EMPTY, TimeZone.of("Europe/London"));
    final ExchangeDocument request = new ExchangeDocument(target);
    
    final ExchangeDocument result = new ExchangeDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindExchange() {
    DataExchangeResource test = _resource.findExchange("Test~A");
    assertSame(_resource, test.getExchangesResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlExchangeId());
  }

}
