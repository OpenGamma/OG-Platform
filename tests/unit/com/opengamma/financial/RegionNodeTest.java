/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.financial.InMemoryRegionRepository.REGIONS_FILE_PATH;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionNodeTest {

  private static final Logger s_logger = LoggerFactory.getLogger(RegionNodeTest.class);

  private RegionRepository _regionRepository1;
  private RegionRepository _regionRepository2;

  @Before
  public void setup() {
    // Use two repositories so that objects are not referentially equal
    _regionRepository1 = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
    _regionRepository2 = new InMemoryRegionRepository(new File(REGIONS_FILE_PATH));
  }

  @Test
  public void testHashCode () {
    final Region ukRegion = _regionRepository1.getHierarchyNode(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, "United Kingdom");
    int hc = ukRegion.hashCode ();
    s_logger.debug("Hashcode = {}", hc);
  }

  @Test
  public void testEquals() {
    final Region ukRegion1 = _regionRepository1.getHierarchyNode(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, "United Kingdom");
    final Region usRegion = _regionRepository1.getHierarchyNodes(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, InMemoryRegionRepository.ISO_COUNTRY_2,
        "US").first();
    final Region ukRegion2 = _regionRepository2.getHierarchyNode(LocalDate.now(Clock.system(TimeZone.UTC)), InMemoryRegionRepository.POLITICAL_HIERARCHY_NAME, "United Kingdom");
    assertTrue(ukRegion1.equals(ukRegion1));
    assertFalse(ukRegion1.equals(usRegion));
    assertTrue(ukRegion1.equals(ukRegion2));
    assertFalse(usRegion.equals(ukRegion1));
    assertTrue(usRegion.equals(usRegion));
    assertFalse(usRegion.equals(ukRegion2));
    assertTrue(ukRegion2.equals(ukRegion1));
    assertFalse(ukRegion2.equals(usRegion));
    assertTrue(ukRegion2.equals(ukRegion2));
  }

}
