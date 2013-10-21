/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
@Test(groups = TestGroup.UNIT_DB)
public class QueryHolidayDbHolidayMasterWorkerSearchTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public QueryHolidayDbHolidayMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion, true);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHolidays_documents() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(_totalHolidays, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(1, 2));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(PagingRequest.ofPage(2, 2));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItemOneBased());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("FooBar");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday102");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday102");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday1*");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday1*");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(HolidayType.CURRENCY);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_providerNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderId(ExternalId.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_providerFound() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderId(ExternalId.of("COPP_CLARK", "2"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_regionEmptyBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setRegionExternalIdSearch(ExternalIdSearch.of());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_regionNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addRegionExternalId(ExternalId.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchange_empty() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setExchangeExternalIdSearch(ExternalIdSearch.of());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchange_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addExchangeExternalId(ExternalId.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currency_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.USD);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currency_oneMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.EUR);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_currency_twoMatches() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.GBP);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_holidayIds_none() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setHolidayObjectIds(new ArrayList<ObjectId>());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_holidayIds() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addHolidayObjectId(ObjectId.of("DbHol", "101"));
    request.addHolidayObjectId(ObjectId.of("DbHol", "201"));
    request.addHolidayObjectId(ObjectId.of("DbHol", "9999"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_holidayIds_badSchemeValidOid() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addHolidayObjectId(ObjectId.of("Rubbish", "120"));
    _holMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

}
