/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.math.BigDecimal;
import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.position.PositionSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.transport.jaxrs.FudgeResponse;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionSourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataPositionSourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final ExternalId EID = ExternalId.of("A", "B");
  private PositionSource _underlying;
  private UriInfo _uriInfo;
  private DataPositionSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PositionSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPositionSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPortfolioByUid() {
    final SimplePortfolio target = new SimplePortfolio("Test");

    when(_underlying.getPortfolio(eq(UID), eq(VersionCorrection.LATEST))).thenReturn(target);

    final Response test = _resource.getPortfolio(OID.toString(), UID.getVersion(), null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetPortfolioByOid() {
    final SimplePortfolio target = new SimplePortfolio("Test");

    when(_underlying.getPortfolio(eq(OID), eq(VC))).thenReturn(target);

    final Response test = _resource.getPortfolio(OID.toString(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetNodeByUid() {
    final SimplePortfolioNode target = new SimplePortfolioNode("Test");

    when(_underlying.getPortfolioNode(eq(UID), eq(VersionCorrection.LATEST))).thenReturn(target);

    final Response test = _resource.getNode(OID.toString(), UID.getVersion(), null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, FudgeResponse.unwrap(test.getEntity()));
  }

  @Test
  public void testGetPositionByUid() {
    final SimplePosition target = new SimplePosition(BigDecimal.ONE, EID);

    when(_underlying.getPosition(eq(UID))).thenReturn(target);

    final Response test = _resource.getPosition(OID.toString(), UID.getVersion(), null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetPositionByOid() {
    final SimplePosition target = new SimplePosition(BigDecimal.ONE, EID);
    when(_underlying.getPosition(eq(OID), eq(VC))).thenReturn(target);
    final Response test = _resource.getPosition(OID.toString(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetTradeByUid() {
    final SimpleTrade target = new SimpleTrade();
    target.setQuantity(BigDecimal.ONE);

    when(_underlying.getTrade(eq(UID))).thenReturn(target);

    final Response test = _resource.getTrade(OID.toString(), UID.getVersion());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

}
