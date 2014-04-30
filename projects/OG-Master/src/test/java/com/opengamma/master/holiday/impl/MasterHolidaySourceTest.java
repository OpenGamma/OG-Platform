/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link MasterHolidaySource}.
 */
@Test(groups = TestGroup.UNIT)
public class MasterHolidaySourceTest {

  private static final LocalDate DATE_MONDAY = LocalDate.of(2010, 10, 25);
  private static final LocalDate DATE_SUNDAY = LocalDate.of(2010, 10, 24);
  private static final Currency GBP = Currency.GBP;
  private static final ObjectId OID = ObjectId.of("A", "B");
  private static final UniqueId UID = UniqueId.of("A", "B", "V");
  private static final ExternalId ID = ExternalId.of("C", "D");
  private static final ExternalIdBundle BUNDLE = ExternalIdBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_nullMaster() throws Exception {
    new MasterHolidaySource(null);
  }

  //-------------------------------------------------------------------------
  public void test_getHoliday_UniqueId_noOverride_found() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);

    HolidayDocument doc = new HolidayDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    Holiday testResult = test.get(UID);
    verify(mock, times(1)).get(UID);

    assertEquals(example(), testResult);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_UniqueId_notFound() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);

    when(mock.get(UID)).thenThrow(new DataNotFoundException(""));
    MasterHolidaySource test = new MasterHolidaySource(mock);
    try {
      test.get(UID);
    } finally {
      verify(mock, times(1)).get(UID);
    }
  }

  //-------------------------------------------------------------------------
  public void test_getHoliday_ObjectId_found() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);

    HolidayDocument doc = new HolidayDocument(example());
    when(mock.get(OID, VC)).thenReturn(doc);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    Holiday testResult = test.get(OID, VC);
    verify(mock, times(1)).get(OID, VC);

    assertEquals(example(), testResult);
  }

  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_getHoliday_ObjectId_notFound() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);

    when(mock.get(OID, VC)).thenThrow(new DataNotFoundException(""));
    MasterHolidaySource test = new MasterHolidaySource(mock);
    try {
      test.get(OID, VC);
    } finally {
      verify(mock, times(1)).get(OID, VC);
    }
  }

  //-------------------------------------------------------------------------
  public void test_isHoliday_LocalDateCurrency_holiday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_MONDAY);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));

    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_MONDAY, GBP);
    verify(mock, times(1)).search(request);

    assertEquals(true, testResult);
  }

  public void test_isHoliday_LocalDateCurrency_workday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_MONDAY);
    HolidaySearchResult result = new HolidaySearchResult();

    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_MONDAY, GBP);
    verify(mock, times(1)).search(request);

    assertEquals(false, testResult);
  }

  public void test_isHoliday_LocalDateCurrency_sunday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(GBP);
    request.setDateToCheck(DATE_SUNDAY);
    request.setVersionCorrection(VC);
    HolidaySearchResult result = new HolidaySearchResult();

    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_SUNDAY, GBP);
    verify(mock, times(0)).search(request);

    assertEquals(true, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_isHoliday_LocalDateTypeExternalId_holiday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, ExternalIdBundle.of(ID));
    request.setDateToCheck(DATE_MONDAY);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));

    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_MONDAY, HolidayType.BANK, ID);
    verify(mock, times(1)).search(request);

    assertEquals(true, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_isHoliday_LocalDateTypeExternalIdBundle_holiday() throws Exception {
    HolidayMaster mock = mock(HolidayMaster.class);
    HolidaySearchRequest request = new HolidaySearchRequest(HolidayType.BANK, BUNDLE);
    request.setDateToCheck(DATE_MONDAY);
    ManageableHoliday holiday = new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
    HolidaySearchResult result = new HolidaySearchResult();
    result.getDocuments().add(new HolidayDocument(holiday));

    when(mock.search(request)).thenReturn(result);
    MasterHolidaySource test = new MasterHolidaySource(mock);
    boolean testResult = test.isHoliday(DATE_MONDAY, HolidayType.BANK, BUNDLE);
    verify(mock, times(1)).search(request);

    assertEquals(true, testResult);
  }

  //-------------------------------------------------------------------------
  protected Holiday example() {
    return new ManageableHoliday(GBP, Collections.singletonList(DATE_MONDAY));
  }

}
