/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.position;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.TimeZone;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryPositionDbPositionMasterWorker.
 */
public class QueryPositionDbPositionMasterWorkerSearchTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryPositionDbPositionMasterWorkerSearchTest.class);

  public QueryPositionDbPositionMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_documents() {
    PositionSearchRequest request = new PositionSearchRequest();
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(_totalPositions, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(1, 2));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(2, 2));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
    assert122(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageThree() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPagingRequest(new PagingRequest(3, 2));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(5, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalPositions, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert123(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_positionIds_none() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setPositionIds(new ArrayList<ObjectIdentifier>());
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_positionIds() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionId(ObjectIdentifier.of("DbPos", "120"));
    request.addPositionId(ObjectIdentifier.of("DbPos", "221"));
    request.addPositionId(ObjectIdentifier.of("DbPos", "9999"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert120(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_search_positionIds_badSchemeValidOid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionId(ObjectIdentifier.of("Rubbish", "120"));
    _posMaster.search(request);
  }

  @Test
  public void test_search_tradeIds_none() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setTradeIds(new ArrayList<ObjectIdentifier>());
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_tradeIds() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addTradeId(ObjectIdentifier.of("DbPos", "402"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "403"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "407"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "9999"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
    assert222(test.getDocuments().get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_search_tradeIds_badSchemeValidOid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addTradeId(ObjectIdentifier.of("Rubbish", "402"));
    _posMaster.search(request);
  }

  @Test
  public void test_search_positionAndTradeIds_matchSome() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionId(ObjectIdentifier.of("DbPos", "120"));
    request.addPositionId(ObjectIdentifier.of("DbPos", "122"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "402"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "403"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "407"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert122(test.getDocuments().get(0));
  }

  @Test
  public void test_search_positionAndTradeIds_matchNone() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addPositionId(ObjectIdentifier.of("DbPos", "120"));
    request.addPositionId(ObjectIdentifier.of("DbPos", "121"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "402"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "403"));
    request.addTradeId(ObjectIdentifier.of("DbPos", "407"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_noKeys_Exact_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_All_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_Any_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ANY);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_noKeys_None_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setSecurityKeys(new IdentifierSearch());
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(_totalPositions, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_Any_1() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("TICKER", "S100"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_Any_1_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("A", "Z"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_Any_2() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKeys(Identifier.of("TICKER", "MSFT"), Identifier.of("TICKER", "S100"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert121(test.getDocuments().get(1));
  }

  @Test
  public void test_search_twoKeys_Any_2_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKeys(Identifier.of("E", "H"), Identifier.of("A", "D"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_All_1() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("TICKER", "S100"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
  }

  @Test
  public void test_search_oneKey_All_1_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("A", "Z"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoKeys_All_2() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKeys(Identifier.of("TICKER", "MSFT"), Identifier.of("NASDAQ", "Micro"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_search_twoKeys_All_2_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKeys(Identifier.of("TICKER", "MSFT"), Identifier.of("A", "D"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.ALL);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneKey_None() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("TICKER", "MSFT"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(5, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert122(test.getDocuments().get(2));
    assert123(test.getDocuments().get(3));
    assert222(test.getDocuments().get(4));
  }

  @Test
  public void test_search_oneKey_None_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("TICKER", "S100"));
    request.addSecurityKey(Identifier.of("TICKER", "T130"));
    request.addSecurityKey(Identifier.of("TICKER", "MSFT"));
    request.addSecurityKey(Identifier.of("NASDAQ", "Micro"));
    request.addSecurityKey(Identifier.of("TICKER", "ORCL"));
    request.addSecurityKey(Identifier.of("TICKER", "ORCL134"));
    request.addSecurityKey(Identifier.of("NASDAQ", "ORCL135"));
    request.addSecurityKey(Identifier.of("TICKER", "IBMC"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.NONE);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_threeKeys_Exact() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKeys(Identifier.of("TICKER", "MSFT"), Identifier.of("NASDAQ", "Micro"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    PositionSearchResult test = _posMaster.search(request);
    
    System.out.println(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  @Test
  public void test_search_threeKeys_Exact_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.addSecurityKey(Identifier.of("TICKER", "MSFT"));
    request.getSecurityKeys().setSearchType(IdentifierSearchType.EXACT);
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_providerKey_noMatch() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setProviderKey(Identifier.of("A", "999"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_providerKey_found() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setProviderKey(Identifier.of("A", "121"));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert121(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_minQuantity_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(50));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));
  }

  @Test
  public void test_search_minQuantity_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(150));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert222(test.getDocuments().get(0));
  }

  @Test
  public void test_search_minQuantity_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMinQuantity(BigDecimal.valueOf(450));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_maxQuantity_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(50));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_maxQuantity_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(150));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(5, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
  }

  @Test
  public void test_search_maxQuantity_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setMaxQuantity(BigDecimal.valueOf(450));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert221(test.getDocuments().get(5));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    PositionSearchRequest request = new PositionSearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    PositionSearchResult test = _posMaster.search(request);
    
    assertEquals(6, test.getDocuments().size());
    assert100(test.getDocuments().get(0));
    assert120(test.getDocuments().get(1));
    assert121(test.getDocuments().get(2));
    assert122(test.getDocuments().get(3));
    assert123(test.getDocuments().get(4));
    assert222(test.getDocuments().get(5));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_posMaster.getClass().getSimpleName() + "[DbPos]", _posMaster.toString());
  }

}
