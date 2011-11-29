/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.exchange.rest;

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
    final ManageableExchange exchange = new ManageableExchange(ExternalIdBundle.of("A", "B"), "Test", ExternalIdBundle.EMPTY, TimeZone.of("Europe/London"));
    final ExchangeDocument request = new ExchangeDocument(exchange);
    
    final ExchangeDocument result = new ExchangeDocument(exchange);
    result.setUniqueId(UniqueId.of("Test", "PosA"));
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindExchange() {
    DataExchangeResource test = _resource.findExchange("Test~PosA");
    assertSame(_resource, test.getExchangesResource());
    assertEquals(ObjectId.of("Test", "PosA"), test.getUrlExchangeId());
  }

}
