/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

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

import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataMarketDataSnapshotsResource.
 */
public class DataMarketDataSnapshotsResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private MarketDataSnapshotMaster _underlying;
  private UriInfo _uriInfo;
  private DataMarketDataSnapshotsResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(MarketDataSnapshotMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataMarketDataSnapshotsResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddMarketDataSnapshot() {
    final ManageableMarketDataSnapshot target = new ManageableMarketDataSnapshot();
    target.setBasisViewName("Basis");
    final MarketDataSnapshotDocument request = new MarketDataSnapshotDocument(target);
    
    final MarketDataSnapshotDocument result = new MarketDataSnapshotDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindMarketDataSnapshot() {
    DataMarketDataSnapshotResource test = _resource.findMarketDataSnapshot("Test~A");
    assertSame(_resource, test.getMarketDataSnapshotsResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlMarketDataSnapshotId());
  }

}
