/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.common.Currency;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
public class QueryHolidayDbHolidayMasterWorkerSearchTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerSearchTest.class);

  private DbHolidayMasterWorker _worker;

  public QueryHolidayDbHolidayMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryHolidayDbHolidayMasterWorker();
    _worker.init(_holMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHolidays_documents() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
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
    request.setPagingRequest(new PagingRequest(1, 2));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
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
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday102");
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday102");
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday1*");
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday1*");
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(HolidayType.CURRENCY);
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_providerNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderKey(Identifier.of("A", "B"));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_providerFound() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderKey(Identifier.of("COPP_CLARK", "2"));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_regionEmptyBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setRegionIdentifiers(IdentifierBundle.EMPTY);
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_regionNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setRegionIdentifiers(IdentifierBundle.of(Identifier.of("A", "B")));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchangeEmptyBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setExchangeIdentifiers(IdentifierBundle.EMPTY);
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchangeNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setExchangeIdentifiers(IdentifierBundle.of(Identifier.of("A", "B")));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currencyNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.getInstance("USD"));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currencyOneMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.getInstance("EUR"));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_currencyTwoMatches() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.getInstance("GBP"));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionAsOfInstant(_version1Instant.minusSeconds(5));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionAsOfInstant(_version1Instant.plusSeconds(5));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionAsOfInstant(_version2Instant.plusSeconds(5));
    HolidaySearchResult test = _worker.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbHol]", _worker.toString());
  }

}
