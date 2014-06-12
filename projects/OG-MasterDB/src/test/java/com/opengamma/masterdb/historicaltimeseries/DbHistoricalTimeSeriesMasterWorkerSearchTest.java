/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.test.DbTest;
import com.opengamma.util.test.TestGroup;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
@Test(groups = TestGroup.UNIT_DB)
public class DbHistoricalTimeSeriesMasterWorkerSearchTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DbTest.class)
  public DbHistoricalTimeSeriesMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(PagingRequest.ALL, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PagingRequest pr = PagingRequest.ofPage(1, 2);
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(pr);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PagingRequest pr = PagingRequest.ofPage(2, 2);
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(pr);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(pr, test.getPaging().getRequest());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_seriesIds_none() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setObjectIds(new ArrayList<ObjectId>());
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_seriesIds() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addObjectId(ObjectId.of("DbHts", "101"));
    request.addObjectId(ObjectId.of("DbHts", "201"));
    request.addObjectId(ObjectId.of("DbHts", "9999"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_seriesIds_badSchemeValidOid() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addObjectId(ObjectId.of("Rubbish", "101"));
    _htsMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.EXACT));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ALL));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.NONE));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("A", "Z"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("TICKER", "V503"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_oneMatches() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("TICKER", "RUBBISH"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("E", "H"), ExternalId.of("A", "D"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_identifier() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("V501");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_case() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("v501");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("FooBar");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("*3");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setExternalIdValue("v*3");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("A", "Z"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("NASDAQ", "V502"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("A", "D"));
    request.setExternalIdSearchType(ExternalIdSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.addExternalId(ExternalId.of("TICKER", "V503"));
    request.addExternalId(ExternalId.of("TICKER", "V505"));
    request.setExternalIdSearchType(ExternalIdSearchType.NONE);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Exact() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalIds(ExternalId.of("TICKER", "V501"), ExternalId.of("NASDAQ", "V502"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addExternalId(ExternalId.of("TICKER", "V501"));
    request.setExternalIdSearchType(ExternalIdSearchType.EXACT);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("FooBar");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("N102");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("n102");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("N1*");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setName("n1*");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert203(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
