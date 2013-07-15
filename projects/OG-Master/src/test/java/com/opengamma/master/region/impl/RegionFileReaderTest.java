/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.core.region.RegionClassification;
import com.opengamma.master.region.ManageableRegion;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link RegionFileReader}.
 */
@Test(groups = TestGroup.UNIT)
public class RegionFileReaderTest {

  private static RegionMaster _regionMaster;

  @BeforeClass
  public static void setUpOnce() {
    _regionMaster = new InMemoryRegionMaster();
    RegionFileReader.createPopulated(_regionMaster);
  }

  public void test_search_regionType() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.INDEPENDENT_STATE);
    assertEquals(193, _regionMaster.search(request).getDocuments().size());
  }

  public void test_search_all() {
    RegionSearchRequest request = new RegionSearchRequest();
    int all = _regionMaster.search(request).getDocuments().size();
    request.setClassification(RegionClassification.MUNICIPALITY);
    int municipalities = _regionMaster.search(request).getDocuments().size();
    assertEquals(283, all - municipalities);
  }

  public void test_search_name() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setName("United Kingdom");
    assertEquals(1, _regionMaster.search(request).getDocuments().size());
    RegionDocument doc = _regionMaster.search(request).getFirstDocument();
    ManageableRegion ukRegion = doc.getRegion();
    assertNotNull(ukRegion);
    assertEquals("United Kingdom", ukRegion.getName());
    assertEquals(RegionClassification.INDEPENDENT_STATE, ukRegion.getClassification());
    assertEquals(Country.GB, ukRegion.getCountry());
    assertEquals(Currency.GBP, ukRegion.getCurrency());
    assertEquals(3, ukRegion.getParentRegionIds().size());
    
    RegionDocument gotDoc = _regionMaster.get(doc.getUniqueId());
    assertEquals(doc, gotDoc);
  }

  public void test_search_unknownName() {
    RegionSearchRequest searchReq = new RegionSearchRequest();
    searchReq.setName("Unknown");
    assertEquals(0, _regionMaster.search(searchReq).getDocuments().size());
  }

}
