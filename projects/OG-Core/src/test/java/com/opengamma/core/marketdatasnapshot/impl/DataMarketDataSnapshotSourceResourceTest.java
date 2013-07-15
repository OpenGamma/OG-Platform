/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataMarketDataSnapshotSourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataMarketDataSnapshotSourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private MarketDataSnapshotSource _underlying;
  private UriInfo _uriInfo;
  private DataMarketDataSnapshotSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(MarketDataSnapshotSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataMarketDataSnapshotSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetSnapshotByUid() {
    final StructuredMarketDataSnapshot target = new ManageableMarketDataSnapshot();
    
    when(_underlying.get(eq(UID))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), UID.getVersion());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

}
