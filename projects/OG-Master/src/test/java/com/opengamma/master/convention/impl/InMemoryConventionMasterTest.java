/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention.impl;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryConventionMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryConventionMasterTest {

  private static String NAME = "FooBar";
  private static ExternalId ID_ISIN_12345 = ExternalId.of(ExternalSchemes.ISIN, "12345");
  private static ExternalId ID_FOO = ExternalId.of("FOO", "979");
  private static ExternalId ID_BAR = ExternalId.of("BAR", "987654");
  private static ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "VAL1");
  private static ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "VAL2");
  private static ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_FOO);
  private static ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_ISIN_12345, ID_FOO);
  private static ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_ISIN_12345, ID_BAR, ID_OTHER1);

  private InMemoryConventionMaster master;
  private ConventionDocument addedDoc;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryConventionMaster();
    final ManageableConvention inputConvention = new MockConvention(NAME, BUNDLE_FULL, Currency.GBP);
    ConventionDocument inputDoc = new ConventionDocument(inputConvention);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueId.of("A", "B"));
  }

  public void test_get_match() {
    ConventionDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemCnv", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  //-------------------------------------------------------------------------
  public void test_search_oneId_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest(ID_OTHER1);
    ConventionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneId_mic() {
    ConventionSearchRequest request = new ConventionSearchRequest(ID_ISIN_12345);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneId_ccid() {
    ConventionSearchRequest request = new ConventionSearchRequest(ID_ISIN_12345);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_oneBundle_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    ConventionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneBundle_full() {
    ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_FULL);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneBundle_part() {
    ConventionSearchRequest request = new ConventionSearchRequest(BUNDLE_PART);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_twoBundles_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    ConventionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_twoBundles_oneMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_OTHER1);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_twoBundles_bothMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.addExternalId(ID_ISIN_12345);
    request.addExternalId(ID_FOO);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_name_noMatch() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName("No match");
    ConventionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_name_match() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName(NAME);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_replace_adds_uniqueid() {
    ConventionSearchRequest request = new ConventionSearchRequest();
    request.setName(NAME);
    ConventionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    ConventionDocument retrievedDoc = result.getFirstDocument();
    UniqueId uniqueId = retrievedDoc.getUniqueId();
    retrievedDoc.getValue().setUniqueId(null);
    ConventionDocument updatedDoc = master.update(retrievedDoc);
    assertEquals(uniqueId.toLatest(), updatedDoc.getUniqueId().toLatest());
    assertEquals(uniqueId.toLatest(), updatedDoc.getConvention().getUniqueId().toLatest());
  }

}
