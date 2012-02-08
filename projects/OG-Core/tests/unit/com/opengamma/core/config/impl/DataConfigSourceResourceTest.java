/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;
import java.util.Collection;

import javax.time.Instant;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Test.
 */
public class DataConfigSourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final String NAME = "name";
  private ConfigSource _underlying;
  private UriInfo _uriInfo;
  private DataConfigSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConfigSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConfigSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetConfigByUid() {
    final SimpleExchange target = new SimpleExchange();
    target.setName("Test");
    
    when(_underlying.getConfig(eq(SimpleExchange.class), eq(UID))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), SimpleExchange.class.getName(), UID.getVersion(), "", "");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetConfigByOid() {
    final SimpleExchange target = new SimpleExchange();
    target.setName("Test");
    
    when(_underlying.getConfig(eq(SimpleExchange.class), eq(OID), eq(VC))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), SimpleExchange.class.getName(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void testSearch() {
    final SimpleExchange target = new SimpleExchange();
    target.setName("Test");
    Collection targetColl = ImmutableList.of(target);
    
    when(_underlying.getConfigs(eq(SimpleExchange.class), eq(NAME), eq(VC))).thenReturn(targetColl);
    
    Response test = _resource.search(SimpleExchange.class.getName(), VC.getVersionAsOfString(), VC.getCorrectedToString(), NAME);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl), test.getEntity());
  }

}
