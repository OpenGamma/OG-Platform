/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.world.region.InMemoryRegionRepository;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionFileReader;
import com.opengamma.financial.world.region.RegionMaster;
import com.opengamma.financial.world.region.RegionSearchRequest;
import com.opengamma.financial.world.region.RegionType;

/**
 * Test InMemoryRegionRepository.
 */
public class InMemoryRegionRepositoryTest {

  @Test
  public void testConstructor() throws URISyntaxException {
    RegionMaster regionRepository = new InMemoryRegionRepository();
    RegionFileReader.populateMaster(regionRepository, new File(RegionFileReader.REGIONS_FILE_PATH));
    System.err.println("Constructed and indexed");
    LocalDate now = LocalDate.now(Clock.system(TimeZone.UTC));
    System.err.println("Got now");
    RegionSearchRequest searchReq = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.TYPE_COLUMN, RegionType.INDEPENDENT_STATE);
    searchReq.setGraphIncluded(true);
    Set<Region> allOfType = regionRepository.searchRegions(searchReq).getResults();
    System.err.println("getAllOfType");
    assertEquals(193, allOfType.size());
    searchReq = null; // safety
    
    RegionSearchRequest searchReq2 = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME);
    searchReq2.setRootRequest(true);
    searchReq2.setGraphIncluded(true);
    assertEquals(278, regionRepository.searchRegions(searchReq2).getBestResult().getSubRegions().size());
    System.err.println("getHierarchyRoot");
    searchReq2 = null; // safety
    
    RegionSearchRequest searchReq3 = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.NAME_COLUMN, "United Kingdom");
    searchReq3.setGraphIncluded(true);
    Region ukRegion = regionRepository.searchRegions(searchReq3).getBestResult();
    System.err.println("getHierarchyNode");
    assertNotNull(ukRegion);
    assertTrue(allOfType.contains(ukRegion));
    assertEquals(RegionType.INDEPENDENT_STATE, ukRegion.getRegionType());
    
    RegionSearchRequest searchReq4 = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME);
    searchReq4.setRootRequest(true);
    searchReq4.setGraphIncluded(true);
    assertEquals(regionRepository.searchRegions(searchReq4).getBestResult(), ukRegion.getSuperRegion());
    searchReq4 = null;
    assertEquals(0, ukRegion.getSubRegions().size());
        
    RegionSearchRequest searchReq5 = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME);
    searchReq5.setRootRequest(true);
    searchReq5.setGraphIncluded(true);
    RegionSearchRequest searchReq6 = new RegionSearchRequest(RegionMaster.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.NAME_COLUMN, "World");
    searchReq6.setGraphIncluded(true);
    assertEquals(regionRepository.searchRegions(searchReq5).getBestResult(), regionRepository.searchRegions(searchReq6).getBestResult());
    System.err.println("more getHierarchyNode");
  }

  @Test(expected = OpenGammaRuntimeException.class)
  // TODO: check if this should throw exception or return null
  public void test_getHierarchyNode_unknownName() throws URISyntaxException {
    RegionMaster regionRepository = new InMemoryRegionRepository();
    RegionFileReader.populateMaster(regionRepository, new File(RegionFileReader.REGIONS_FILE_PATH));
    RegionSearchRequest searchReq = new RegionSearchRequest("Incorrect Name", InMemoryRegionRepository.NAME_COLUMN, "World");
    assertNull(regionRepository.searchRegions(searchReq).getBestResult());
  }

}
