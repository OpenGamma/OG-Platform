/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link com.opengamma.master.legalentity.impl.InMemoryLegalEntityMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryLegalEntityMasterTest {

  private static String NAME = "FooBar";
  private static ExternalId ID_ISIN_12345 = ExternalId.of(ExternalSchemes.ISIN, "12345");
  private static ExternalId ID_FOO = ExternalId.of("FOO", "979");
  private static ExternalId ID_BAR = ExternalId.of("BAR", "987654");
  private static ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "VAL1");
  private static ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "VAL2");
  private static ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_FOO);
  private static ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_ISIN_12345, ID_FOO);
  private static ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_OTHER1);

  private InMemoryLegalEntityMaster master;
  private LegalEntityDocument addedDoc;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryLegalEntityMaster();
    final ManageableLegalEntity inputLegalEntity = new MockLegalEntity(NAME, BUNDLE_FULL, Currency.GBP);
    LegalEntityDocument inputDoc = new LegalEntityDocument(inputLegalEntity);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueId.of("A", "B"));
  }

  public void test_get_match() {
    LegalEntityDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemLen", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  //-------------------------------------------------------------------------
  public void test_search_oneId_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_OTHER1);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneId_mic() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_ISIN_12345);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneId_ccid() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(ID_ISIN_12345);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_oneBundle_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    LegalEntitySearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneBundle_full() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_FULL);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneBundle_part() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest(BUNDLE_PART);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_twoBundles_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_twoBundles_oneMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_OTHER1);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_twoBundles_bothMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_FOO);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_name_noMatch() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName("No match");
    LegalEntitySearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_name_match() {
    LegalEntitySearchRequest request = new LegalEntitySearchRequest();
    request.setName(NAME);
    LegalEntitySearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

}
