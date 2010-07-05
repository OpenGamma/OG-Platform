/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.financial.InMemoryRegionRepository.REGIONS_FILE_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Test InMemoryRegionRepository.
 */
public class InMemoryRegionRepositoryTest {

  @Test
  public void testConstructor() throws URISyntaxException {
    RegionRepository regionRepository = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
    System.err.println("Constructed and indexed");
    LocalDate now = LocalDate.now(Clock.system(TimeZone.UTC));
    System.err.println("Got now");
    Set<Region> allOfType = regionRepository.getAllOfType(now, "Political", RegionType.INDEPENDENT_STATE);
    System.err.println("getAllOfType");
    assertEquals(193, allOfType.size());
    assertEquals(278, regionRepository.getHierarchyRoot(now, "Political").getSubRegions().size());
    System.err.println("getHierarchyRoot");
    Region ukRegion = regionRepository.getHierarchyNode(now, "Political", "United Kingdom");
    System.err.println("getHierarchyNode");
    assertNotNull(ukRegion);
    assertTrue(allOfType.contains(ukRegion));
    assertEquals(RegionType.INDEPENDENT_STATE, ukRegion.getRegionType());
    assertEquals(regionRepository.getHierarchyRoot(now, "Political"), ukRegion.getSuperRegion());
    assertEquals(0, ukRegion.getSubRegions().size());
    
    assertEquals(regionRepository.getHierarchyRoot(now, "Political"), regionRepository.getHierarchyNode(now, "Political", "World"));
    System.err.println("more getHierarchyNode");
  }

  @Test(expected = OpenGammaRuntimeException.class)
  // TODO: check if this should throw exception or return null
  public void test_getHierarchyNode_unknownName() throws URISyntaxException {
    RegionRepository regionRepository = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
    LocalDate now = LocalDate.now(Clock.system(TimeZone.UTC));
    regionRepository.getHierarchyNode(now, "Incorrect Name", "World");
  }

  @Test
  public void test_getHierarchyRoot_unknownName() throws URISyntaxException {
    RegionRepository regionRepository = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
    LocalDate now = LocalDate.now(Clock.system(TimeZone.UTC));
    assertEquals(null, regionRepository.getHierarchyRoot(now, "Incorrect Name"));
  }

}
