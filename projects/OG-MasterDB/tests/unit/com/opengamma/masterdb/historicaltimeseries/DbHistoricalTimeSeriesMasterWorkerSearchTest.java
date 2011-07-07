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
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSearchResult;
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
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setPagingRequest(PagingRequest.of(1, 2));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setPagingRequest(PagingRequest.of(2, 2));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHistoricalTimeSeries, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_seriesIds_none() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setHistoricalTimeSeriesIds(new ArrayList<ObjectIdentifier>());
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_seriesIds() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesId(ObjectIdentifier.of("DbHts", "101"));
    request.addHistoricalTimeSeriesId(ObjectIdentifier.of("DbHts", "201"));
    request.addHistoricalTimeSeriesId(ObjectIdentifier.of("DbHts", "9999"));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_seriesIds_badSchemeValidOid() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesId(ObjectIdentifier.of("Rubbish", "101"));
    _htsMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setHistoricalTimeSeriesKeys(new IdentifierSearch());
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.EXACT);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setHistoricalTimeSeriesKeys(new IdentifierSearch());
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setHistoricalTimeSeriesKeys(new IdentifierSearch());
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ANY);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setHistoricalTimeSeriesKeys(new IdentifierSearch());
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V501"));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("A", "Z"));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKeys(Identifier.of("TICKER", "V501"), Identifier.of("TICKER", "V503"));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKeys(Identifier.of("E", "H"), Identifier.of("A", "D"));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValue("V501");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_case() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValue("v501");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValue("FooBar");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_identifier_wildcard() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValue("*3");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_identifier_wildcardCase() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifierValue("v*3");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V501"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("A", "Z"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKeys(Identifier.of("TICKER", "V501"), Identifier.of("NASDAQ", "V502"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKeys(Identifier.of("TICKER", "V501"), Identifier.of("A", "D"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.ALL);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V501"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V501"));
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V503"));
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V505"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.NONE);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Exact() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKeys(Identifier.of("TICKER", "V501"), Identifier.of("NASDAQ", "V502"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.EXACT);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    System.out.println(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.addHistoricalTimeSeriesKey(Identifier.of("TICKER", "V501"));
    request.getHistoricalTimeSeriesKeys().setSearchType(IdentifierSearchType.EXACT);
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setName("FooBar");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setName("N102");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setName("n102");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setName("N1*");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setName("n1*");
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    HistoricalTimeSeriesSearchResult test = _htsMaster.search(request);
    
    assertEquals(_totalHistoricalTimeSeries, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_htsMaster.getClass().getSimpleName() + "[DbHts]", _htsMaster.toString());
  }

}
