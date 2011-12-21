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

import java.util.ArrayList;

import javax.time.calendar.LocalDate;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHolidayResource.
 */
public class DataHolidayResourceTest {

  private static final ObjectId OID = ObjectId.of("Test", "PosA");
  private HolidayMaster _underlying;
  private DataHolidayResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HolidayMaster.class);
    _resource = new DataHolidayResource(new DataHolidaysResource(_underlying), OID.getObjectId());
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
    result.setUniqueId(OID.atLatestVersion());
    when(_underlying.update(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeleteHoliday() {
    Response test = _resource.delete();
    verify(_underlying).remove(OID.atLatestVersion());
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}
