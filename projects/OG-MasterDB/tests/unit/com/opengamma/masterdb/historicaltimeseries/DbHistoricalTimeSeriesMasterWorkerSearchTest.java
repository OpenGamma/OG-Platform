/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.test.DBTest;

/**
 * Tests DbHistoricalTimeSeriesMaster.
 */
public class DbHistoricalTimeSeriesMasterWorkerSearchTest extends AbstractDbHistoricalTimeSeriesMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(DbHistoricalTimeSeriesMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public DbHistoricalTimeSeriesMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(PagingRequest.of(1, 2));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setPagingRequest(PagingRequest.of(2, 2));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert203(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_seriesIds_none() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setInfoIds(new ArrayList<ObjectIdentifier>());
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_seriesIds() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addInfoId(ObjectIdentifier.of("DbHts", "101"));
    request.addInfoId(ObjectIdentifier.of("DbHts", "201"));
    request.addInfoId(ObjectIdentifier.of("DbHts", "9999"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_seriesIds_badSchemeValidOid() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addInfoId(ObjectIdentifier.of("Rubbish", "101"));
    _htsMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierKeys(new IdentifierSearch());
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.EXACT);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierKeys(new IdentifierSearch());
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierKeys(new IdentifierSearch());
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ANY);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierKeys(new IdentifierSearch());
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("TICKER", "V501"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("A", "Z"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKeys(Identifier.of("TICKER", "V501"), Identifier.of("TICKER", "V503"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKeys(Identifier.of("E", "H"), Identifier.of("A", "D"));
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierValue("V501");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_case() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierValue("v501");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierValue("FooBar");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierValue("*3");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setIdentifierValue("v*3");
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("TICKER", "V501"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("A", "Z"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKeys(Identifier.of("TICKER", "V501"), Identifier.of("NASDAQ", "V502"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKeys(Identifier.of("TICKER", "V501"), Identifier.of("A", "D"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("TICKER", "V501"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert203(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("TICKER", "V501"));
    request.addIdentifierKey(Identifier.of("TICKER", "V503"));
    request.addIdentifierKey(Identifier.of("TICKER", "V505"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Exact() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKeys(Identifier.of("TICKER", "V501"), Identifier.of("NASDAQ", "V502"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.EXACT);
    HistoricalTimeSeriesInfoSearchResult test = _htsMaster.search(request);
    
    System.out.println(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.addIdentifierKey(Identifier.of("TICKER", "V501"));
    request.getIdentifierKeys().setSearchType(IdentifierSearchType.EXACT);
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
