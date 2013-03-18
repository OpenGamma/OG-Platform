/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

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
import org.threeten.bp.Instant;

import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHolidaySourceResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataHolidaySourceResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "A");
  private static final UniqueId UID = OID.atVersion("B");
  private static final VersionCorrection VC = VersionCorrection.LATEST.withLatestFixed(Instant.now());
  private static final ExternalId EID = ExternalId.of("A", "B");
  private HolidaySource _underlying;
  private UriInfo _uriInfo;
  private DataHolidaySourceResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HolidaySource.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataHolidaySourceResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetHolidayByUid() {
    final SimpleHoliday target = new SimpleHoliday();
    target.setType(HolidayType.BANK);
    target.setRegionExternalId(EID);
    
    when(_underlying.get(eq(UID))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), UID.getVersion(), "", "");
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

  @Test
  public void testGetHolidayByOid() {
    final SimpleHoliday target = new SimpleHoliday();
    target.setType(HolidayType.BANK);
    target.setRegionExternalId(EID);
    
    when(_underlying.get(eq(OID), eq(VC))).thenReturn(target);
    
    Response test = _resource.get(OID.toString(), null, VC.getVersionAsOfString(), VC.getCorrectedToString());
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(target, test.getEntity());
  }

}
