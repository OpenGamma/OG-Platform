/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.region.master.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.opengamma.core.region.RegionClassification;
import com.opengamma.financial.world.region.master.ManageableRegion;
import com.opengamma.financial.world.region.master.RegionDocument;
import com.opengamma.financial.world.region.master.RegionMaster;
import com.opengamma.financial.world.region.master.RegionSearchRequest;
import com.opengamma.financial.world.region.master.memory.InMemoryRegionMaster;

/**
 * Test RegionFileReader.
 */
public class RegionFileReaderTest {

  private static RegionMaster _regionMaster;

  @BeforeClass
  public static void setUpOnce() {
    _regionMaster = new InMemoryRegionMaster();
    RegionFileReader.populate(_regionMaster);
  }

  @Test
  public void test_search_regionType() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setClassification(RegionClassification.INDEPENDENT_STATE);
    assertEquals(193, _regionMaster.search(request).getDocuments().size());
  }

  @Test
  public void test_search_all() {
    RegionSearchRequest request = new RegionSearchRequest();
    assertEquals(279, _regionMaster.search(request).getDocuments().size());
  }

  @Test
  public void test_search_name() {
    RegionSearchRequest request = new RegionSearchRequest();
    request.setName("United Kingdom");
    assertEquals(1, _regionMaster.search(request).getDocuments().size());
    RegionDocument doc = _regionMaster.search(request).getFirstDocument();
    ManageableRegion ukRegion = doc.getRegion();
    assertNotNull(ukRegion);
    assertEquals("United Kingdom", ukRegion.getName());
    assertEquals(RegionClassification.INDEPENDENT_STATE, ukRegion.getClassification());
    assertEquals("GB", ukRegion.getCountryISO());
    assertEquals("GBP", ukRegion.getCurrency().getISOCode());
    assertEquals(2, ukRegion.getParentRegionIds().size());
    
    RegionDocument gotDoc = _regionMaster.get(doc.getRegionId());
    assertEquals(doc, gotDoc);
  }

  @Test
  public void test_search_unknownName() {
    RegionSearchRequest searchReq = new RegionSearchRequest();
    searchReq.setName("Unknown");
    assertEquals(0, _regionMaster.search(searchReq).getDocuments().size());
  }

}
