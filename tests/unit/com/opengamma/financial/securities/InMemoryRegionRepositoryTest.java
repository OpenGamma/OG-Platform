/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.securities;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Resources;
import com.opengamma.financial.security.InMemoryRegionRepository;
import com.opengamma.financial.security.Region;
import com.opengamma.financial.security.RegionRepository;
import com.opengamma.financial.security.RegionType;

/**
 * Unit tests for the InMemoryRegionRepository
 */

public class InMemoryRegionRepositoryTest {
  @Test
  public void testConstructor() throws URISyntaxException {
    URL countryCSV = Resources.getResource("com/opengamma/financial/securities/countrylist_test.csv");
    RegionRepository regionRepository = new InMemoryRegionRepository(new File(countryCSV.toURI()));
    System.err.println("Constructed and indexed");
    LocalDate now = Clock.system(TimeZone.UTC).today();
    System.err.println("Got now");
    Set<Region> allOfType = regionRepository.getAllOfType(now, "Political", RegionType.INDEPENDENT_STATE);
    System.err.println("getAllOfType");
    Assert.assertEquals(193, allOfType.size());
    Assert.assertEquals(272, regionRepository.getHierarchyRoot(now, "Political").getSubRegions().size());
    System.err.println("getHierarchyRoot");
    Region ukRegion = regionRepository.getHierarchyNode(now, "Political", "United Kingdom");
    System.err.println("getHierarchyNode");
    Assert.assertNotNull(ukRegion);
    Assert.assertTrue(allOfType.contains(ukRegion));
    Assert.assertEquals(RegionType.INDEPENDENT_STATE, ukRegion.getRegionType());
    Assert.assertEquals(regionRepository.getHierarchyRoot(now, "Political"), ukRegion.getSuperRegion());
    Assert.assertEquals(0, ukRegion.getSubRegions().size());
    
    Assert.assertEquals(regionRepository.getHierarchyRoot(now, "Politcal"), regionRepository.getHierarchyNode(now, "Political", "World"));
    System.err.println("more getHierarchyNode");
    
    Assert.assertNull(regionRepository.getHierarchyNode(now, "Incorrect Name", "World"));
    Assert.assertNull(regionRepository.getHierarchyRoot(now, "Incorrect Name"));
  }
}
