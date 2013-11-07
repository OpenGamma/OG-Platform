/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataConventionSourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataConventionSourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of("A", "B");
  private ConventionSource _underlying;
  private UriInfo _uriInfo;
  private DataConventionSourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(ConventionSource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataConventionSourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetConventionByUid() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    
    when(_underlying.get(eq(UID))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), UID.getVersion(), "", "");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetConventionByOid() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    
    when(_underlying.get(eq(OID), eq(VC))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @SuppressWarnings({"rawtypes", "unchecked" })
  @Test
  public void testSearch() {
    final MockConvention target = new MockConvention("TEST");
    target.setExternalIdBundle(BUNDLE);
    target.setName("Test");
    Collection targetColl = ImmutableList.<Convention>of(target);
    
    when(_underlying.get(eq(BUNDLE), eq(VC))).thenReturn(targetColl);
    
    Response test = _resource.search(VC.getVersionAsOfString(), VC.getCorrectedToString(), BUNDLE.toStringList());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertEquals(FudgeListWrapper.of(targetColl), test.getEntity());
  }

}
