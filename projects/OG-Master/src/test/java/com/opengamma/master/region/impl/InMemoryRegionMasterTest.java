/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import static org.testng.AssertJUnit.assertEquals;

import org.threeten.bp.ZoneId;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link InMemoryRegionMaster}.
 */
@Test(groups = TestGroup.UNIT)
public class InMemoryRegionMasterTest {

  private static String NAME = "France";
  private static ExternalId ID_COUNTRY = ExternalSchemes.countryRegionId(Country.FR);
  private static ExternalId ID_CURENCY = ExternalSchemes.currencyRegionId(Currency.EUR);
  private static ExternalId ID_TIME_ZONE = ExternalSchemes.timeZoneRegionId(ZoneId.of("Europe/Paris"));
  private static ExternalId ID_OTHER1 = ExternalId.of("TEST_SCHEME", "The French");
  private static ExternalId ID_OTHER2 = ExternalId.of("TEST_SCHEME", "France");
  private static ExternalIdBundle BUNDLE_FULL = ExternalIdBundle.of(ID_COUNTRY, ID_TIME_ZONE, ID_CURENCY);
  private static ExternalIdBundle BUNDLE_PART = ExternalIdBundle.of(ID_COUNTRY, ID_CURENCY);
  private static ExternalIdBundle BUNDLE_OTHER = ExternalIdBundle.of(ID_COUNTRY, ID_TIME_ZONE, ID_OTHER1);

  private InMemoryRegionMaster master;
  private RegionDocument addedDoc;

  @BeforeMethod
  public void setUp() {
    master = new InMemoryRegionMaster();
    ManageableRegion inputRegion = new ManageableRegion();
    inputRegion.setName(NAME);
    inputRegion.setFullName(NAME);
    inputRegion.setClassification(RegionClassification.INDEPENDENT_STATE);
    inputRegion.setCountry(Country.FR);
    inputRegion.setCurrency(Currency.EUR);
    inputRegion.setTimeZone(ZoneId.of("Europe/Paris"));
    RegionDocument inputDoc = new RegionDocument(inputRegion);
    addedDoc = master.add(inputDoc);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions = DataNotFoundException.class)
  public void test_get_noMatch() {
    master.get(UniqueId.of("A", "B"));
  }

  public void test_get_match() {
    RegionDocument result = master.get(addedDoc.getUniqueId());
    assertEquals(UniqueId.of("MemReg", "1"), result.getUniqueId());
    assertEquals(addedDoc, result);
  }

  //-------------------------------------------------------------------------
  public void test_search_oneId_noMatch() {
    RegionSearchRequest request = new RegionSearchRequest(ID_OTHER1);
    RegionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneId_mic() {
    RegionSearchRequest request = new RegionSearchRequest(ID_COUNTRY);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneId_ccid() {
    RegionSearchRequest request = new RegionSearchRequest(ID_COUNTRY);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_oneBundle_noMatch() {
    RegionSearchRequest request = new RegionSearchRequest(BUNDLE_OTHER);
    request.setExternalIdSearch(request.getExternalIdSearch().withSearchType(ExternalIdSearchType.ALL));
    RegionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_oneBundle_full() {
    RegionSearchRequest request = new RegionSearchRequest(BUNDLE_FULL);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_oneBundle_part() {
    RegionSearchRequest request = new RegionSearchRequest(BUNDLE_PART);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_twoBundles_noMatch() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_OTHER1);
    request.addExternalId(ID_OTHER2);
    RegionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_twoBundles_oneMatch() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_COUNTRY);
    request.addExternalId(ID_OTHER1);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  public void test_search_twoBundles_bothMatch() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.addExternalId(ID_COUNTRY);
    request.addExternalId(ID_CURENCY);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_name_noMatch() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setName("No match");
    RegionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_name_match() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setName(NAME);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

  //-------------------------------------------------------------------------
  public void test_search_classification_noMatch() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.DEPENDENCY);
    RegionSearchResult result = master.search(request);
    assertEquals(0, result.getDocuments().size());
  }

  public void test_search_classification_match() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.INDEPENDENT_STATE);
    RegionSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    assertEquals(addedDoc, result.getFirstDocument());
  }

}
