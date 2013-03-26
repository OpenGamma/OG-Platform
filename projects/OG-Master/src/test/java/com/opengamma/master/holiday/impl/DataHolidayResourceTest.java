/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;
import java.util.ArrayList;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHolidayResource.
 */
@Test(groups = TestGroup.UNIT)
public class DataHolidayResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private HolidayMaster _underlying;
  private DataHolidayResource _resource;
  private UriInfo _uriInfo;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HolidayMaster.class);
    _resource = new DataHolidayResource(new DataHolidayMasterResource(_underlying), OID.getObjectId());
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("http://localhost/"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetHoliday() {
    final ManageableHoliday holiday = new ManageableHoliday(Currency.GBP, new ArrayList<LocalDate>());
    final HolidayDocument result = new HolidayDocument(holiday);
    when(_underlying.get(OID, VersionCorrection.LATEST)).thenReturn(result);
    
    Response test = _resource.get(null, null);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdateHoliday() {
    final ManageableHoliday holiday = new ManageableHoliday(Currency.GBP, new ArrayList<LocalDate>());
    final HolidayDocument request = new HolidayDocument(holiday);
    request.setUniqueId(OID.atLatestVersion());
    
    final HolidayDocument result = new HolidayDocument(holiday);
    result.setUniqueId(OID.atVersion("1"));
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.update(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteHoliday() {
    _resource.remove();
    verify(_underlying).remove(OID.atLatestVersion());
  }

}
