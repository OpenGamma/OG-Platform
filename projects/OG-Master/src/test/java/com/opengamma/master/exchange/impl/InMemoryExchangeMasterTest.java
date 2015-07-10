/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.threeten.bp.ZoneId;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryExchangeMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryExchangeMasterTest {

  private static String NAME = "LIFFE";
  private static ExternalId ID_LIFFE_MIC = ExternalId.of(ExternalSchemes.ISO_MIC, "XLIF");
  private static ExternalId ID_LIFFE_CCID = ExternalId.of("COPP_CLARK_CENTER_ID", "979");
  private static ExternalId ID_LIFFE_CCNAME = ExternalId.of("COPP_CLARK_NAME", "Euronext LIFFE (UK contracts)");
  private static ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "EURONEXT LIFFE");
  private static ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "LIFFE");
  private static ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_LIFFE_CCID);
  private static ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCID);
  private static ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_LIFFE_MIC, ID_LIFFE_CCNAME, ID_OTHER1);
  private static ExternalIdBundle GB = ExternalIdBundle.of(ExternalSchemes.countryRegionId(Country.GB));

  private InMemoryExchangeMaster master;
  private ExchangeDocument addedDoc;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryExchangeMaster();
    ManageableExchange inputExchange = new ManageableExchange(BUNDLE_FULL, NAME, GB, ZoneId.of("Europe/London"));
    ExchangeDocument inputDoc = new ExchangeDocument(inputExchange);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueId.of("A", "B"));
  }

  public void test_get_match() {
    ExchangeDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemExg", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  //-------------------------------------------------------------------------
  public void test_search_oneId_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_OTHER1);
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneId_mic() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneId_ccid() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_oneBundle_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneBundle_full() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_FULL);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneBundle_part() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE_PART);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_twoBundles_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_twoBundles_oneMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_LIFFE_MIC);
    request.addExternalId(ID_OTHER1);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_twoBundles_bothMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.addExternalId(ID_LIFFE_MIC);
    request.addExternalId(ID_LIFFE_CCID);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_name_noMatch() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName("No match");
    ExchangeSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_name_match() {
    ExchangeSearchRequest request = new ExchangeSearchRequest();
    request.setName(NAME);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

}
