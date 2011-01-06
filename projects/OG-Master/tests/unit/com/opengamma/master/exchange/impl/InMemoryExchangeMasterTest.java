/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.ExchangeUtils;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierSearchType;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.master.exchange.impl.InMemoryExchangeMaster;

/**
 * Test InMemoryExchangeMaster.
 */
public class InMemoryExchangeMasterTest {

  private static String NAME = "LIFFE";
  private static Identifier ID_LIFFE_MIC = Identifier.of(ExchangeUtils.ISO_MIC, "XLIF");
  private static Identifier ID_LIFFE_CCID = Identifier.of("COPP_CLARK_CENTER_ID", "979");
  private static Identifier ID_LIFFE_CCNAME = Identifier.of("COPP_CLARK_NAME", "Euronext LIFFE (UK contracts)");
  private static Identifier ID_OTHER1 = Identifier.of("TEST_SCHEME", "EURONEXT LIFFE");
  private static Identifier ID_OTHER2 = Identifier.of("TEST_SCHEME", "LIFFE");
  private static IdentifierBundle BUNDLE_FULL = IdentifierBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_LIFFE_CCID);
  private static IdentifierBundle BUNDLE_PART = IdentifierBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCID);
  private static IdentifierBundle BUNDLE_OTHER = IdentifierBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_OTHER1);
  private static IdentifierBundle GB = IdentifierBundle.of(RegionUtils.countryRegionId("GB"));

  private InMemoryExchangeMaster master;
  private ExchangeDocument addedDoc;

  @Before
  public void setUp() {
    master = new InMemoryExchangeMaster();
    ManageableExchange inputExchange = new ManageableExchange(BUNDLE_FULL, NAME, GB, TimeZone.of("Europe/London"));
    ExchangeDocument inputDoc = new ExchangeDocument(inputExchange);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expected = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueIdentifier.of("A", "B"));
  }

  public void test_get_match() {
    ExchangeDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(Identifier.of("MemExg", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneId_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_OTHER1);
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_oneId_mic() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  @Test
  public void test_search_oneId_ccid() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_oneBundle_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_OTHER);
    request.getExchangeKeys().setSearchType(IdentifierSearchType.ALL);
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_oneBundle_full() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_FULL);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  @Test
  public void test_search_oneBundle_part() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_PART);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_twoBundles_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(ID_OTHER1);
    request.addExchangeKey(ID_OTHER2);
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_twoBundles_oneMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(ID_LIFFE_MIC);
    request.addExchangeKey(ID_OTHER1);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  @Test
  public void test_search_twoBundles_bothMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExchangeKey(ID_LIFFE_MIC);
    request.addExchangeKey(ID_LIFFE_CCID);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("No match");
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  @Test
  public void test_search_name_match() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName(NAME);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

}
