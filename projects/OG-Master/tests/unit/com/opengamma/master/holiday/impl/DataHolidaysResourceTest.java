/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import java.net.URI;
import java.util.ArrayList;

import javax.time.calendar.LocalDate;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataHolidaysResource.
 */
public class DataHolidaysResourceTest {

  private static final UniqueId UID = UniqueId.of("Test", "A", "B");
  private HolidayMaster _underlying;
  private UriInfo _uriInfo;
  private DataHolidaysResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(HolidayMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataHolidaysResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddHoliday() {
    final ManageableHoliday target = new ManageableHoliday(Currency.GBP, new ArrayList<LocalDate>());
    final HolidayDocument request = new HolidayDocument(target);
    
    final HolidayDocument result = new HolidayDocument(target);
    result.setUniqueId(UID);
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindHoliday() {
    DataHolidayResource test = _resource.findHoliday("Test~A");
    assertSame(_resource, test.getHolidaysResource());
    assertEquals(ObjectId.of("Test", "A"), test.getUrlHolidayId());
  }

}
